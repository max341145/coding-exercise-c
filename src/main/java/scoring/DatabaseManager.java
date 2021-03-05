package scoring;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcConnectionPool;
import scoring.model.CreditScore;
import scoring.model.Customer;

import java.io.IOException;
import java.sql.SQLException;

public final class DatabaseManager {

    private static class Holder {
        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }

    public static DatabaseManager getInstance() {
        return Holder.INSTANCE;
    }

    public static final String DATABASE_JDBC_URL = "jdbc:h2:mem:customer-db;DB_CLOSE_DELAY=-1";
    public static final int PRIMITIVE_INT_NULL_VALUE = -1;

    private final Dao<Customer, Integer> customerDao;
    private final Dao<CreditScore, Integer> creditScoreDao;

    private DatabaseManager() {
        try {
            final var databaseConnection = new JdbcPooledConnectionSource(DATABASE_JDBC_URL);
            databaseConnection.setMaxConnectionsFree(4);
            databaseConnection.initialize();

            this.customerDao = DaoManager.createDao(databaseConnection, Customer.class);
            this.creditScoreDao = DaoManager.createDao(databaseConnection, CreditScore.class);

            rolloutSchema();
        } catch (SQLException ex) {
            throw new IllegalStateException("Database manager initialization failed.", ex);
        }
    }

    private void rolloutSchema() throws SQLException {
        TableUtils.createTable(customerDao);
        TableUtils.createTable(creditScoreDao);
    }

    public Dao<Customer, Integer> getCustomerDao() {
        return customerDao;
    }

    public Dao<CreditScore, Integer> getCreditScoreDao() {
        return creditScoreDao;
    }
}
