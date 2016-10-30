package net.iowntheinter.componentRegister.cmds

import io.vertx.core.logging.LoggerFactory
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.shell.session.Session
import io.vertx.ext.shell.command.CommandProcess

/**
 * Created by grant on 4/10/16.
 */
class launch {

    static def INTRO = new String("command to launch a component\n\n")
    //you can actually rewrite the reactions through the process handle when questions is processed


    public static Closure COMMAND = { Map cmdctx, cb ->
        CommandProcess pr = cmdctx.p
        Session session = pr.session()
        Map<String, Closure> vh = session.get('validationHandlers')
        ArrayList<String> args = session.get('Args')
        //walk all the validation handlers (defined below) and check their corespondant arguments
        //this should probably be a helper utility
        vh.each { String idx, Closure handler ->
            handler(["argument": args[idx.toInteger()]], { Map ctx ->
                if (!ctx['valid']) {
                    pr.write("invalid input detected in argument ${idx}:${args[idx.toInteger()]}")
                    pr.end(-1)
                }
            })
        }

        cb([v: cmdctx.v, p: cmdctx.p, d: "example result"])

    }
    public static Map VALIDATION = [
            "1": { ctx, cb ->
                ctx['valid'] = true
                cb(ctx)
            },
            "2": { ctx, cb ->
                ctx['valid'] = true
                cb(ctx)
            },
            "3": { ctx, cb ->
                ctx['valid'] = true
                cb(ctx)
            }
    ]

    public static Closure FINISH = { ctx ->
        def logger = LoggerFactory.getLogger("directoryConnection")
        logger.info("running finish closure with context ${ctx}")
        def v = ctx.v
        def p = ctx.p as CommandProcess
        def d = ctx.d
        def eb = v.eventBus();
        eb.send('questions', d)
        println("result:${d}")
        p.end()
    }
}
