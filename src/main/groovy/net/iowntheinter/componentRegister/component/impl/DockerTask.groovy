package net.iowntheinter.componentRegister.component.impl

/*
for this to be really async
will need to switch to https://github.com/shekhargulati/rx-docker-client
 */
import de.gesellix.docker.client.DockerClientImpl
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.componentRegister.component.componentType
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
    void start(cb) {
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
                    if (running)
                        dockerClient.stop(oldId)
                    dockerClient.rm(oldId)
                    running = false
                    break
                case 'restart':
                    if (running)
                        dockerClient.stop(oldId)
                    try {
                        dockerClient.startContainer(oldId)
                        running = true
                    } catch (e) {
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
            def error = '', success
            try {
                dockerClient.run(image, cfg, tag, name)
                success = true
            }
            catch (e) {
                logger.error("error after launching ctr : " + e.getMessage())
                error = e.getMessage()
                success = false
            }
            cb([success: success, error: error])// if not error its assumed to be lanunched
            //should set a timer and wait for dail back
        } else {
            cb([success: true, error: "running"])
        }
    }

    @Override
    void stop(cb) {

        cb([success: true, result: dockerClient.stop(this.id)])

    }

    @Override
    void backup(cb) {
        /*
        map connections
        volumes.each { it ->
           connections.add(it,"/tmp/${it}"
         }
         with volumes "docker start $volumes backupctr"
         */

    }

    @Override
    String getId() {
        return this.id
    }

    void registrationEvent(Map peerNotification, Closure cb) {
        // use the docer driver to exec a task on the container
    }


}
