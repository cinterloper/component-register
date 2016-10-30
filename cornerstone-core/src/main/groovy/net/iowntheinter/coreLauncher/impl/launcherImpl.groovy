package net.iowntheinter.coreLauncher.impl

import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.DeploymentOptionsConverter
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import net.iowntheinter.componentRegister.component.impl.DockerTaskRX
import net.iowntheinter.componentRegister.component.impl.VXVerticle
import net.iowntheinter.coreLauncher.componentLauncher
import net.iowntheinter.kvdn.util.distributedWaitGroup

/**
 * Created by g on 10/21/16.
 */
class launcherImpl implements componentLauncher {
    Vertx vertx

    launcherImpl(Vertx vertx) {
        this.vertx = vertx
    }

    @Override
    void launchGroup(JsonObject groupDesc, Handler<AsyncResult<JsonObject>> resultHandler) {
        def wg
        def GROUPID=UUID.randomUUID()
        Map<String,Map> lg = [:]
        def containers = groupDesc.getJsonObject('ext').getJsonObject('docker')
        def verticles = groupDesc.getJsonObject('vx')
        def components = containers.mergeIn(verticles)

        verticles.fieldNames().each { key ->
            def nextV = verticles.getJsonObject(key)
            if (nextV.getBoolean('enabled')) {
                def lid = UUID.randomUUID().toString()
                lg.put(lid, [name: key, config: nextV, type: "vertx"])
            }
        }
        containers.fieldNames().each { key ->
            def nextC = containers.getJsonObject(key)
            if (nextC.getBoolean('enabled')) {
                def lid = UUID.randomUUID().toString()
                lg.put(lid, [name: key, config: nextC, type: "docker"])
            }
        }
        wg=new distributedWaitGroup(lg.keySet(),{
            vertx.eventBus().send("_cornerstone:start",GROUPID)
        },vertx) as distributedWaitGroup
        lg.each { comp, launchdata ->

            switch(launchdata.type){
                case 'docker':
                    break;
                case 'vertx':
                    this.deployVertxComponent(launchdata.config,{ result ->
                        (wg as distributedWaitGroup).ack(comp)
                    })
            }
        }

    }

    @Override
    void launchContainer(JsonObject containerDesc, Handler<AsyncResult<JsonObject>> resultHandler) {
       def d = new DockerTaskRX(containerDesc.getString("name"),containerDesc)
        d.start({ result ->

        })
    }

    @Override
    void deployVertxComponent(JsonObject componentDesc, Handler<AsyncResult<JsonObject>> resultHandler) {
        DeploymentOptions d = new DeploymentOptions()
        if (componentDesc.containsKey('deploymentOpts'))
            DeploymentOptionsConverter.fromJson(nextV.getJsonObject('deploymentOpts'), d)
        def v = new VXVerticle(vertx, d, key)
        v.start(resultHandler)
    }


}
