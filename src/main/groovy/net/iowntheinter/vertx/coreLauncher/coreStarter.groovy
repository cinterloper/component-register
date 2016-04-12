package net.iowntheinter.vertx.coreLauncher

import groovy.json.JsonOutput
import groovy.util.CliBuilder
import io.vertx.core.json.JsonObject
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Created by grant on 4/11/16.
 */
@Singleton
class coreStarter {
    static String cfgfile;
    static JsonObject jconfig;

    static void halt() {
        def rt = Runtime.getRuntime()
        rt.halt(-1)
    }

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers
                .newArgumentParser("")
                .defaultHelp(true)
                .description("project")
        MutuallyExclusiveGroup mode = parser.addMutuallyExclusiveGroup()
        mode.addArgument("-z","--cluster-zookeeper").help("start with embedded zk").action(storeTrue())
        mode.addArgument("-s", "--stand-alone").help("start standalone").action(storeTrue())
        parser.addArgument("-d", "--dev-mode").help("start in debug/dev mode").action(storeTrue())

        Namespace ns = null
        try {
            ns = parser.parseArgs(args);
            println("parsed args: ${args}")
         } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

/*
core launch config:
 - inital startup verticle
 - any pre-vertx java dep options to load
 - inital launch group

 */


        def env = System.getenv()
        if (!env['CONFIG_PATH']) {
            println("you must declare the CONFIG_PATH enviornment var")
            halt()
        } else {
            try {
                cfgfile = new File(env['CONFIG_PATH']).text
                jconfig = new JsonObject(cfgfile)
            } catch (Exception e) {
                println("caught exception pareing config: ${e}")
                halt()
            }
            println("parsed Json:" + JsonOutput.prettyPrint(jconfig.toString()))
        }


    }

}
