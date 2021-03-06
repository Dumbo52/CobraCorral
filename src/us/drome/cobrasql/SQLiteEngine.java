package us.drome.cobrasql;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a connection to a SQLite database file.
 * 
 * @author TheAcademician
 * @since 0.1
 */
public class SQLiteEngine extends SQLEngine {
    private String file;
    
    /**
     * Construct a new <tt>SQLiteEngine</tt> by specifying a logger for output and a path to database file.
     * 
     * @param logger a <tt>Logger</tt> instance for sending output.
     * @param file a <tt>String</tt> containing the path to the database file.
     * @throws java.sql.SQLException
     */
    public SQLiteEngine (Logger logger, String file) throws SQLException {
        super(logger);
        if(file == null || file.isEmpty()) {
            throw new SQLException("File parameter is required for the SQLite Engine.");
        } else {
            this.file = file;
        }
    }
    
    /**
     * Retrieve the <tt>File</tt> instance containing the location to the SQLite database file.
     * 
     * @return a <tt>File</tt> object containing the location to the database file.
     */
    public File getFile() {
        File db = new File(file);
        if(!db.isAbsolute()) {
            db = new File(Paths.get("plugins" + File.separator + "CobraCorral").toAbsolutePath().toString(), db.getPath());
        }
        return db;
    }
    
    /**
     * @return A <tt>Connection</tt> object to provide connectivity with the database.
     */
    @Override
    public Connection getConnection() {
        try {
            if(pool != null) {
                return pool.getConnection();
            } else {
                return openConnection(getFile());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }
    
    private Connection openConnection(File db) throws SQLException {
        try {
            pool = new ComboPooledDataSource();
            pool.setDriverClass("org.sqlite.JDBC");
            pool.setJdbcUrl("jdbc:sqlite:" + db);
            pool.setMaxPoolSize(50);
            return pool.getConnection();
        } catch (Exception ex) {
            throw new SQLException("Cannot load SQLite. Check your installation and try again.");
        }
    }

    @Override
    public String getName() {
        return "SQLite";
    }
}
