package net.iowntheinter.cornerstone

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.util.configLoader
import net.iowntheinter.util.errorHandler
import net.iowntheinter.util.registrationHelper
import net.iowntheinter.util.resourceLoader

/**
 * Created by g on 11/21/16.
 */
abstract class CornerstoneVerticle extends AbstractVerticle{
    protected Logger logger
    protected EventBus eb
    protected JsonObject config
    def resLoader
    def cfgLoader
    def regHelper
    def errors

    @Override
    void start() throws Exception {
        this.config = vertx.getOrCreateContext().config()
        this.logger = LoggerFactory.getLogger(this.class.getName())
        this.eb = vertx.eventBus()
        this.resLoader = new resourceLoader()
        this.errors = new errorHandler(logger,this.vertx)
        this.regHelper = new registrationHelper(this.vertx)
        this.cfgLoader = new configLoader(this.vertx)
        this.cstart()
    }
    @Override
    void stop() throws Exception {
        this.cstop()
    }

    abstract void cstart()
    abstract void cstop()

}
