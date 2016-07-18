package net.iowntheinter.serviceRegistry.http

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import net.iowntheinter.kvdn.storage.kv.impl.KvTx
import net.iowntheinter.util.http.routeProvider

/**
 * Created by g on 7/16/16.
 */
class httpRouteProvider implements routeProvider {
    def  r
    @Override
    void addRoutes(Router r, Vertx v) {
        r.post('_CSR/lookup')
    }


    def handleServiceLookup(RoutingContext routingContext) {
        def response = routingContext.response()

        def content;
        try {
            def JsonObject query = routingContext.getBodyAsJson()

            KvTx tx = session.newTx("${sName}:${mName}")
            tx.submit(content, { resPut ->
                if (resPut.error == null) {
                    response.end(mName + ":" + resPut.key)
                } else {
                    response.setStatusCode(501).end(resPut.error)
                }
            })

        } catch (Exception e) {
            response.setStatusCode(501).end(e.getMessage())
        }


    }


}
