package net.iowntheinter.util

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by g on 7/25/16.
 */

/**
 * pass the component configuration
 * the callback is called with a Map[] to add to the enviornment of the component
 */
interface injector {
    void inject(JsonObject componentcfg, Handler cb)
}
