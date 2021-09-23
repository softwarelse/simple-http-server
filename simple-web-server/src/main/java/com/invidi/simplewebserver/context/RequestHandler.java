package com.invidi.simplewebserver.context;

import se.softwarelse.stupidhttpserver.model.Request;

public interface RequestHandler {
    String invoke(Request request);
}
