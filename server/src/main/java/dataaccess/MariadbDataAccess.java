package dataaccess;

import server.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public class MariadbDataAccess implements DataAccess{

    public ErrorResponses start(){
        ErrorResponses error1 = DatabaseManager.createDatabase();
        ErrorResponses error2 = DatabaseManager.createTables();
        return error1 == null &&
                error2 == null ?
                null
                : ErrorResponses.DataAccessError;
    }


    public ErrorResponses deleteAll() { // TODO: make it delete and restart the database
        try(Connection conn = DatabaseManager.getConnection().data()){
            String statement = "DROP TABLE authTokens, games, users";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                try(ResultSet rs = ps.executeQuery()){

                }
            }

        } catch (SQLException e){
            DatabaseManager.createTables();
            return ErrorResponses.DataAccessError;
        }
        DatabaseManager.createTables();
        return null;
    }

    public ResponseData<UserData> getUserByUsername(String username) {
        try(Connection conn = DatabaseManager.getConnection().data()){
            String statement = "";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                try(ResultSet rs = ps.executeQuery()){
                    rs.next();
                    return new ResponseData<>(null, null);
                }
            }

        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
    }

    public ResponseData<UserData> getUserByEmail(String email) {
        return null;
    }

    public ErrorResponses createUser(UserData userData) {
        return null;
    }

    public ResponseData<AuthData> createAuthToken(String username) {
        return null;
    }

    public ResponseData<AuthData> getAuthData(String token) {
        return null;
    }

    public ErrorResponses logout(String authToken) {
        return null;
    }

    public ResponseData<GameData> addGame(String gameName) {
        return null;
    }

    public ResponseData<Collection<GameData>> getGames() {
        return null;
    }

    public ResponseData<GameData> getGameData(int gameID) {
        return null;
    }

    public ErrorResponses joinGame(String username, boolean isWhite, int gameID) {
        return null;
    }
}
