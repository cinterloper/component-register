package net.iowntheinter.cornerstone.impl

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.cli.Argument
import io.vertx.core.cli.CLI
import io.vertx.core.cli.Option
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.spi.launcher.DefaultCommandFactory


/**
 * Created by g on 11/21/16.
 */
class cornerstoneStarterFactory extends DefaultCommandFactory {
    static URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
    private Logger logger = LoggerFactory.getLogger(this.class.getName())
    private JsonObject config
    private String project_name

    cornerstoneStarterFactory(Class clazz) {
        super(clazz)
    }

    @Override
    public CLI define() {
        CLI cli = CLI.create(project_name as String)
                .setSummary(project_name as String)
                .addOption(new Option()
                .setLongName("cluster")
                .setShortName("X")
                .setDescription("enables clustering")
                .setFlag(true))
                .addOption(new Option()
                .setLongName("DumpConfig")
                .setShortName("C")
                .setDescription("output config then exit")
                .setFlag(true))
                .addOption(new Option()
                .setLongName("debug")
                .setShortName("d")
                .setDescription("enables debugging")
                .setFlag(true))
                .addOption(new Option()
                .setLongName("loglevel")
                .setShortName("l")
                .setDescription("log level TRACE|INFO|WARN|DEBUG"))
                .addArgument(new Argument()
                .setIndex(0)
                .setDescription("override configuration with this json file")
                .setArgName("config").setRequired(false))
                .addOption(new Option()
                .setLongName("help").setShortName("h").setFlag(true).setHelp(true))

        return cli;
    }

//____________utility__________________

    static JsonObject parseProjectConfig() {
        def config
        try {
            config = classloader.
                    getResourceAsStream('project.json').getText()
        }
        catch (NullPointerException e) {
            logger.trace(e)
            return (parseDefaultProjectConfig())
        }
        return (new JsonObject(config))
    }

    static JsonObject parseProjectConfig(String path) {
        def config
        try {
            config = new JsonObject(new File(path).text)
        }
        catch (Exception e) {
            logger.trace(e)
            return (parseDefaultProjectConfig())
        }
        return (config ?: new JsonObject())
    }

    static JsonObject parseDefaultProjectConfig() {
        String defaultCfg = classloader.
                getResourceAsStream('example-project.json').getText()
        return (new JsonObject(defaultCfg))
    }

    void startClusterVertx(VertxOptions opts, Closure<Map> cb) {

        Vertx.clusteredVertx(opts, { res ->
            if (res.succeeded()) {
                vertx = res.result();
                cb([result: vertx,error:null])
                logger.info("We have a clustered vertx ${vertx.getOrCreateContext()}")
            } else {
                logger.error("there was a failure starting clustered vertx ")
                res.cause().printStackTrace()
                System.exit(-1)
                // failed!
            }
        })
    }


    Closure afterVXStart = { Map res ->
        Vertx vertx
        logger.debug(res)
        if (!res.success) {
            logger.error("could not start vertx")
            halt()
        } else {
            vertx = res.vertx as Vertx
            project_config.put("_cornerstone_incarnation", vertx.getOrCreateContext().deploymentID())
            logger.debug("cornerstone incarnation: " + project_config.getString('_cornserstone_incarnation'))
            def opts = new DeploymentOptions([config: project_config.getMap()])

            vertx.deployVerticle('net.iowntheinter.cornerstone.impl.cornerstoneLauncher', opts,{ deployResult ->
                if(!deployResult.succeeded()){
                    logger.fatal(
                            "failed to deploy the launcher, \n" +
                                    "halting because no application components can be deployed")
                    halt(-1,deployResult.cause())
                }
            })


        }
    }






}
