package com.invidi.mywebproject;

import com.invidi.simplewebserver.main.SimpleWebServer;
import com.invidi.simplewebserver.main.WebServer;
import se.softwarelse.stupidhttpserver.StupidHttpServer;

public class MyWebApplication {

   public static void main(String[] args) {
      final WebServer ws = new StupidHttpServer();

      // TODO: Set path for static files
      // TODO: Register controller MyController

      /*
       * Example:
       *
       *  ws.getWebContext().setStaticPath("/static");
       *  ws.getWebContext().addController(new MyController());
       */

      ws.start(8080);
   }
}
