package net.iowntheinter.cornerstone.util.http

import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * implement this to attached routes the the built-in KVDN webserver
 * you may still start your own webserver if you dont want to do this, but you must use a diffrent port
 */
interface routeProvider {

    void addRoutes(Router r, Vertx v, cb)
}