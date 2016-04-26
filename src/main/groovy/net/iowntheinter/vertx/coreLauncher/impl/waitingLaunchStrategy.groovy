package net.iowntheinter.vertx.coreLauncher.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import net.iowntheinter.vertx.componentRegister.component.componentType

/**
 * Created by grant on 4/15/16.
 * a dependent task is launched after the starting process is made aware
 * that all the task's dependencies have become available
 *
 * dependent tasks need not include internal startup logic, they may assume
 * the things they are looking for are already present at startup time
 *
 * a docker dependent task has an assumed set of provided services,
 * an assumed time to startup
 * #futureimpl: optionaly a closure can be provided to check the service has started after
 * the timeout
 */
class waitingLaunchStrategy implements componentType {
    def task
    def List dependencies
    def Map depset
    def String id
    def boolean listening
    def Closure startCb
    def Closure runCb
    def started
    def boolean vertxTask


    waitingLaunchStrategy(componentType task, List dependencies) {
        this.task = task
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
        this.startCb = cb
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
