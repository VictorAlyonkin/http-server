package ru.netology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Request {
    @NonNull
    private String nameMethod;
    @NonNull
    private String nameHeading;
    private List<NameValuePair> query;
    @NonNull
    private String nameProtocol;

    public List<NameValuePair> getQueryParams() {
        if (query == null) {
            return null;
        }
        return query;
    }

    public Optional<NameValuePair> getQueryParam(String name) {
        return query.stream()
                .filter(parameter -> parameter.getName().equals(name))
                .findFirst();
    }
}
