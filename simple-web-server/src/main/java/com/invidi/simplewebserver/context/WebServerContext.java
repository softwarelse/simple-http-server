package com.invidi.simplewebserver.context;

public interface WebServerContext {

    // TODO: Add methods for supporting static resources and controller mapping

    void setStaticPath(String path);
    String getStaticPath();

    void addController(Object controller);
    RequestHandler getHandler(String method, String requestPath);
}
