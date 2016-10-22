package net.iowntheinter.coreLauncher.impl

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.DeploymentOptionsConverter
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.componentRegister.component.impl.VXVerticle
import net.iowntheinter.coreLauncher.componentLauncher

/**
 * Created by g on 10/21/16.
 */
class launcherImpl implements componentLauncher {
    Vertx vertx
    launcherImpl(Vertx vertx){
        this.vertx = vertx
    }
    @Override
    void launchGroup(JsonObject groupDesc, Handler<AsyncResult<JsonObject>> resultHandler) {
        Map lg = [:]
        def containers = groupDesc.getJsonObject('ext').getJsonObject('docker')
        def verticles  = groupDesc.getJsonObject('vx')
        verticles.fieldNames().each { key ->
            def nextV = verticles.getJsonObject(key)
            if(nextV.getBoolean('enabled')){
                DeploymentOptions d = new DeploymentOptions()
                if(nextV.containsKey('deploymentOpts'))
                    DeploymentOptionsConverter.fromJson(nextV.getJsonObject('deploymentOpts'),d)
                def v = new VXVerticle(vertx,d,key)
                v.start(resultHandler)
            }
        }



    }

    @Override
    void launchContainer(JsonObject containerDesc, Handler<AsyncResult<JsonObject>> resultHandler) {

    }

    @Override
    void deployVertxComponent(JsonObject componentDesc, Handler<AsyncResult<JsonObject>> resultHandler) {

    }







}
