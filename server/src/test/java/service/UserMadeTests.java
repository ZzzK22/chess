package service;

import org.junit.jupiter.api.*;
import server.AuthData;
import server.ErrorResponses;
import server.UserData;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserMadeTests {
    static AuthData auth;
    static String tok;
    static UserData user;

    @BeforeAll
    public static void before(){
        user = new UserData("username", "password","email");
    }

    @BeforeEach
    public void beforeEach(){
        Service.deleteAll();
        auth = Service.register(user).data();
        tok = auth.authToken();
    }

    @Test
    public void createGame(){
        Service.createGame(tok, "bob");
        var list = Service.listGames(tok).data();
        Assertions.assertEquals(1, list.size());
    }

    @Test
    public void createGameNoAuth(){
        Service.createGame(null, "bob");
        var list = Service.listGames(tok).data();
        assert list.isEmpty();
    }

    @Test
    public void logoutTestValid(){
        Assertions.assertNull(Service.logout(tok));
    }

    @Test
    public void logoutTestInvalid(){
        Assertions.assertNotNull(Service.logout(""));
    }

    @Test
    public void loginTestValid(){
        Assertions.assertNull(Service.login(user).error());
    }

    @Test
    public void loginTestInvalid(){
        Assertions.assertNotNull(Service.login(new UserData("", "", "")).error());
        Assertions.assertNotNull(Service.login(new UserData(null, "", "")).error());
        Assertions.assertNotNull(Service.login(new UserData("", null, "")).error());
        Assertions.assertNotNull(Service.login(new UserData("username", "notpassword", "")).error());
    }

    @Test
    public void registerTestValid(){
        Assertions.assertNull(Service.register(new UserData("non", "non", "non@non.non")).error());
    }

    @Test
    public void registerTestInvalid(){
        Assertions.assertNotNull(Service.register(new UserData("username", "non", "non@non.non")).error());
        Assertions.assertNotNull(Service.register(user).error());
    }

    @Test
    public void badAuthorization(){
        Assertions.assertEquals(ErrorResponses.InvalidAuthToken, Service.listGames("badAuth").error());
        Assertions.assertEquals(ErrorResponses.InvalidAuthToken, Service.joinGame("badAuth", true, 1));
        Assertions.assertEquals(ErrorResponses.InvalidAuthToken, Service.logout("badAuth"));
        Assertions.assertEquals(ErrorResponses.InvalidAuthToken, Service.createGame("badAuth", "irrelevant").error());
    }

    @Test
    public void joinGameValid(){
        int id = Service.createGame(tok, "name").data().gameID();
        Assertions.assertNull(Service.joinGame(tok, true, id));
    }

    @Test
    public void joinGameInvalid(){
        int id = Service.createGame(tok, "name").data().gameID();
        Assertions.assertEquals(ErrorResponses.GameExistsnt, Service.joinGame(tok, true, id + 100));
        Assertions.assertNull(Service.joinGame(tok, true, id));
        Assertions.assertEquals(ErrorResponses.InvalidColor, Service.joinGame(tok, true, id));
    }
}
