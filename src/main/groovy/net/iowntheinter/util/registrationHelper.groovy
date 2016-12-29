package net.iowntheinter.util

import io.vertx.core.Vertx

/**
 * Created by g on 6/27/16.
 */

class registrationHelper {

    registrationHelper() {
    }
    /**
     *
     * Notify the launching system that I am ready to start
     * this is ment to be called from a vertx component launched by cornerstone
     *
     * @param v
     * @param cb
     */
    void notify_start_ready(Vertx v, cb) {
        v.eventBus().publish('_cornerstone:registration',
                v.getOrCreateContext().config().getString('launchId'))
        cb()
    }

    /**
     *
     * Notify the launching system that I am ready to start
     * this is ment to be called from a vertx component launched by cornerstone
     *
     * @param v
     * @param cb
     */
    void notify_start_ready(Vertx v, String id, cb) {
        v.eventBus().publish('_cornerstone:registration', id)
        cb()
    }

    /**
     * add an event to be called when all the launch group components have checked in
     * @param v
     * @param cb
     */
    void on_start_signial(Vertx v, cb) {
        String nodeid = v.sharedData().getLocalMap("_cornerstone:config").get("nodeid")

        v.eventBus().consumer("_cornerstone:start:$nodeid", { sevent ->
            cb(sevent.body())
        })
    }

}
