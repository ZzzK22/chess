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

    @BeforeEach
    public void reinit(){
        Assertions.assertNull(serverFacade.sendRequest(Method.DELETE, serverFacade.url + "/db").error());
        Assertions.assertNull(serverFacade.register(u.username(), u.password(), u.email()).error());
        var res = serverFacade.login(u.username(), u.password());
        Assertions.assertNull(res.error());
        tok = res.data().authToken();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void registerTest() {
        Assertions.assertNotNull(serverFacade.register(u.username(), u.password(), u.email()).error());
        Assertions.assertNotNull(serverFacade.register(u.username(), u.password(), u.username()).error());
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
        Assertions.assertNull(serverFacade.listGames(tok).error());
        Assertions.assertNotNull(serverFacade.listGames("not tok").error());
    }
    @Test
    public void createGameJoinGameTests() {
        Assertions.assertNull(serverFacade.createGame("bob", tok).error());
        Assertions.assertNotNull(serverFacade.createGame("bob", "not tok").error());
        Assertions.assertNull(serverFacade.joinGame(1, true, tok));
        Assertions.assertNotNull(serverFacade.joinGame(1, true, tok));
        Assertions.assertNotNull(serverFacade.joinGame(1, false, "not tok"));
    }
    @Test
    public void logoutTest(){
        Assertions.assertNull(serverFacade.logout(tok));
        Assertions.assertNotNull(serverFacade.logout(tok));
        Assertions.assertNotNull(serverFacade.logout("not tok"));
    }

}
