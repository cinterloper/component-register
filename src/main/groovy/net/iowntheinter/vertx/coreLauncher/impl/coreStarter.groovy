package net.iowntheinter.vertx.coreLauncher.impl

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.cli.Argument
import io.vertx.core.cli.CLI
import io.vertx.core.cli.CommandLine
import io.vertx.core.cli.Option
import io.vertx.core.cli.impl.DefaultCommandLine
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import net.iowntheinter.vertx.coreLauncher.impl.cluster.clusterVertxStarter
import net.iowntheinter.vertx.coreLauncher.impl.single.singleVertxStarter
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.LoggerConfig

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import io.vertx.core.logging.LoggerFactory

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
@Singleton
class coreStarter {
    static String cfgfile
    static String project_name =""
    static JsonObject launch_config
    static JsonObject project_config
    static URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
    static Logger logger = LoggerFactory.getLogger(this.class.getName())
    static boolean debug = false


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
            return (parseDefaultProjectConfig())
        }
        return (new JsonObject(config))
    }

    static JsonObject parseDefaultProjectConfig() {
        String defaultCfg = classloader.
                getResourceAsStream('example-project.json').getText()
        return (new JsonObject(defaultCfg))
    }

    public static void main(String[] args) {

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
                .setDescription("The runtime configuration")
                .setArgName("config"))
                .addOption(new Option()
                .setLongName("help").setShortName("h").setFlag(true).setHelp(true))


        StringBuilder builder = new StringBuilder();
        cli.usage(builder);

        println("PROPERTY: ${System.getProperty('vertx.logger-delegate-factory-class-name')}")
        CommandLine commandLine = DefaultCommandLine.create(cli)
        try {
            commandLine = cli.parse(Arrays.asList(args));
            logger.debug("parsed args: ${commandLine.allArguments()}")
            if (!commandLine.isValid() && commandLine.isAskingForHelp()) {
                System.out.print(builder.toString());
                System.exit(1);

            }
        } catch (ArgumentParserException e) {
            logger.error("could not parse cli: " + e.getMessage())
            System.exit(1);
        }

// The parsing does not fail and let you do:

        if (commandLine.getArgumentValue("debug")) {
            logger.debug("loaded project def: ${project_name}")
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            if (commandLine.getArgumentValue("loglevel")) {
                loggerConfig.setLevel(Level.toLevel(commandLine.getArgumentValue("loglevel") as String));
            } else {
                loggerConfig.setLevel(Level.DEBUG);
            }
            ctx.updateLoggers();

        }

        def env = System.getenv()

        project_config.getJsonArray("env_vars").each { v ->
            def var = v as String
            if (!env[var]) {
                logger.debug("you must declare the ${var} enviornment var")
                halt()
            }
        }

        Closure afterVXStart = { Map res ->
            Vertx vx
            logger.debug(res)
            if (!res.success) {
                logger.error("could not start vertx")
                halt()
            } else {
                vx = res.vertx as Vertx
                project_config.put("clustered", commandLine.getOptionValue("cluster"))
                def opts = new DeploymentOptions([config: project_config.getMap()])
                vx.deployVerticle('net.iowntheinter.vertx.componentRegister.impl.coreLauncher', opts)
            }
        }

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4jLogDelegateFactory")
        logger.debug("starting first vertx")

        new singleVertxStarter().start(new VertxOptions(), afterVXStart)

    }
}
