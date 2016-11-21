package net.iowntheinter.componentRegister.component.ActorTypes

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject

/**
 * Created by grant on 4/13/16.
 * every component should have a start SLATimeout
 * it must check in by that time or will be considered failed
 */
interface Unmanaged {
    void onHealthCheckEvent(Handler<AsyncResult> h)
    void pollActorInfo(JsonObject msg, Handler<AsyncResult<JsonObject>> h)
}
