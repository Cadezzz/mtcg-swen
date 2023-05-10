package server;

import app.App;
import http.ContentType;
import http.HttpStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.*;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class Server {
    private Request request;
    private Response response;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private App app;
    private int port;

    public Server(App app, int port) {
        setApp(app);
        setPort(port);
    }

    public void start() throws IOException {
        setServerSocket(new ServerSocket(getPort()));

        run();
    }

    private void run() {
        while (true) {
            try {
                RequestHandler requestHandler = new RequestHandler(getApp(), getServerSocket().accept());
                Thread thread = new Thread(requestHandler);
                thread.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}