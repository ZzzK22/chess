package client;

import com.google.gson.Gson;
import shared.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    HttpClient httpClient;
    int port;
    String url;
    public ServerFacade(int port){
        this(port, "localhost");
    }
    public ServerFacade(int port, String url){
        httpClient = HttpClient.newHttpClient();
        this.port = port;
        this.url = "http://" + url + ":" + port;
    }

    public ErrorResponses logout(String authToken) {
        var response = sendRequest(Method.DELETE, url + "/session", null, authToken);
    }

    public ResponseData<AuthData> register(String username, String password, String email) {
        String request = String.format("{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\"}", username, password, email);
        var response = sendRequest(Method.POST, url + "/user", request);
        if (response.error() != null){
            return new ResponseData<>(response.error(), null);
        }
        if(response.data().statusCode() == 200) {
            return new ResponseData<>(null, new Gson().fromJson(response.data().body(), AuthData.class));
        }
        return new ResponseData<>(getErrorFromErrorMessage(response.data().body()), null);
    }

    public ResponseData<AuthData> login(String username, String password){
        String request = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        var response = sendRequest(Method.POST, url + "/session", request);
        if (response.error() != null){
            return new ResponseData<>(response.error(), null);
        }
        if(response.data().statusCode() == 200) {
            return new ResponseData<>(null, new Gson().fromJson(response.data().body(), AuthData.class));
        }
        return new ResponseData<>(getErrorFromErrorMessage(response.data().body()), null);
    }

    private static ErrorResponses getErrorFromErrorMessage(String body) {
        if(body.contains("email")){
            return ErrorResponses.InvalidEmail;
        } else if (body.contains("username")) {
            return ErrorResponses.InvalidUsername;
        } else if (body.contains("password")) {
            return ErrorResponses.InvalidPassword;
        } else if (body.contains("color")) {
            return ErrorResponses.InvalidColor;
        } else if (body.contains("game")) {
            return ErrorResponses.GameExistsnt;
        } else if (body.contains("authorization")) {
            return ErrorResponses.InvalidAuthToken;
        } else {
            return ErrorResponses.DataAccessError;
        }
    }

    private ResponseData<HttpResponse<String>> sendRequest(Method method, String url) {
        return sendRequest(method, url, null);
    }

    public ResponseData<HttpResponse<String>> sendRequest(Method method, String url, String request){
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(new URI(url));
            switch(method){
                case GET -> builder.GET();
                case PUT -> builder.PUT(HttpRequest.BodyPublishers.ofString(request));
                case POST -> builder.POST(HttpRequest.BodyPublishers.ofString(request));
                case DELETE -> builder.DELETE();
            }
            HttpRequest httpRequest = builder.build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new ResponseData<>(null, response);
        } catch (URISyntaxException e) {
            return new ResponseData<>(ErrorResponses.InvalidURL, null);
        } catch (Exception _) {
            return new ResponseData<>(ErrorResponses.MajorError, null);
        }
    }
}
