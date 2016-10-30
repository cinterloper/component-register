package net.iowntheinter.cornerstone.single

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions

/**
 * Created by grant on 4/11/16.
 */
class singleVertxStarter {
    Vertx vertx
    void start(VertxOptions opts, Closure<Map> cb) {
        try {
            vertx = Vertx.vertx(opts)
        }catch (e){
            e.printStackTrace()
            System.exit(-1)
        }
        cb([success: true, vertx: vertx])
    }
}
