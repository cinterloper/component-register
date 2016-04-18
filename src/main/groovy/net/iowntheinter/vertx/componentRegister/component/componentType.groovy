package net.iowntheinter.vertx.componentRegister.component

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

/**
 * Created by grant on 4/13/16.
 * every component should have a start SLATimeout
 * it must check in by that time or will be considered failed
 */
interface componentType {
    void start(Handler<AsyncResult> cb) //wakeup cb after its actually started
    void stop( Handler<AsyncResult> cb)
    void registrationEvent(Map peerNotification, Handler<AsyncResult> cb) //notify that a component has become available

}
