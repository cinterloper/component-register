package example

import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx as GVertx
import net.iowntheinter.util.registrationHelper

/**
 * Created by grant on 3/23/16.
 */
Vertx v = (vertx as GVertx).getDelegate()
def rh = new registrationHelper()
rh.on_start_signial(v, {

    LoggerFactory.getLogger(this.class.getName()).warn("inside example 1")

})

rh.notify_start_ready(v, {})
