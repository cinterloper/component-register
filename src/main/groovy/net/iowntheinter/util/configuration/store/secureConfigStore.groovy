package net.iowntheinter.util.configuration.store

/**
 * Created by grant on 4/18/16.
 */
interface secureConfigStore extends configStore{
    void setCredentials(Map creds)
}
