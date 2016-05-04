/**
 * Created by grant on 3/28/16.
 */
import io.vertx.groovy.core.Vertx
import io.vertx.core.shareddata.LocalMap

v = vertx as Vertx
println("inside example.groovy")


println(" comp: ")
LocalMap comp = v.sharedData().getLocalMap("cornerstone_components").getDelegate() as LocalMap

comp.keySet().each{ key ->
  println("key: ${key} value: ${comp.get(key)}")
}
println(" depl: " )

LocalMap depl = v.sharedData().getLocalMap("cornerstone_deployments").getDelegate() as LocalMap

depl.keySet().each{ key ->
    println("key: ${key} value: ${comp.get(key)}")
}