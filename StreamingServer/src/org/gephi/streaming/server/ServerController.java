package org.gephi.streaming.server;


public interface ServerController {

    public ClientManager getClientManager();

    public void handle(Request request, Response response);

    public void stop();

}