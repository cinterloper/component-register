package net.iowntheinter.util.crypto

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.jwt.JWTOptions
import net.iowntheinter.util.injector
import net.iowntheinter.util.crypto.jwt
import net.iowntheinter.util.configLoader

/** 
 * Created by g on 7/25/16.
 * this should create a JWT token based on the componentcfg
 * it will return this token, which will be 'injected' in the enviornment of the component being launched
 */
class JWTInjector implements injector {
    JsonObject config
    def c
    @Override
    void inject(JsonObject componentcfg, Vertx vertx,cb) {
        c = new configLoader(vertx)
        c.loadConfigSet(['$.crypto.jwt.pass'].toSet(),{
            try {
                config = vertx.getOrCreateContext().config()
                JsonObject cryptcfg = config.getJsonObject("crypto").getJsonObject("jwt")

                cryptcfg.put('password',c.getConfig('$.crypto.jwt.pass'))
                LoggerFactory.getLogger(this.class.getName()).info("jwt cryptconfig ${cryptcfg} def algo: ${new JWTOptions().getAlgorithm()}")

                def j = new jwt(vertx, new JsonObject().put('keyStore',cryptcfg))

                def sk = j.createToken(new JsonObject().put('sub', componentcfg.getString('launchid')), new JWTOptions())
                assert sk != null
                LoggerFactory.getLogger(this.class.getName()).info("injector returning ${sk}")

                cb([result:["JWT_TOKEN=${sk}"].toSet(),error:null]);
            } catch (e) {
                LoggerFactory.getLogger(this.class.getName()).error(e)
                cb([result:null,error:e])
            }
        })


    }
}
