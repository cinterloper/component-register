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
        def wg = new distributedWaitGroup(configPaths, vertx)
        configPaths.each { String path ->
            JsonObject c = vertx.getOrCreateContext().config()
            String result = JsonPath.read(c.toString(), path)
            def marker = result.take(2)
            switch (marker) {
                case '$$':
                    lookupSysEnv(result.substring(1), { wg.ack(path, cb) })
                    break
                case '$@':
                    extConfigLoader(result.substring(1), { wg.ack(path, cb) })
                    break
            }
        }
    }

    String getConfig(String path) {
        return configs[path]
    }

    void lookupSysEnv(String var, cb) {
        configs[var] = System.getenv(var)
        cb()
    }

    void extConfigLoader(String url, cb) {
        def extsys = url.tokenize(':')[0]
        switch(extsys){
            case 'kvdn': // $@kvdn://this/that/whatever
                def s = new kvdnSession(vertx)
                def tokens = url.tokenize('/')
                KvTx tx = s.newTx("${tokens[1]}:${tokens[2]}") as KvTx
                tx.get(tokens[3],cb)
                break
            case 'http' || 'https':
                HttpClient client = vertx.createHttpClient()
                client.getNow(url,{ resp -> cb(resp)})
                break
            default:
                comploader(extsys,url,cb)

        }
    }

    void setNodeInternalConfig(String key, String value){
        vertx.sharedData().getLocalMap("CSinternalconfig").put(key,value)
    }
    String getNodeInternalConfig(String key){
        return vertx.sharedData().getLocalMap("CSinternalconfig").get(key)
    }
}
