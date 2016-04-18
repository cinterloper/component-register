package net.iowntheinter.vertx.coreLauncher

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.Handler

/**
 * Created by grant on 4/13/16.
 */
interface coreCTX {

    void start(VertxOptions opts, Closure<Map> cb)
}
