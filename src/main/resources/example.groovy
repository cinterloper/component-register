import groovy.json.JsonOutput
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx

/**
 * Created by grant on 3/28/16.
 */

def Vertx  v = vertx as Vertx
LoggerFactory.getLogger(this.getClass().name).debug "inside a loaded verticle with config: \n"
v.eventBus().send('_cornerstone:registration',v.getOrCreateContext().config().launchId)
