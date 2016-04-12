package net.iowntheinter.vertx.coreLauncher.single

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions

/**
 * Created by grant on 4/11/16.
 */
class singleVertxStarter {
    Vertx vertx

    void start(){
        vertx = Vertx.vertx()
    }
    void start(VertxOptions opts){
        vertx = Vertx.vertx(opts)
    }
}
