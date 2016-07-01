package net.iowntheinter.util

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory


/**
 * Created by g on 6/30/16.
 */
class ebReflector extends AbstractVerticle {
    private HashMap reflectors = new HashMap()
    private Logger log
    private EventBus eb

    @Override
    void start() throws Exception {
        eb = vertx.eventBus()
        log = LoggerFactory.getLogger(this.class.getName())
        JsonObject r = vertx.getOrCreateContext().config().getJsonObject("reflectors")
        r.fieldNames().each { key ->
            createReflector(key, r.getString(key), {

                log.debug("reflector setup :${key}")

            })
        }
        vertx.eventBus().send('_cornerstone:registration',vertx.getOrCreateContext().config().getString('launchId'))


    }


    public void removeReflector(String RID, cb) {
        try {
            MessageConsumer r = reflectors[RID] as MessageConsumer
            r.unregister()
        }
        catch (e) {
            log.error("error ${e}")
        }
        cb()
    }

    public void createReflector(String srcaddr, String dstaddr, cb) {
        def ret = true
        MessageConsumer subscriptionChannel = eb.consumer(srcaddr)
        subscriptionChannel.handler({ message ->
            def String jreq = ""
            try {
                jreq = message.body().toString()
                eb.publish(dstaddr, jreq)
            } catch (e) {
                log.error("error ${e}")
            }
        })
        //store a refrence to the channel so it can be removed, return the id
        def RID = UUID.randomUUID().toString()
        reflectors[RID] = subscriptionChannel
        cb(RID)
    }
}