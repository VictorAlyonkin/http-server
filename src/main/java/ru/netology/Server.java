package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final List<String> validPaths;
    private final ExecutorService threadPool;
    private final Map<String, Handler> handlers = new HashMap<>();

    public Server(int port, List<String> validPaths, int numberThreads) {
        this.port = port;
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(numberThreads);
    }

    public void start() {
        try (var serverSocket = new ServerSocket(this.port)) {
            while (!serverSocket.isClosed()) {
                final var socket = serverSocket.accept();
                threadPool.execute(() -> this.connect(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            Request request = new Request(parts[0], parts[1], parts[2]);
            Handler handler = handlers.get(request.getNameMethod() + request.getNameHeading());
            if (handler != null) {
                handler.handle(request, out);
                return;
            }

            final var path = parts[1];
            if (!this.validPaths.contains(path)) {
                String unsuccessfulAnswer = getUnsuccessfulAnswer();
                out.write(unsuccessfulAnswer.getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();

                String successAnswer = getSuccessfulAnswer(mimeType, content.length);

                out.write(successAnswer.getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);

            String successAnswer = getSuccessfulAnswer(mimeType, length);
            out.write(successAnswer.getBytes());
            Files.copy(filePath, out);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSuccessfulAnswer(String mimeType, long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public String getUnsuccessfulAnswer() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public String getSuccessfulAnswerWithoutMimeType(long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public synchronized void addHandler(String nameMethod, String nameHeading, Handler handler) {
        if (!handlers.containsKey(nameMethod + nameHeading)) {
            this.handlers.put(nameMethod + nameHeading, handler);
        }
    }
}
