package net.iowntheinter.util.http.example

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.PermittedOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import net.iowntheinter.util.http.routeProvider

/**
 * Created by g on 7/12/16.
 */
class helloWorldRouteProvider implements routeProvider  {
    @Override
    void addRoutes(Router router, Vertx v) {
        def sjsh = SockJSHandler.create(v)
	 //apply security here, this is wide open
        def options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions()
                .setAddressRegex(".*"))
                .addInboundPermitted(new PermittedOptions()
                .setAddressRegex(".*"))
        sjsh.bridge(options)
        router.route("/eb/*").handler(sjsh)

        router.get('/hello').blockingHandler({ request ->
            request.response().end("hello!")
        })
    }
}
