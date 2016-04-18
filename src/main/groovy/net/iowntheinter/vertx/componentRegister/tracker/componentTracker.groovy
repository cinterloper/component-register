package net.iowntheinter.vertx.componentRegister.tracker
interface componentTracker {
    /* ctx to register a verticle:
     def mailExample = [
             readChannels : [
                     mail : [reply: false, broadcast: false],
                     start: [reply: false, broadcast: false, administrative: true]
             ],
             writeChannels: [
                     store: [reply: true, broadcast: false],
                     alert: [reply: false, broadcast: true]
             ],
             capabilities: ["smtp"]
             dependencies: ["storage"]
     ]

     ctx to unregister:
     [deploymentid:id]
     */

    void registerComponent(Map ctx, Closure cb)

    void unregisterComponent(Map ctx, Closure cb)

}