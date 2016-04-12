package net.iowntheinter.vertx.coreLauncher

/**
 * Created by grant on 4/11/16.
 */
class coreStarter {
  void halt(){
      def rt = Runtime.getRuntime()
      rt.halt(-1)
  }

    public static void main(String[] args){
        def cli = new CliBuilder(usage: 'EXE -[zsd] ')
        cli.z('use zookeeper')
        cli.s('run standalone')
        cli.d('run in develop/debugging mode')
        def opts = cli.parse(args);

    }

}
