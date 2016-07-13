package net.iowntheinter.util.http

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

interface routeConfig {

    void addRoutes(Router r, Vertx v)
}