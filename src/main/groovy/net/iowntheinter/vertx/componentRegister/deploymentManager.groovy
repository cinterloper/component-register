package net.iowntheinter.vertx.componentRegister

import io.vertx.core.json.JsonObject

/**
 * Created by grant on 3/23/16.
 */
interface deploymentManager {
    void deploy(String name, JsonObject config, Closure cb)
    void undeploy(String id, Closure cb)

}
