package net.iowntheinter.cornerstone.launcherService.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.iowntheinter.coreLauncher.componentLauncher;
import net.iowntheinter.coreLauncher.impl.launcherImpl;
import net.iowntheinter.cornerstone.launcherService.Launcher;

public class LauncherService implements Launcher {
    private final componentLauncher launcher;
    public LauncherService (Vertx vertx){
        launcher = new launcherImpl(vertx);
    }

    @Override
    public void deploy(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {

    }

    @Override
    public void retire(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {

    }

    @Override
    public void lookup(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {

    }
}
