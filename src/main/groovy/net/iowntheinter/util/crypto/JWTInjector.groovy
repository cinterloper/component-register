package net.iowntheinter.util.crypto

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTOptions
import net.iowntheinter.util.injector
import net.iowntheinter.util.crypto.jwt
/**
 * Created by g on 7/25/16.
 */
class JWTInjector implements injector {
    JsonObject config
    @Override
    Set inject(JsonObject componentcfg, Vertx vertx) {
        config=vertx.getOrCreateContext().config()
        def j = new jwt(vertx,config.getJsonObject("crypto").getJsonObject("jwt"))
        def sk = j.createToken(new JsonObject().put('sub',componentcfg.getString('launchid')),new JWTOptions())
        return ["JWTOKEN=${sk}"]
    }
}
