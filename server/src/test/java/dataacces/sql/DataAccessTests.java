package dataacces.sql;

import dataaccess.MariadbDataAccess;
import org.junit.jupiter.api.*;
import server.UserData;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTests {
    static MariadbDataAccess dao = new MariadbDataAccess();
    static UserData uData = new UserData("uname", "pword", "email");

    @BeforeAll
    public static void before(){
        dao.start();
        dao.deleteAll();
    }

    @BeforeEach
    public void beforeEach(){
        dao.deleteAll();
    }

    @Test
    public void userTests(){
        Assertions.assertNull(dao.getUserByEmail(uData.email()).data());
        Assertions.assertNull(dao.getUserByUsername(uData.username()).data());
        Assertions.assertNull(dao.createUser(uData));
        Assertions.assertNotNull(dao.getUserByEmail(uData.email()).data());
        Assertions.assertNotNull(dao.getUserByUsername(uData.username()).data());
    }

    @Test
    public void loginLogoutTests(){
        Assertions.assertNotNull(dao.createAuthToken(uData.username()).error());
        dao.createUser(uData);
        var data = dao.createAuthToken(uData.username());
        Assertions.assertNull(data.error());
        Assertions.assertNull(dao.logout(data.data().authToken()));
        Assertions.assertNotNull(dao.logout(data.data().authToken()));
    }

    @Test
    public void gameTests(){
        Assertions.assertNull(dao.getGameData(1).data());
        dao.createUser(uData);
        var data = dao.addGame("game");
        Assertions.assertNull(data.error());
        Assertions.assertNull(dao.joinGame(uData.username(), true, data.data().gameID()));
        var data2 = dao.getGames();
        Assertions.assertNotNull(dao.getGameData(data.data().gameID()).data());
        Assertions.assertEquals(1, data2.data().size());
    }
}

