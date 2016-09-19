package net.iowntheinter.util

import com.jayway.jsonpath.JsonPath
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

/**
 * Created by g on 9/19/16.
 */
class configLoader {
    Vertx vertx
    configLoader(Vertx v){
        this.vertx = v
    }
    String getConfig(String path){
        JsonObject c = vertx.getOrCreateContext().config()
        String result = JsonPath.read(c.toString(),path)
        if(result.take(1) == '$')
            result=System.getenv(result.substring(1))
        return result
    }
}
