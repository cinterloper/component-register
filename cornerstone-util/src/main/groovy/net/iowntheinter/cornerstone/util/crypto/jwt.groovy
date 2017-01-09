package net.iowntheinter.cornerstone.util.crypto

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions

/**
 * Created by g on 7/24/16.
 */
class jwt {
    Vertx vertx
    def provider, logger
    jwt(Vertx vertx, JsonObject cryptconfig){
        this.vertx = vertx
        this.logger= LoggerFactory.getLogger(this.class.getName())
        logger.trace("jwt object created, vertx ${vertx} cryptconfig ${cryptconfig}")
        provider = JWTAuth.create(this.vertx, cryptconfig)
        logger.info("jwt cryptconfig: ${cryptconfig}")
    }
    String createToken(JsonObject tokenconfig, JWTOptions jwtOptions){
        try{
            provider = provider as JWTAuth
            logger.info("json jwtoptions: ${jwtOptions.toJson()}")
            return provider.generateToken(tokenconfig, jwtOptions)
        }        catch(e){
            logger.error(e)
            e.printStackTrace()
        }
    }


}
