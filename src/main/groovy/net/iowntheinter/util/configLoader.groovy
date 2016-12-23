package net.iowntheinter.util

import com.jayway.jsonpath.JsonPath
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.kvdn.storage.kvdnSession
import net.iowntheinter.kvdn.util.distributedWaitGroup
import net.iowntheinter.util.config.vaultConfigLoader

/**
 * Created by g on 9/19/16.
 * the idea here is that you ask for a json path, that looks up a variable from the config
 * if the value starts with '$', we treat it as an inderection and try to load it from the system ENV
 */
public class configLoader {
    Logger log
    Vertx vertx
    Map configs = [:]
    Logger logger
    configLoader(Vertx v) {
        this.vertx = v
        logger = LoggerFactory.getLogger(this.class.getName())
    }

    void loadConfigSet(Set configPaths, cb) {
        def wg = new distributedWaitGroup(configPaths,cb, vertx)
        configPaths.each { String path ->
            JsonObject c = vertx.getOrCreateContext().config()
            String result = JsonPath.read(c.toString(), path)
            def marker = result.take(2)
            switch (marker) {
                case '$$': // system enviornment var
                    lookupSysEnv(result.substring(2),path, {
                        wg.ack(path)
                    })
                    break
                case '$@':
                    extConfigLoader(result.substring(2),path, { wg.ack(path) })
                    break
            }
        }
    }

    String getConfig(String path) {
        return configs[path]
    }

    void lookupSysEnv(String var,String path, cb) {
        configs[path] = System.getenv(var)
        logger.trace("looked up sys env: " + configs[var])
        cb()
    }

    void extConfigLoader(String url, String path, cb) {
        def extsys = url.tokenize(':')[0]
        switch(extsys){
            case 'kvdn': // $@kvdn://this/that?whatever
                def s = new kvdnSession(vertx)
                s.init({
                    def tokens = url.minus("kvdn://").tokenize('?')[0].tokenize('/')
                    def key =  url.minus("kvdn://").tokenize('?')[1]
                    KvTx tx = s.newTx("${tokens[0]}:${tokens[1]}") as KvTx
                    tx.get(key,{ res ->
                        if(res.result)
                            configs[path]=res.result
                        cb()
                    })
                },{ error -> logger.error(error)})
                break
            case 'http' || 'https':
                HttpClient client = vertx.createHttpClient()
                client.getNow(url,{ resp ->
                    resp.bodyHandler({ body ->
                        configs[path]=body.toString()
                        cb()
                    })
                })
                break
            case 'file':
                vertx.fileSystem().readFile(url.minus("file://"), { asyncResult ->
                    if(asyncResult.succeeded()){
                        configs[path] = asyncResult.result()
                        cb()
                    }else{
                        logger.error(asyncResult.cause())
                        asyncResult.cause().printStackTrace()
                    }
                })
                break
            //example: '$@vault://secret/this/that?akey'
            case 'vault':
                def vcl = new vaultConfigLoader(vertx)
                vcl.loadConfig(url.minus("vault://"),{ vault_result ->
                    if(!vault_result.error){
                        configs[path] = vault_result.result
                        cb()
                    }else{
                        logger.error(vault_result.error)
                    }
                })
                break
            default:
                throw new Exception( extsys + " configuration Unimplemented ")
                //comploader(extsys,url,cb)

        }
    }

    void setNodeInternalConfig(String key, String value){
        vertx.sharedData().getLocalMap("CSinternalconfig").put(key,value)
    }
    String getNodeInternalConfig(String key){
        return vertx.sharedData().getLocalMap("CSinternalconfig").get(key)
    }
}
