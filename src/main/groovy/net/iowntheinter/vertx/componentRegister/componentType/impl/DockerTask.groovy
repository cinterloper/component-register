package net.iowntheinter.vertx.componentRegister.componentType.impl

import de.gesellix.docker.client.DockerClientImpl
import io.vertx.core.json.JsonObject

/**
 * Created by grant on 4/10/16.
 */


class DockerTask {

    DockerTask(JsonObject cfg){

    }

    def dockerClient = new DockerClientImpl(System.env.DOCKER_HOST)


}
