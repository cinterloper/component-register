package net.iowntheinter.vertx.componentRegister.component.impl

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.ContainerInfo
import com.spotify.docker.client.messages.HostConfig
import com.spotify.docker.client.messages.PortBinding
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import net.iowntheinter.vertx.componentRegister.component.componentType
import org.apache.tinkerpop.gremlin.structure.util.Host

/**
 * Created by grant on 4/10/16.
 */


class DockerTask implements componentType {
    def dockerClient
    DockerClient docker
    ContainerCreation cctr
    List volumes
    Map volumeBinds
    List ports
    HostConfig dhostcfg
    String id

    DockerTask(final Map cfg) {
        docker = DefaultDockerClient.builder().uri("unix:///var/run/docker.sock").build();
        volumeBinds = (cfg.volumeBinds as JsonObject ).getMap()
        ports = (cfg.ports as JsonArray).getList()
        volumes = (cfg.volumes as JsonArray).getList()


        def dhostcfgbldr = HostConfig.builder()

        final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
        (cfg.get('portBinds')as JsonObject).getMap().each { ctrport, hstport ->
            portBindings.put(ctrport, ([PortBinding.of("0.0.0.0", hstport as String)]))
        }

        dhostcfgbldr.portBindings(portBindings)

        cctr = docker.createContainer(ContainerConfig.builder()
                .env(cfg.env as List<String>)
                .image(cfg.image as String)
                .volumes(cfg.volumes as Set<String>)
                .exposedPorts(cfg.ports as Set<String>)
                .hostConfig(dhostcfgbldr.build())
                .cmd(cfg.cmd as List<String>)
                .build())

        //= new DefaultDockerClient("unix:///var/run/docker.sock");
        // --or--
        //.uri(URI.create("https://boot2docker:2376"))
        // .dockerCertificates(new DockerCertificates(Paths.get("/Users/rohan/.docker/boot2docker-vm/")))
        // .build();
    }


    @Override
    void start(Closure cb) {
        this.id = cctr.id()
        final ContainerInfo info = docker.inspectContainer(this.id);
        docker.startContainer(id);
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
