package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.responce.Answer;
import ru.netology.responce.Handler;
import ru.netology.responce.HandlerImpl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.codec.Charsets.UTF_8;

public class Server {
    private static final String TEXT_PUBLIC = "public";
    private static final String TEXT_POINT = ".";
    private static final String TEXT_QUESTION_MARK = "?";
    private static final String TEXT_SPACE = " ";
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
            final var parts = requestLine.split(TEXT_SPACE);

            if (parts.length != 3) {
                // just close socket
                return;
            }

            Request request = getRequest(parts);
            Handler handler = handlers.get(request.getNameMethod() + request.getNameHeading());
            if (handler != null) {
                handler.handle(request, out);
                return;
            }

            final var nameHeading = request.getNameHeading();
            if (!this.validPaths.contains(nameHeading)) {
                sendNotFound(out);
                return;
            }

            sendAnotherSuccessAnswer(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request getRequest(String[] parts) {
        Request request;
        int indexEndHeading = parts[1].indexOf(TEXT_QUESTION_MARK);
        if (indexEndHeading != -1) {
            request = createRequest(parts, indexEndHeading);
        } else {
            request = new Request(parts[0], parts[1], parts[2]);
        }
        return request;
    }

    private Request createRequest(String[] parts, int indexEndHeading) {
        String nameHeading = parts[1].substring(0, indexEndHeading);
        int indexStartQuery = indexEndHeading + 1;
        String queryText = parts[1].substring(indexStartQuery);
        List<NameValuePair> queryParse = URLEncodedUtils.parse(queryText, UTF_8);
        return new Request(parts[0], nameHeading, queryParse, parts[2]);
    }

    public void addHandlersToTheServer() {
        this.addHandler("GET", "/messages", HandlerImpl.handlerGet());
        this.addHandler("POST", "/messages", HandlerImpl.handlerPost());
        this.addHandler("GET", "/classic.html", HandlerImpl.handlerGetForClassicHtml());
        this.addHandler("GET", "/", HandlerImpl.handlerGet());
        this.addHandler("POST", "/", HandlerImpl.handlerPost());
    }

    public synchronized void addHandler(String nameMethod, String nameHeading, Handler handler) {
        if (!handlers.containsKey(nameMethod + nameHeading)) {
            this.handlers.put(nameMethod + nameHeading, handler);
        }
    }

    public static void sendNotFound(BufferedOutputStream out) throws IOException {
        String unsuccessfulAnswer = Answer.getUnsuccessfulAnswer();
        out.write(unsuccessfulAnswer.getBytes());
        out.flush();
    }

    public static void sendAnotherSuccessAnswer(Request request, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(TEXT_POINT, TEXT_PUBLIC, request.getNameHeading());
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);

        String successAnswer = Answer.getSuccessfulAnswer(mimeType, length);
        out.write(successAnswer.getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
