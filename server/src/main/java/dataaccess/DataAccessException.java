package dataaccess;

import java.sql.SQLException;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends SQLException {
    public DataAccessException(String message) {
        super(message);
    }
    public DataAccessException(String message, Throwable ex) {
        super(message, ex);
    }
}
