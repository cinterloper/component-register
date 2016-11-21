package net.iowntheinter.coreLauncher.impl

import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.componentRegister.component.ActorTypes.Managed
import net.iowntheinter.coreLauncher.launchStrategy
import net.iowntheinter.util.registrationHelper

/**
 * Created by grant on 4/15/16.
 *

 */
class capabilitiesLaunchStrategy implements launchStrategy {
    def task
    def List dependencies
    def Map depset
    def String id
    def boolean listening
    def Closure startCb
    def Closure runCb
    def started
    def boolean vertxTask
    Vertx vertx

    capabilitiesLaunchStrategy(Vertx v, String launchId, Managed task, List dependencies) {
        this.vertx = v
        this.id = launchId
        this.task = task as Managed
        this.dependencies = dependencies
        this.depset = [:]
        this.dependencies.each { dep ->
            depset[dep] = false
        }
        listening = false
        started = false
    }

    @Override
    void start(Closure cb) {
        def togo = this.depset.size()

        this.depset.each { dep, readyStatus ->
            if (readyStatus == true)
                togo--
        }
        if (togo == 0)
            this.task.start(cb)
        if (!listening && vertxTask)
            task.listen()
        listening = true
        LoggerFactory.getLogger(this.class.getName()).info("sending registration event: ${this.id}")
        new registrationHelper().notify_start_ready(vertx,this.id,cb)
    }

    @Override
    void stop(Closure cb) {
        this.task.stop(cb)
    }

    @Override
    void registrationEvent(Map peerNotification, Closure cb) {
        this.dependencies[peerNotification.name] = peerNotification.state
        try {
            start(cb)
        } catch (e) {
            e.printStackTrace() // unexpected
        }
    }

    void callWhenRunning(Closure cb) {
        this.task.runCb = cb
    }

}
