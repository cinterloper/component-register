package net.iowntheinter.cornerstone.launcherService.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import net.iowntheinter.coreLauncher.impl.launcherImpl;
import net.iowntheinter.cornerstone.launcherService.Launcher;

public class LauncherService implements Launcher {
    private final launcherImpl launcher;
    public LauncherService (Vertx vertx){
        launcher = new launcherImpl(vertx);
    }

    @Override
    public void deploy(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
       launcher.launchGroup(document, resultHandler);
    }

    @Override
    public void retire(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        LoggerFactory.getLogger(this.getClass().getName()).error("retire: UNIMPLEMENTED"); //will get to this
    }

    @Override
    public void lookup(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        LoggerFactory.getLogger(this.getClass().getName()).error("lookup: UNIMPLEMENTED"); //will get to this
    }
}
