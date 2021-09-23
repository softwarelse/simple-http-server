package com.invidi.simplewebserver.context;

import se.softwarelse.stupidhttpserver.model.Request;

public interface RequestHandler {

    /**
     * @param request data
     * @return Body for response in POJO form, needs to be serialized using jackson, or null if there is no body to return
     * @throws RuntimeException if something goes wrong :P
     */
    Object invoke(Request request);
}
