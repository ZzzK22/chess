package dataaccess;

import server.ErrorResponses;
import server.ResponseData;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    static String getDatabaseName(){
        return databaseName;
    }

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public ErrorResponses createDatabase() {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    static public ErrorResponses createTables() {
        ErrorResponses error1 = createUserTable();
        ErrorResponses error2 = createAuthTokenTable();
        ErrorResponses error3 = createGamesTable();
        return error1 == null &&
                error2 == null &&
                error3 == null ?
                null
                : ErrorResponses.DataAccessError;
    }

    static public ErrorResponses createUserTable() {
        var statement = "";
        try (var conn = DatabaseManager.getConnection().data();
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    static public ErrorResponses createAuthTokenTable() {
        var statement = "";
        try (var conn = DatabaseManager.getConnection().data();
             var preparedStatement = conn.prepareStatement(statement)) {
             preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getSQLState());
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    static public ErrorResponses createGamesTable() {
        var statement = "";
        try (var conn = DatabaseManager.getConnection().data();
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            return ErrorResponses.DataAccessError;
        }
        return null;
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static ResponseData<Connection> getConnection() {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return new ResponseData<>(null, conn);
        } catch (SQLException ex) {
            return new ResponseData<>(ErrorResponses.DataAccessError, null);
        }
    }

    private static void loadPropertiesFromResources(){
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties"); // TODO: figure out what in the world this is
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties", ex); // TODO: And this too
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}
