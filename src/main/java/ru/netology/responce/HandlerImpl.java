package ru.netology.responce;

import ru.netology.Request;

import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class HandlerImpl {

    public static Handler handlerGet() {
        return (Request request, BufferedOutputStream out) -> {
            String response;
            if (request.getQueryParams() == null || request.getQueryParams().isEmpty()) {
                response = "Hello my friend! It`s GET method. You are the BEST!";
            } else {
                response = "Hello my friend. It`s GET method. You wrote this (may be): " + request.getQueryParams();
            }
            String successfulAnswer = Answer.getSuccessfulAnswerWithoutMimeType(response.length());
            out.write(successfulAnswer.getBytes());
            out.write(response.getBytes());
            out.flush();
        };
    }

    public static Handler handlerPost() {
        return (request, out) -> {
            String response = "Hello POS/messages. Good luck";
            String successfulAnswer = Answer.getSuccessfulAnswerWithoutMimeType(response.length());
            out.write(successfulAnswer.getBytes(StandardCharsets.UTF_8));
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        };
    }

    public static Handler handlerPostWithAnswer() {
        return (request, out) -> {
            String response = "Hello my friend. It`s POST method. You wrote this (may be): " + request.getQueryParams();
            String successfulAnswer = Answer.getSuccessfulAnswerWithoutMimeType(response.length());
            out.write(successfulAnswer.getBytes(StandardCharsets.UTF_8));
            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();
        };
    }

    public static Handler handlerGetForClassicHtml() {
        return (request, out) -> {
            final var filePath = Path.of(".", "public", request.getNameHeading());
            final var mimeType = Files.probeContentType(filePath);
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();

            String successAnswer = Answer.getSuccessfulAnswer(mimeType, content.length);

            out.write(successAnswer.getBytes());
            out.write(content);
            out.flush();
        };
    }
}
