package com.stupm.core.server.tcp;

import com.stupm.core.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

public class VertxTcpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();

        NetServer netServer = vertx.createNetServer();

        netServer.connectHandler(new VertxTcpHandler());

        netServer.listen(port , result ->{
            if(result.succeeded()) {
                System.out.println("Server started on port " + port);
            }else{
                System.err.println("Failed to start server" + result.cause());
            }
        });
    }
}
