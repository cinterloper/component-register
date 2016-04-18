package net.iowntheinter.vertx.componentRegister.component.impl

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.vertx.componentRegister.component.componentType

/**
 * Created by grant on 4/10/16.
 */


class DockerTask implements componentType {


    DockerClientImpl dockerClient
    Map cfg
    String image
    String name
    String tag
    String id
/*
meta
[
 image:
 tag: ?
 name:
]
 */
    DockerTask(Map meta, Map cfg) {
        dockerClient = new DockerClientImpl()
        def info = dockerClient.info()
        println("\ndockerinfo:${info}")
        image = meta.image
        tag = meta.tag ?: "latest"
        name = meta.name
        this.cfg =cfg
    }


    @Override
    void start(Closure cb) {
        println("\n dkr config: \n ${cfg}")
         println("new container: ${dockerClient.run(image, cfg, tag,name).container}")
    }

    @Override
    void stop(Closure cb) {

        cb([success: true, result: docker.killContainer(this.id)])
    }

    @Override
    void registrationEvent(Map peerNotification, Closure cb) {
        // use the docer driver to exec a task on the container
    }


}
