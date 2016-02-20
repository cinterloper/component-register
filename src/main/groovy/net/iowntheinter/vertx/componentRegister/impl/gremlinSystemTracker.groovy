package net.iowntheinter.vertx.componentRegister.impl

import net.iowntheinter.vertx.componentRegister.componentTracker
import org.apache.tinkerpop.gremlin.structure.Edge
import org.apache.tinkerpop.gremlin.structure.Vertex
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph

/**
 * Created by grant on 2/20/16.
 */
class gremlinSystemTracker implements componentTracker {
    TinkerGraph g

    gremlinSystemTracker() {
        g = TinkerGraphFactory
        g = TinkerGraph.open()
    }

    @Override
    void registerComponent(Map ctx, Closure cb) {
        Vertex compV = g.addVertex(null, [name: ctx["name"]])
        ctx["readChannels"].each { k, v ->

        }

        findOrCreateChannel([compV: compV, compCTX: ctx], findOrCreateChannel)

    }

    @Override
    void unregisterComponent(Map ctx, Closure cb) {

    }

    void findOrCreateChannel(Map ctx, String chnl, Closure cb) {
        Vertex chan;
        def exist = g.V("name", chnl)
        if (exist == null || exist.size == 0) {
            chan = g.addVertex(null, chnl)
        } else {
            chan = exist
        }
        cb([compV: ctx['compV'], chanV: chan, ctx: ctx])


    }

    void linkFromChan(Map ctx, Closure cb) {
        Edge ed
        try {
            Vertex from = ctx['compV'] as Vertex
            Vertex to = ctx['chanV'] as Vertex
            ed = from.addEdge('recieves', to)
        }catch(ex){
            cb([result:null,error:ex])
        }
        cb([result:ed,error:null])
    }

    void linkToChan(Map ctx, Closure cb) {
        Edge ed
        try {
            Vertex source = ctx['compV'] as Vertex
            Vertex dst = ctx['chanV'] as Vertex
            ed = dst.addEdge('sends', source)
        }catch(ex){
            cb([result:null,error:ex])
        }
        cb([result:ed,error:null])
    }

}
