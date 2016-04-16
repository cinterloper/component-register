package net.iowntheinter.vertx.componentRegister.componentType

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx

/**
 * Created by grant on 4/15/16.
 * a coordinatedTask is launched immediately
 * it is informed when its dependent services become available via an event bus message
 * and is responsible for only processing  work after it has been made aware
 * that its dependencies are available
 * A docker coordonatedTask must either implement well defined json on standard out
 * or call back to the event bus callback address
 */
class coordinatedTask implements component {

    Map dependentServices
    Vertx vertx
    def task
    def Map dependencies
    def String id
    def boolean listening
    def Handler startCb
    def Closure runCb
    def started
    def boolean vertxTask

    coordinatedTask(component task, Map dependencies) {
        this.task = task
        this.dependencies = dependencies
        listening = false
        started = false
    }

    @Override
    void start(Handler<AsyncResult> cb) {
        this.task.start(cb)
        if (!listening && vertxTask)
            task.listen()
        listening = true
        this.startCb = cb
    }

    @Override
    void stop(Handler<AsyncResult> cb) {
        this.task.stop(cb)
    }

    @Override
    void registrationEvent(Map peerNotification, Handler<AsyncResult> cb) {
        this.task.registrationEvent( notification, cb)

    }


}
