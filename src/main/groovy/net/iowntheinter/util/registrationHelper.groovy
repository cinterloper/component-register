package net.iowntheinter.util

import io.vertx.core.Vertx

/**
 * Created by g on 6/27/16.
 */
class registrationHelper {

    void notify_start_ready(Vertx v, cb){
        v.eventBus().send('_cornerstone:registration',
                v.getOrCreateContext().config().getString('launchId'))
    }
    void on_start_signial(Vertx v,cb){
        v.eventBus().consumer('_cornerstone:start',{ sevent ->
            cb()
        })
    }

}
