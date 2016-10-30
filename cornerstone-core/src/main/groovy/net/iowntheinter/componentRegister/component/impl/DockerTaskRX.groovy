package net.iowntheinter.componentRegister.component.impl

import com.shekhargulati.reactivex.docker.client.RxDockerClient
import com.shekhargulati.reactivex.docker.client.representations.DockerContainerResponse
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.rx.java.RxHelper
import net.iowntheinter.componentRegister.component.componentType
import rx.Observable

/**
 * Created by g on 7/9/16.
 */
class DockerTaskRX implements componentType {
    final UUID requestID
    String id, name
    RxDockerClient client
    JsonObject container
    Logger logger

    DockerTaskRX(String name, JsonObject container, RxDockerClient client) {
        this.client = client
        this.container = container
        this.name = name
        this.requestID = UUID.randomUUID()
        logger = LoggerFactory.getLogger(this.class.getName() + ": $requestID : $name ")
    }

    DockerTaskRX(String name, JsonObject container) {
        DockerTaskRX(name, container, RxDockerClient.fromDefaultEnv())
    }

    @Override
    void start(Handler<AsyncResult<JsonObject>> cb) {
        Observable<DockerContainerResponse> r = client.createContainerObs(container.toString(), name)
        r.flatMap({ res ->
            client.startContainerObs(res.getId())
                    .doOnError({ error ->
                logger.error(error)
                cb.handle(Future.failedFuture(error))
            })
                    .subscribe({ sres ->
                logger.info(sres.toString())
                cb.handle(Future.succeededFuture(new JsonObject().put("result",sres.toString())))
            })
        })


    }

    @Override
    void stop(Handler<AsyncResult<JsonObject>> cb) {
        Observable<DockerContainerResponse> r = client.stopContainerObs(this.id, 0)
        r.doOnError({ error ->
            logger.error(error)
            cb.handle(Future.failedFuture(error))
        }).subscribe({ result ->
            cb.handle(Future.succeededFuture(new JsonObject().put("result",result.toString())))
        })
    }

    @Override
    void backup(Handler<AsyncResult<JsonObject>> cb) {
        throw new Exception("unimplemented")
    }

    @Override
    String getId() {
        return this.id
    }
}
