package net.iowntheinter.util.configuration.manager.impl

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import net.iowntheinter.util.configuration.store.configStore

/**
 * Created by grant on 4/15/16.
 */
class SDLocalMapConfigStore implements configStore{


    @Override
    void init(Map cfg, Closure cb) {

    }

    @Override
    void setConfig(String componentName, JsonObject cfg, Closure cb) {

    }

    @Override
    void removeConfig(String componentName, Closure cb) {

    }

    @Override
    void getConfig(String componentName, Closure cb) {

    }

    @Override
    void close(Closure cb) {

    }
}
