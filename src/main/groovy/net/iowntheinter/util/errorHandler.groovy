package net.iowntheinter.util

import io.vertx.core.Vertx
import io.vertx.core.logging.Logger

/**
 * Created by g on 8/16/16.
 */
class errorHandler {
    private final Logger logger
    private final Vertx vertx
    errorHandler(Logger logger, Vertx vertx){
        this.logger = logger
        this.vertx = vertx
    }
    static void handleError(Exception e, Logger l){
        l.error(e)
        e.printStackTrace()
    }
}
