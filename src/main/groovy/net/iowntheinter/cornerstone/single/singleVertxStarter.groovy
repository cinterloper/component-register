package net.iowntheinter.cornerstone.single

import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions

/**
 * Created by grant on 4/11/16.
 */
class singleVertxStarter {
    Context ctx


    void start(VertxOptions opts, Closure<Map> cb) {

        cb([success: true, vertx: Vertx.vertx(opts)])
    }
}
