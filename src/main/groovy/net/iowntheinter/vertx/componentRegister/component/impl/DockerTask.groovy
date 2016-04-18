package net.iowntheinter.vertx.componentRegister.component.impl

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.HostConfig
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import net.iowntheinter.vertx.componentRegister.component.componentType

/**
 * Created by grant on 4/10/16.
 */


class DockerTask implements componentType {
    def dockerClient
    DockerClient docker
    ContainerCreation cctr
    String tasktype

    DockerTask(final Map cfg) {
        docker = DefaultDockerClient.fromEnv().build();
        Map pathBinds = cfg.binds
        def hcfgb = HostConfig.builder()
        pathBinds.each { k, v ->
            hcfgb.appendBinds("${k}:${v}")  //support attributes like read only later
        }
        final HostConfig hcfg = hcfgb.build()

        cctr = docker.createContainer(ContainerConfig.builder()
                .env(cfg.env as List<String>)
                .image(cfg.image as String)
                .volumes(cfg.volumes as Set<String>)
                .exposedPorts(cfg.ports as Set<String>)
                .hostConfig(hcfg)
                .build())

        //= new DefaultDockerClient("unix:///var/run/docker.sock");
        // --or--
        //.uri(URI.create("https://boot2docker:2376"))
        // .dockerCertificates(new DockerCertificates(Paths.get("/Users/rohan/.docker/boot2docker-vm/")))
        // .build();
    }


    @Override
    void start(Handler<AsyncResult> cb) {

    }

    @Override
    void stop(Handler<AsyncResult> cb) {

    }

    @Override
    void registrationEvent(Map peerNotification, Handler<AsyncResult> cb) {
        // use the docer driver to exec a task on the container
    }


}
