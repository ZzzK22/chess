package shared;

public record ResponseData<T>(ErrorResponses error, T data) {
}
