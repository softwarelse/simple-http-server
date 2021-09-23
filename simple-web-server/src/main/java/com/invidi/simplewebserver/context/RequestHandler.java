package com.invidi.simplewebserver.context;

import se.softwarelse.stupidhttpserver.model.Request;

public interface RequestHandler {

    /**
     * @param request data
     * @return JSON body for response, or null if there is no body to return
     * @throws RuntimeException if something goes wrong :P
     */
    String invoke(Request request);
}
