package server;

public record ResponseData<T>(ErrorResponses error, T data) {
}
