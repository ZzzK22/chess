package userTests;

import org.junit.jupiter.api.*;
import server.AuthData;
import server.Service;
import server.UserData;

import java.net.HttpURLConnection;

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
    }

    @Test
    public void registerTestValid(){

    }
}
