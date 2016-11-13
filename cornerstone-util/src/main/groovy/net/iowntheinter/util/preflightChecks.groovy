package net.iowntheinter.util

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx

/**
 * Created by g on 9/19/16.
 */
interface preflightChecks {
    public void runChecks(Handler<AsyncResult<Boolean>> handler, Vertx vertx)
}
