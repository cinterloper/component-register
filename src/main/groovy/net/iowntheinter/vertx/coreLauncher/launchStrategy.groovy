package net.iowntheinter.vertx.coreLauncher

/**
 * Created by grant on 4/17/16.
 */
interface launchStrategy {
    void start(Closure cb) //wakeup cb after its actually started
    void stop(Closure cb)
    void registrationEvent(Map peerNotification, Closure cb) //notify that a component has become available

}
