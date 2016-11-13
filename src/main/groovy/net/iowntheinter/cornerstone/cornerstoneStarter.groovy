package net.iowntheinter.cornerstone

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger as LBLogger
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.cli.Argument
import io.vertx.core.cli.CLI
import io.vertx.core.cli.CommandLine
import io.vertx.core.cli.Option
import io.vertx.core.cli.impl.DefaultCommandLine
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.SLF4JLogDelegateFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import net.iowntheinter.cornerstone.cluster.clusterVertxStarter
import net.iowntheinter.cornerstone.single.singleVertxStarter


import io.vertx.core.logging.LoggerFactory
import net.iowntheinter.kvdn.kvserver
import net.iowntheinter.util.displayOutput
import net.iowntheinter.util.http.routeProvider

/**
 * Created by grant on 4/11/16.
 * Parse order:
 *  1. project.json (baked project config (defines any additional cli args))
 *  2. cli arguments (startup options)
 *  3. cli json (vertx config)
 *  4. environment variables (if declared in project.json or cli json )
 *
 * Notes:
 * execution of the startup logic is for the most part synchronous
 * once Vert.x is started, all real application code should run inside it
 * and this should all be strictly async code, except maybe worker verticles
 *
 */
class cornerstoneStarter {
    static String project_name = ""
    static JsonObject project_config
    static URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
    static Logger logger = LoggerFactory.getLogger(this.class.getName())
    private final UUID incarnation


    cornerstoneStarter() {
        incarnation = UUID.randomUUID()
    }

    static void halt() {
        def rt = Runtime.getRuntime()
        rt.halt(-1)
    }


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

    public static void main(String[] args) {
        (org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as LBLogger).
                setLevel(Level.WARN)
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        System.setProperty('vertx.logger-delegate-factory-class-name', 'io.vertx.core.logging.SLF4JLogDelegateFactory');
        project_config = parseProjectConfig()
        project_name = project_config.getString('name')

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

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);


        CommandLine commandLine = DefaultCommandLine.create(cli)
        try {
            commandLine = cli.parse(Arrays.asList(args)) as CommandLine;
            if (!commandLine.isValid() || commandLine.isAskingForHelp()) {
                System.out.print(builder.toString());
                System.exit(1);

            }
        } catch (Exception e) {
            logger.error("could not parse cli: " + e.getMessage())
            System.exit(1);
        }

        if (commandLine.isFlagEnabled("DumpConfig")) {
            println(project_config.encodePrettily())
            System.exit(0)
        }
        if (commandLine.isFlagEnabled("debug")) {

            if (commandLine.isFlagEnabled("debug")) {
                (org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as LBLogger).
                        setLevel(Level.toLevel(commandLine.getOptionValue("loglevel") as String))
            }

        }
        logger.debug("parsed args: ${commandLine.allArguments()}")
        logger.debug("loglevel option: ${commandLine.getOptionValue("loglevel")}")
        logger.debug("debug flag: ${commandLine.isFlagEnabled("debug")}")
        logger.debug("cluster flag: ${commandLine.isFlagEnabled("cluster")}")
        logger.debug("loaded project def: ${project_name}")
        String config_override_path = commandLine.getArgumentValue("config") ?: ""
        if (!config_override_path.isEmpty()) {
            project_config = parseProjectConfig(config_override_path)
            logger.debug("config override path ${config_override_path}")
        }

        def env = System.getenv()

        project_config.getJsonArray("env_vars").each { v ->
            def var = v as String
            if (!env[var]) {
                logger.error("you must declare the ${var} enviornment var")
                halt()
            }
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
                vertx.deployVerticle('net.iowntheinter.coreLauncher.impl.coreLauncher', opts)












            }
        }

        logger.debug("starting first vertx")
        def startmessage = ["header": "cornerstone init",
                            "cols"  : ["COMPONENT", "STATUS", "MESSAGE"],
                            "data"  : [
                                    'cornerstoneStarter': ["running", "ok"],
                            ]
        ]


        new displayOutput(project_config.getString("output_type") ?: "json").display(startmessage)


        if (commandLine.isFlagEnabled("cluster"))
            new clusterVertxStarter().start(new VertxOptions(), afterVXStart)
        else
            new singleVertxStarter().start(new VertxOptions(), afterVXStart)

    }
}
