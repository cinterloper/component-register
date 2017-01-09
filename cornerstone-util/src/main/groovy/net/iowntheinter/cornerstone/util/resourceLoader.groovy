package net.iowntheinter.cornerstone.util

/**
 * Created by grant on 4/11/16.
 */
class resourceLoader {

    static String getResource(String name) {
        def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
        def cpth = classloader.findResource(name);
        if (cpth) {
            return (classloader.getResourceAsStream(name).getText())
        } else
            return (-1)
    }

}
