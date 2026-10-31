package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import server.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class MariadbDataAccess implements DataAccess{

    public ErrorResponses start(){
        ErrorResponses error1 = CustomizedDatabaseManager.createDatabase();
        ErrorResponses error2 = CustomizedDatabaseManager.createTables();
        return error1 == null &&
                error2 == null ?
                null
                : ErrorResponses.DataAccessError;
    }


    public ErrorResponses deleteAll() {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            for (String i : new String[]{"authTokens", "games"}){
                String statement = "TRUNCATE TABLE " + i + ";";
                try (PreparedStatement ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
            String statement = "DELETE FROM users;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }

        } catch (SQLException e){
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    public ResponseData<UserData> getUserByUsername(String username) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "SELECT username, email, password FROM users WHERE username=?";
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
        String username = rs.getString("username");
        String email = rs.getString("email");
        String password = rs.getString("password");
        return new UserData(username, password, email);
    }

    private AuthData readAuthData(ResultSet rs) throws SQLException{
        String username = rs.getString("username");
        String token = rs.getString("token");
        return new AuthData(username, token);
    }

    public ResponseData<UserData> getUserByEmail(String email) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "SELECT username, email, password FROM users WHERE email=?";
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
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, userData.username());
                ps.setString(2, userData.email());
                ps.setString(3, userData.password()); // TODO: HASH THE PASSWORD
                ps.executeUpdate();
            }

        } catch (SQLException e){
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    public ResponseData<AuthData> createAuthToken(String username) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String queryStatement = "SELECT id FROM users WHERE username = ?;";
            String statement = "INSERT INTO authTokens (token, user_id) VALUES " +
                    "(?, ?);";
            int queryResult;
            String tok;
            try(PreparedStatement ps = conn.prepareStatement(queryStatement)) {
                ps.setString(1, username);
                try(var rs = ps.executeQuery()){
                    if(rs.next()){
                        queryResult = rs.getInt("id");
                    } else {
                        return new ResponseData<>(ErrorResponses.InvalidUsername, null);
                    }
                }
            }
            String queryStatement2 = "SELECT MAX(id) FROM authTokens;";
            try(PreparedStatement ps = conn.prepareStatement(queryStatement2)) {
                try(var rs = ps.executeQuery()){
                    if(rs.next()){
                        tok = getNewAuthTokenFromInt(rs.getInt(1));
                    } else {
                        tok = getNewAuthTokenFromInt(0);
                    }
                }
            }
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, tok);
                ps.setInt(2, queryResult);
                ps.executeUpdate();
                return new ResponseData<>(null, new AuthData(username, tok));

            }

        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
        // return new ResponseData<>(null, null); // Should be unreachable
    }

    private String getNewAuthTokenFromInt (int i){
        return "Auth: " + i;
    }

    public ResponseData<AuthData> getAuthData(String token) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "SELECT token, username FROM authTokens JOIN users ON users.id = authTokens.user_id WHERE token = ?;";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, token);
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()) {
                        return new ResponseData<>(null, readAuthData(rs));
                    }
                }
            }

        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
        return new ResponseData<>(null, null);
    }

    public ErrorResponses logout(String authToken) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String queryStatement = "SELECT * FROM authTokens WHERE token = ?;";
            try (PreparedStatement ps = conn.prepareStatement(queryStatement)) {
                ps.setString(1, authToken);
                try(ResultSet rs = ps.executeQuery()){
                    if(!rs.next()){
                        return ErrorResponses.InvalidAuthToken;
                    }
                }
            }
            String statement = "DELETE FROM authTokens WHERE token = ?;";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.executeUpdate();
            }

        } catch (SQLException e){
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    public ResponseData<GameData> addGame(String gameName) {
        ChessGame game = new ChessGame();
        int id;
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "INSERT INTO games (name, game_data) VALUES (?, ?);";
            String queryStatement = "SELECT LAST_INSERT_ID();";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, gameName);
                ps.setString(2, new Gson().toJson(game));
                ps.executeUpdate();
            }
            try(PreparedStatement ps = conn.prepareStatement(queryStatement)){
                try(ResultSet rs = ps.executeQuery()){
                    rs.next();
                    id = rs.getInt(1);
                }
            }

        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
        return new ResponseData<>(null, new GameData(game, null, null, gameName, id));
    }

    public ResponseData<Collection<GameData>> getGames() {
        ArrayList<GameData> rv = new ArrayList<>();
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "SELECT game_data, white.username, black.username, name, games.id FROM games " +
                    "LEFT JOIN users AS white ON player_white = white.id " +
                    "LEFT JOIN users AS black ON player_black = black.id;";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                try(ResultSet rs = ps.executeQuery()){
                    while(rs.next()){
                        rv.add(readGameData(rs));
                    }
                }
            }
        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
        return new ResponseData<>(null, rv);
    }

    private GameData readGameData(ResultSet rs) throws SQLException{
        ChessGame game = new Gson().fromJson(rs.getString("game_data"), ChessGame.class);
        String userWhite = rs.getString(2);
        String userBlack = rs.getString(3);
        String name = rs.getString("name");
        int id = rs.getInt(5);
        return new GameData(game, userWhite, userBlack, name, id);
    }

    public ResponseData<GameData> getGameData(int gameID) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String queryStatement = "SELECT game_data, white.username, black.username, name, games.id FROM games " +
                    "LEFT JOIN users AS white ON player_white = white.id " +
                    "LEFT JOIN users AS black ON player_black = black.id " +
                    "WHERE games.id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(queryStatement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new ResponseData<>(null, readGameData(rs));
                    } else {
                        return new ResponseData<>(null, null);
                    }
                }
            }
        } catch (SQLException e){
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
    }

    public ErrorResponses joinGame(String username, boolean isWhite, int gameID) {
        try(Connection conn = CustomizedDatabaseManager.getConnection().data()){
            String statement = "UPDATE games SET player_" + (isWhite ? "white" : "black") +
                    " = (SELECT id FROM users WHERE username = ?) WHERE id = ?";
            try(PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, username);
                ps.setInt(2, gameID);
                ps.executeUpdate();
            }

        } catch (SQLException e){
            return ErrorResponses.DataAccessError;
        }
        return null;
    }
}
