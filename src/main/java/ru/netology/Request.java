package ru.netology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Request {
    @NonNull
    private String nameMethod;
    @NonNull
    private String nameHeading;
    private List<Parameter> query;
    @NonNull
    private String nameProtocol;

    public String getQueryParams() {
        if (query == null) {
            return null;
        }
        return query.toString();
    }

    public String getQueryParam(String name) {
        return query.stream()
                .filter(parameter -> parameter.getKey().equals(name))
                .map(Parameter::getValue)
                .findFirst().toString();
    }
}
