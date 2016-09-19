package net.iowntheinter.util

import com.jayway.jsonpath.JsonPath
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject

/**
 * Created by g on 9/19/16.
 * the idea here is that you ask for a json path, that looks up a variable from the config
 * if the value starts with '$', we treat it as an inderection and try to load it from the system ENV
 */
public class configLoader {
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
