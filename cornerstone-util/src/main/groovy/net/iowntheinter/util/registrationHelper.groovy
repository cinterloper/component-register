package net.iowntheinter.util

import io.vertx.core.Vertx

/**
 * Created by g on 6/27/16.
 */

class registrationHelper {

    /**
     *
     * Notify the launching system that I am ready to start
     * this is ment to be called from a vertx component launched by cornerstone
     *
     * @param v
     * @param cb
     */
    void notify_start_ready(Vertx v, cb) {
        v.eventBus().send('_cornerstone:registration',
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
        v.eventBus().send('_cornerstone:registration', id)
        cb()
    }

    /**
     * add an event to be called when all the launch group components have checked in
     * @param v
     * @param cb
     */
    void on_start_signial(Vertx v, cb) {
        v.eventBus().consumer('_cornerstone:start', { sevent ->
            cb(sevent.body())
        })
    }

    void raise_fatal_error(Vertx v, Exception e) {
        v.eventBus().send("_CORNERSTONE_FATAL_ERRORS", [error: e.getMessage(),
                                                        id   : v.getOrCreateContext().config().getString('launchId')])
    }

}
