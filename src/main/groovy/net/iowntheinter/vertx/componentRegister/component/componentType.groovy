package net.iowntheinter.vertx.componentRegister.component

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

/**
 * Created by grant on 4/13/16.
 * every component should have a start SLATimeout
 * it must check in by that time or will be considered failed
 */
interface componentType {
    void start(Closure cb) //wakeup cb after its actually started
    void stop( Closure cb)
    void registrationEvent(Map peerNotification, Closure cb) //notify that a component has become available

}
