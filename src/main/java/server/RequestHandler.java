package server;

import app.App;
import http.ContentType;
import http.HttpStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Getter
@Setter
public class RequestHandler implements Runnable {

    private Request request;
    private Response response;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private App app;
    private Socket clientSocket;

    public RequestHandler(App app, Socket clientSocket) {
        setApp(app);
        setClientSocket(clientSocket);
    }

    public void run() {
        try {

            setInputStream(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));
            setRequest(new Request(getInputStream()));
            setOutputStream(new PrintWriter(clientSocket.getOutputStream(), true));

            if (request.getPathname() == null) {
                setResponse(new Response(
                        HttpStatus.BAD_REQUEST,
                        ContentType.TEXT,
                        ""
                ));
            } else {
                setResponse(getApp().handleRequest(request));
            }
            getOutputStream().write(getResponse().build());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (getOutputStream() != null) {
                    getOutputStream().close();
                }
                if (getInputStream() != null) {
                    getInputStream().close();
                    getClientSocket().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
