package client;

import java.net.URI;
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

    public String login(String username, String password) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(url))
            .POST(HttpRequest.BodyPublishers.ofString(
                    String.format("{\"username\": \"%s\", \"password\": \"%s\"", username, password)
            ))
            .build();
        System.out.println(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
        return null;
    }
}
