package net.iowntheinter.util.crypto

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.auth.jwt.impl.JWTAuthProviderImpl

/**
 * Created by g on 7/24/16.
 */
class jwt {
    Vertx vertx
    JWTAuthProviderImpl provider
    jwt(Vertx vertx, JsonObject cryptconfig){
        this.vertx = vertx
        provider = JWTAuth.create(this.vertx, cryptconfig)
    }
    String createToken(JsonObject tokenconfig, JWTOptions jwtOptions){
        try{
            return provider.generateToken(tokenconfig, jwtOptions)
        }        catch(e){
            LoggerFactory.getLogger(this.class.getName()).error(e)
        }
    }


}
