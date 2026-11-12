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

    public void logout(String authToken) {

    }

    public ResponseData<AuthData> login(String username, String password){
        String request = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        var response = sendRequest(Method.POST, url + "/session", request);
        if (response.error() != null){
            return new ResponseData<>(response.error(), null);
        }
        return new ResponseData<>(null, new Gson().fromJson(response.data().body(), AuthData.class));

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
