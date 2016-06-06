package net.iowntheinter.vertx.componentRegister.component.impl

/*
for this to be really async
will need to switch to https://github.com/shekhargulati/rx-docker-client
 */


import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.vertx.componentRegister.component.componentType
import static groovy.json.JsonOutput.*

/**
 * Created by grant on 4/10/16.
 */


class DockerTask implements componentType {

    def logger = LoggerFactory.getLogger(this.class.getName())

    DockerClientImpl dockerClient
    Map cfg
    Map meta
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
        logger.debug("\ndockerinfo:${info}")
        image = meta.image
        tag = meta.tag ?: "latest"
        name = meta.name
        this.cfg = cfg
        this.meta = meta
    }


    @Override
    void start(Closure cb) {
        boolean running = false
        logger.info("\n dkr config: \n ${cfg}")
        def resp = new JsonObject()
        try {
            resp = new JsonObject(toJson(dockerClient.inspectContainer(name)))
        } catch (e) {
            logger.info("could not inspect , not running? " + e.getCause())
        }
        logger.debug("resp: ${resp}")
        if (!resp.isEmpty()) {
            def oldId = resp.getJsonObject('content').getString('Id')
            running = resp.getJsonObject('content').getJsonObject('State').getBoolean('Running')
            logger.info("existing container: " +
                    " \n ifExists: " + meta.ifExists +
                    " \n id: " + oldId +
                    " \n running: " + running)
            switch (meta.ifExists) {
                case 'recreate':
                    if(running)
                        dockerClient.stop(oldId)
                    dockerClient.rm(oldId)
                    running = false
                    break
                case 'restart':
                    if(running)
                        dockerClient.stop(oldId)
                    try{
                        dockerClient.startContainer(oldId)
                        running = true
                    }catch(e){
                        logger.error("error on container restart " + e.getMessage())
                        running = false
                    }
                    break
                case 'halt':
                    logger.fatal("${name} ctr exists, and configured to halt if already present")
                    System.exit(-1)
                    break
                case 'leave':
                    logger.info("leaving existing container in place")
                    break
            }
        }
        if (!running) {
            try{
            cb(dockerClient.run(image, cfg, tag, name))}
            catch(e){
                logger.error("error after launching ctr : "+ e.getMessage())
            }
        }
    }

    @Override
    void stop(Closure cb) {

        cb([success: true, result: dockerClient.stop(this.id)])

    }

    @Override
    void backup(Closure cb) {
        /*
        map connections
        volumes.each { it ->
           connections.add(it,"/tmp/${it}"
         }
         with volumes "docker start $volumes backupctr"
         */

    }

    void registrationEvent(Map peerNotification, Closure cb) {
        // use the docer driver to exec a task on the container
    }


}
