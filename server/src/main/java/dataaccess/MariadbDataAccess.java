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


    public ErrorResponses deleteAll() {
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
            String statement = "SELECT username, email FROM users WHERE username=?";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, username);
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()) {
                        return new ResponseData<>(null, readUser(rs));
                    }
                }
            }

        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
        return new ResponseData<>(null, null);
    }

    private UserData readUser(ResultSet rs) throws SQLException{
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        return new UserData(username, null, email);
    }

    private AuthData readAuthData(ResultSet rs) throws SQLException{
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String token = rs.getString("token");
        return new AuthData(username, token);
    }

    public ResponseData<UserData> getUserByEmail(String email) {
        try(Connection conn = DatabaseManager.getConnection().data()){
            String statement = "SELECT username, email FROM users WHERE email=?";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, email);
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()) {
                        return new ResponseData<>(null, readUser(rs));
                    }
                }
            }

        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
        return new ResponseData<>(null, null);
    }

    public ErrorResponses createUser(UserData userData) {
        try(Connection conn = DatabaseManager.getConnection().data()){
            String statement = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, userData.username());
                ps.setString(2, userData.email());
                ps.setString(3, userData.password()); // TODO: HASH THE PASSWORD
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
