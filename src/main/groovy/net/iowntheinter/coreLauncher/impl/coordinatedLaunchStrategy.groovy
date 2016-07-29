package net.iowntheinter.coreLauncher.impl

import io.vertx.core.Handler
import io.vertx.core.Vertx
import net.iowntheinter.componentRegister.component.componentType
import net.iowntheinter.coreLauncher.launchStrategy

/**
 * Created by grant on 4/15/16.
 * a coordinatedLaunchStrategy is launched immediately
 * it is informed when its dependent services become available via an event bus message
 * and is responsible for only processing  work after it has been made aware
 * that its dependencies are available
 * A docker coordonatedTask must call back to the event bus callback address
 * with its registration id (injected into the Env)
 */
class coordinatedLaunchStrategy implements launchStrategy {

    Map dependentServices
    Vertx vertx
    def task
    def List dependencies
    def String id
    def boolean listening
    def Handler startCb
    def Closure runCb
    def started
    def boolean vertxTask


    coordinatedLaunchStrategy(Vertx v , String launchId, componentType task, List dependencies) {
        this.vertx = v
        this.task = task
        this.dependencies = dependencies
        listening = false
        started = false
    }

    @Override
    void start(Closure cb) {
        this.task.start(cb)
        if (!listening && vertxTask)
            task.listen()
        listening = true
        this.startCb = cb
    }

    @Override
    void stop(Closure cb) {
        this.task.stop(cb)
    }

    @Override
    void registrationEvent(Map peerNotification, Closure cb) {
        this.task.registrationEvent(notification, cb)

    }


}
