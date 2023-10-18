package ru.netology.responce;

public class Answer {

    public static String getSuccessfulAnswer(String mimeType, long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }

    public static String getUnsuccessfulAnswer() {
        return """
                HTTP/1.1 404 Not Found\r
                Content-Length: 0\r
                Connection: close\r
                \r
                """;
    }

    public static String getSuccessfulAnswerWithoutMimeType(long length) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
}
