package net.iowntheinter.vertx.coreLauncher

import groovy.json.JsonOutput
import groovy.util.CliBuilder
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import net.iowntheinter.vertx.coreLauncher.cluster.zookeeperVertxStarter
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import io.vertx.core.logging.LoggerFactory

/**
 * Created by grant on 4/11/16.
 */
@Singleton
class coreStarter {
    static String cfgfile
    static String project_name
    static JsonObject launch_config
    static JsonObject project_config
    static URLClassLoader classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())

    static Logger lgr = LoggerFactory.getLogger(this.class.getName())

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

        ArgumentParser parser = ArgumentParsers
                .newArgumentParser(project_name)
                .defaultHelp(true)
                .description(project_name)
        MutuallyExclusiveGroup mode = parser.addMutuallyExclusiveGroup()
        mode.addArgument("-z", "--cluster-zookeeper").help("start with embedded zk").action(storeTrue())
        mode.addArgument("-s", "--stand-alone").help("start standalone").action(storeTrue())
        parser.addArgument("-d", "--dev-mode").help("start in debug/dev mode").action(storeTrue())

        Namespace ns = null
        try {
            ns = parser.parseArgs(args);
            println("parsed args: ${args}")
        } catch (ArgumentParserException e) {
            println("thown message: ")
            parser.handleError(e);
            System.exit(1);
        }
        if (ns.getAttrs()["dev_mode"]) {
            lgr.info("loaded project def: ${project_name}")
        }


        def env = System.getenv()

        project_config.getJsonArray("env_vars").each { v ->
            def var = v as String
            if (!env[var]) {
                println("you must declare the ${var} enviornment var")
                halt()
            }
        }
        if (ns.getAttrs()["cluster_zookeeper"]){
            def zks = new zookeeperVertxStarter().start(new VertxOptions())
        }

    }


}
