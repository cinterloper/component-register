
/**
 * Created by grant on 3/28/16.
 */
import io.vertx.groovy.core.Vertx
v = vertx as Vertx
println("inside example.groovy" + v.getOrCreateContext().config())