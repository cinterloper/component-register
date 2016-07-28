package net.iowntheinter.util.crypto

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.jwt.JWTOptions
import net.iowntheinter.util.injector
import net.iowntheinter.util.crypto.jwt

/**
 * Created by g on 7/25/16.
 * this should create a JWT token based on the componentcfg
 * it will return this token, which will be 'injected' in the enviornment of the component being launched
 */
class JWTInjector implements injector {
    JsonObject config

    @Override
    Set inject(JsonObject componentcfg, Vertx vertx) {
        try {
            config = vertx.getOrCreateContext().config()
            JsonObject cryptcfg = config.getJsonObject("crypto").getJsonObject("jwt")
            cryptcfg.put('password',System.getenv("KEYSTORE_PASS"))
            LoggerFactory.getLogger(this.class.getName()).info("jwt cryptconfig ${cryptcfg} def algo: ${new JWTOptions().getAlgorithm()}")

            def j = new jwt(vertx, new JsonObject().put('keyStore',cryptcfg))

            def sk = j.createToken(new JsonObject().put('sub', componentcfg.getString('launchid')), new JWTOptions())
            assert sk != null
            LoggerFactory.getLogger(this.class.getName()).info("injector returning ${sk}")

            return ["JWTOKEN=${sk}"]
        } catch (e) {
            LoggerFactory.getLogger(this.class.getName()).error(e)
            e.printStackTrace()
        }

    }
}
