package net.iowntheinter.vertx.configurationManager.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import net.iowntheinter.vertx.configurationManager.configurationStore

/**
 * Created by grant on 4/15/16.
 */
class SDLocalMapConfigStore implements configurationStore{

    @Override
    boolean registerComponent(String Name) {
        return false
    }

    @Override
    boolean unregisterComponent(String Name) {
        return false
    }

    @Override
    void setConfig(String componentName, JsonObject cfg, Handler<AsyncResult> cb) {

    }

    @Override
    void removeConfig(String componentName, Handler<AsyncResult> cb) {

    }

    @Override
    void getConfig(String componentName, Handler<AsyncResult> cb) {

    }
}
