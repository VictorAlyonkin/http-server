package ru.netology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Request {
    @NonNull
    private String nameMethod;
    @NonNull
    private String nameHeading;
    private String bodyRequest;
}
