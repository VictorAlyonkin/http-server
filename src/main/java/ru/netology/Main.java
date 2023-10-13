package ru.netology;

import java.io.BufferedOutputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        int port = 9999;
        int numberThreads = 64;
        var validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
                "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
                "/classic.html", "/events.html", "/events.js");

        Server server = new Server(port, validPaths, numberThreads);
        addHandlersToTheServer(server);
        server.start();
    }

    public static void addHandlersToTheServer(Server server) {
        server.addHandler("GET", "/messages",
                (Request request, BufferedOutputStream out) -> {
                    String response = "Hello GET/messages. Good luck";
                    String successfulAnswer = server.getSuccessfulAnswerWithoutMimeType(response.length());
                    out.write(successfulAnswer.getBytes());
                    out.write(response.getBytes());
                    out.flush();
                });
        server.addHandler("POST", "/messages", (request, out) -> {
            String response = "Hello POS/messages. Good luck";
            String successfulAnswer = server.getSuccessfulAnswerWithoutMimeType(response.length());
            out.write(successfulAnswer.getBytes());
            out.write(response.getBytes());
            out.flush();
        });
    }
}
