package net.iowntheinter.componentRegister.component.impl

import net.iowntheinter.componentRegister.component.componentType

/**
 * Created by g on 7/9/16.
 */
class DockerTaskRX implements componentType {
    String id

    @Override
    void start(Closure cb) {

    }

    @Override
    void stop(Closure cb) {

    }

    @Override
    void backup(Closure cb) {

    }
    @Override
    String getId(){
        return this.id
    }
}
