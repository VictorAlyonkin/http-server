package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.netology.responce.Answer;
import ru.netology.responce.Handler;
import ru.netology.responce.HandlerImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.apache.commons.codec.Charsets.UTF_8;

public class Server {
    private static final String TEXT_PUBLIC = "public";
    private static final String TEXT_POINT = ".";
    private final int port;
    private final List<String> validPaths;
    private final ExecutorService threadPool;
    private final Map<String, Handler> handlers = new HashMap<>();

    private final List<String> allowedMethods = List.of("GET", "POST");

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
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            Request request = getRequest(in);

            if (request == null) {
                sendNotFound(out);
                return;
            }

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

    private Request getRequest(BufferedInputStream in) throws IOException {

        int limit = 4096;
        byte[] buffer = new byte[limit];
        in.mark(limit);
        int read = in.read(buffer);

        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);


        if (requestLineEnd == -1) {
            return null;
        }

        // читаем request line
        final var parts = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

        if (parts.length != 3) {
            return null;
        }

        final var method = parts[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }

        final var path = parts[1];
        if (!path.startsWith("/")) {
            return null;
        }

        final var protocol = parts[2];

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        // для GET тела нет
        if (!method.equals("GET")) {
            in.skip(headersDelimiter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                int contentLengthInt = Integer.parseInt(contentLength.get());
                List<Parameter> query = createQuery(in, contentLengthInt);
                return new Request(method, path, query, protocol);
            }
            return null;
        }
        return new Request(method, path, protocol);


    }

    private List<Parameter> createQuery(BufferedInputStream in, int contentLengthInt) throws IOException {

        final var bodyBytes = in.readNBytes(contentLengthInt);
        final var body = new String(bodyBytes);

        List<NameValuePair> parse = URLEncodedUtils.parse(body, UTF_8);
        return parse.stream()
                .map(nameValuePair -> new Parameter(nameValuePair.getName(), nameValuePair.getValue()))
                .collect(Collectors.toList());
    }

    public void addHandlersToTheServer() {
        this.addHandler("GET", "/messages", HandlerImpl.handlerGet());
        this.addHandler("POST", "/messages", HandlerImpl.handlerPost());
        this.addHandler("GET", "/classic.html", HandlerImpl.handlerGetForClassicHtml());
        this.addHandler("GET", "/", HandlerImpl.handlerGet());
        this.addHandler("POST", "/", HandlerImpl.handlerPost());
        this.addHandler("POST", "/?value=get-value", HandlerImpl.handlerPostWithAnswer());
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

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
