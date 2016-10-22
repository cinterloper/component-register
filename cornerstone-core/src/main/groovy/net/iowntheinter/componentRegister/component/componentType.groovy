package net.iowntheinter.componentRegister.component

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

/**
 * Created by grant on 4/13/16.
 * every component should have a start SLATimeout
 * it must check in by that time or will be considered failed
 */
interface componentType {
    void start(Handler<AsyncResult> cb) //wakeup cb after its actually started
    void stop(Handler<AsyncResult> cb)
    String getId()
    void backup(Handler<AsyncResult> cb) // *see below
/*
for a docker task, backup should
  provide some notification to the container
  attach another container on the same server to the same volumes
   - serialize them to tar volumes
   - return a map of their names and the location of the tar archives

for a verticle
this should
 - send an event that
  - notifies the verticle to persist or finish its transactions
    - the verticle shoudl reply when this is done

 */
}
