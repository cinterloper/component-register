package net.iowntheinter.util

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory

/**
 * Created by g on 12/27/16.
 * this class helps to launch a task after acquiring a cluster wide lock on the name of the task
 * An example use of this is connections to remote messaging systems
 */
class exclusiveTask {
    Vertx vertx
    String name
    Logger logger
    def cb

    exclusiveTask(Vertx vertx, name, cb) {
        this.name = name
        this.vertx = vertx
        this.cb = cb
        this.logger = LoggerFactory.getLogger(this.class.name)
    }

    void exec(alreadyLockedCb) {
        vertx.sharedData().getLock(name, { AsyncResult lockAttempt ->
            if (lockAttempt.succeeded()) {
                logger.debug("this node has acquired the lock for $name")
                cb(lockAttempt.result())
            } else{
                logger.debug("the lock attempt was rejected for $name")
                alreadyLockedCb(lockAttempt.result())
            }
        })
    }

    void execWithRetry(long retryTime) {
        vertx.sharedData().getLock(name, { AsyncResult lockAttempt ->
            if (lockAttempt.succeeded()) {
                logger.debug("this node has acquired the lock for $name")
                cb(lockAttempt.result())
            } else{
                logger.debug("the lock attempt was rejected for $name, will attempte in $retryTime secs")
                vertx.setTimer(retryTime,{
                    execWithRetry(retryTime)
                })

            }
        })
    }


}
