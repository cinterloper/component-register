package net.iowntheinter.cornerstone.test.components

import io.vertx.core.AbstractVerticle
import net.iowntheinter.cornerstone.util.registrationHelper

/**
 * Created by g on 10/15/16.
 */
class testVerticle extends AbstractVerticle {

    testVerticle(){
        def r = new registrationHelper()
        r.on_start_signial(vertx,{
            println("started")
        })
        r.notify_start_ready(vertx,{})
    }


}
