package ru.netology;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        int port = 9999;
        var validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
                "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
                "/classic.html", "/events.html", "/events.js");

        Server server = new Server(port, validPaths);
        server.start();
    }
}


