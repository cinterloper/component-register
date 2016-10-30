package net.iowntheinter.util

import io.vertx.core.logging.Logger

/**
 * Created by g on 8/16/16.
 */
class errorHandler {

    static void handleError(Exception e, Logger l) {
        l.error(e)
        e.printStackTrace()
    }
}
