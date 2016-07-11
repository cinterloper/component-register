This project coordonates the startup of a group of Vert.X verticles, and a group of Docker containers. 
it maintains a service registry of deployed components, and provides an event based startup coordonation service

this service waits for expected components to register, and then announces a 'start' message

a docker base image with utilities for the docker containers to perform health checks, and register with the vertx instance or cluster when they are ready, is provided in Containers/

The container utilities (lash) allow each container to listen for pre-configured requests on the Vert.X event bus, and run scripts that react to them, optionally returning output data

Cornerstone also provides basic implementation of logging, console output, command line parsing,
