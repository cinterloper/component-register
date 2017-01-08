package net.iowntheinter.util

import io.vertx.core.Vertx

/**
 * Created by g on 6/27/16.
 */

class registrationHelper {

    Vertx vertx
    registrationHelper(vertx = null){
        if (vertx != null){
            this.vertx=vertx
        }
    }

    /**
     *
     * Notify the launching system that I am ready to start
     * this is ment to be called from a vertx component launched by cornerstone
     *
     * @param v
     * @param cb
     */
    void notify_start_ready(Vertx v = this.vertx, cb) {
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
    void notify_start_ready(Vertx v = this.vertx, String id, cb) {
        v.eventBus().publish('_cornerstone:registration', id)
        cb()
    }

    /**
     * add an event to be called when all the launch group components have checked in
     * @param v
     * @param cb
     */
    void on_start_signial(Vertx v = this.vertx, cb) {
        String nodeid = v.sharedData().getLocalMap("_cornerstone:config").get("nodeid")

        v.eventBus().consumer("_cornerstone:start:$nodeid", { sevent ->
            cb(sevent.body())
        })
    }


    /**
     * add an event to be called when all the launch group components have checked in
     * @param v
     * @param cb
     */
    void on_group_start_signial(Vertx v = this.vertx , cb) {
        def gid = v.getOrCreateContext().config().getString('groupId')
        v.eventBus().consumer(channelref.START + "_$gid", { startevent ->
            cb(startevent.body())
        })
    }
    /**
     * add an event to be called when all the launch group components have checked in
     * @param v
     * @param cb
     */
    void on_individual_start_signial(Vertx v = this.vertx , cb) {
        def id = v.getOrCreateContext().config().getString('launchId')
        v.eventBus().consumer(channelref.START + "_$id", { startevent ->
            cb(startevent.body())
        })
    }

    /**
     * add an event to be called when a shutdown request is recieved
     * @param v
     * @param cb
     */
    void on_stop_signial(Vertx v = this.vertx , cb) {
        v.eventBus().consumer(channelref.STOP, { stopevent ->
            cb(stopevent.body())
        })
    }

}
