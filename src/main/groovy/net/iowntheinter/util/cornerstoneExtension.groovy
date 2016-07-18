package net.iowntheinter.util

import io.vertx.core.json.JsonObject

/**
 * Created by g on 7/16/16.
 */
interface cornerstoneExtension {

    Map provides()
    Map depends()
    String name()
    JsonObject metadata()
}
