package ru.netology;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        int port = 9999;
        int numberThreads = 64;
        var validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
                "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
                "/classic.html", "/events.html", "/events.js");

        Server server = new Server(port, validPaths, numberThreads);
        server.addHandlersToTheServer();
        server.start();
    }
}
