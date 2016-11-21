package net.iowntheinter.coreLauncher.impl

import io.vertx.core.Vertx
import net.iowntheinter.componentRegister.component.ActorTypes.Managed
import net.iowntheinter.coreLauncher.launchStrategy

/**
 * Created by grant on 4/15/16.
 * a startSignalLaunchStrategy is launched immediately
 * it is informed when its dependent services become available via an event bus message
 * and is responsible for only processing  work after it has been made aware
 * that its dependencies are available
 * A docker coordonatedTask must call back to the event bus callback address
 * with its registration id (injected into the Env)
 */
class startSignalLaunchStrategy implements launchStrategy {
    boolean vertxTask

    Map dependentServices
    Vertx vertx
    List dependencies
    String id
    boolean listening
    def startCb
    Closure runCb
    def started
    def task


    startSignalLaunchStrategy(Vertx v, String launchId, Managed task, List dependencies) {
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
        this.task.registrationEvent(peerNotification, cb)

    }


}
