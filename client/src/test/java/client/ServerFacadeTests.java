package client;

import org.junit.jupiter.api.*;
import server.Server;
import server.UserData;


public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private static ServerFacade serverFacade;
    private static UserData u = new UserData("username", "password", "email");
    private static String tok;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        serverFacade = new ServerFacade(port);
        System.out.println("Started test HTTP server on " + port);
        serverFacade.sendRequest(Method.DELETE, serverFacade.url + "/db");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    public void registerTest() {
        Assertions.assertNull(serverFacade.register(u.username(), u.password(), u.email()).error());
        Assertions.assertNotNull(serverFacade.register(u.username(), u.password(), u.email()).error());
        Assertions.assertNotNull(serverFacade.register(u.username(), "\"},,:\"\"\\", u.email()).error());
    }
    @Test
    public void loginTest() {
        var res = serverFacade.login(u.username(), u.password());
        Assertions.assertNull(res.error());
        tok = res.data().authToken();
        Assertions.assertNotNull(serverFacade.login(u.username(), u.email()).error());

    }
    @Test
    public void listGamesTests() {

    }

}
