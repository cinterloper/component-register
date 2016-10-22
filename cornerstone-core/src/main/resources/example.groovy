import groovy.json.JsonOutput
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx as GVertx
import io.vertx.core.Vertx
import net.iowntheinter.util.registrationHelper

/**
 * Created by grant on 3/28/16.
 */

Vertx v = (vertx as GVertx).getDelegate() as Vertx
def rh = new registrationHelper()
rh.on_start_signial(v, {

    LoggerFactory.getLogger(this.class.getName()).warn("inside example")

})

rh.notify_start_ready(v, {})
