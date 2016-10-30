package net.iowntheinter.cornerstone.launcherService;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;
import net.iowntheinter.cornerstone.launcherService.impl.LauncherService;

/**
 * Created by grant on 3/26/16.
 */
@ProxyGen
@VertxGen
public interface Launcher {
    // A couple of factory methods to create an instance and a proxy
    static Launcher create(Vertx vertx) {
        return new LauncherService(vertx);
    }

    static Launcher createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(Launcher.class, vertx, address);
        // Alternatively, you can create the proxy directly using:
        // return new ProcessorServiceVertxEBProxy(vertx, address);
        // The name of the class to instantiate is the service interface + `VertxEBProxy`.
        // This class is generated during the compilation
    }

    void deploy(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void retire(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

    void lookup(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);

}
