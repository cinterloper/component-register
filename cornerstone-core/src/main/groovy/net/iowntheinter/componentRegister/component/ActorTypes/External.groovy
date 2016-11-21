package net.iowntheinter.componentRegister.component.ActorTypes

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import net.iowntheinter.componentRegister.component.Actor

/**
 * Created by grant on 4/13/16.
 * every component should have a start SLATimeout
 * it must check in by that time or will be considered failed
 */
interface External extends Actor {

    void backup(Handler<AsyncResult<JsonObject>> cb) // *see below

}
