package scoring.services;

import com.j256.ormlite.dao.Dao;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import scoring.DatabaseManager;
import scoring.algorithms.DefaultScoringAlgorithm;
import scoring.algorithms.ScoringAlgorithm;
import scoring.model.CreditScore;
import scoring.model.Customer;

import java.sql.SQLException;

public final class CreditScoreService {

    private static class Holder {
        private static final CreditScoreService INSTANCE = new CreditScoreService();
    }

    public static CreditScoreService getInstance() {
        return Holder.INSTANCE;
    }

    private ScoringAlgorithm scoringAlgorithm = new DefaultScoringAlgorithm();

    private final Dao<Customer, Integer> customerDao;
    private final Dao<CreditScore, Integer> creditScoreDao;

    private CreditScoreService() {
        customerDao = DatabaseManager.getInstance().getCustomerDao();
        creditScoreDao = DatabaseManager.getInstance().getCreditScoreDao();
    }

    /**
     * Finds credit score by customer id.
     */
    @Nullable
    public CreditScore findCreditScoreById(int customerId) {
        try {
            return creditScoreDao.queryForId(customerId);
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Finds customer's credit score. If {@code allowStaleData} set to {@code false} checks that
     * all parameters involved into scoring calculation is still relevant.
     *
     * @param allowStaleData if {@code false} then not up-to-date scoring can be returned.
     * @return Customer's scoring value.
     */
    @Nullable
    public CreditScore findCustomerCreditScore(Customer customer, boolean allowStaleData) {
        Validate.notNull(customer);

        try {
            var customerEntity = findPersistedCustomer(customer);
            if (customerEntity == null) {
                return null;
            }

            var isValidScoring = allowStaleData || (
                    customerEntity.getAge() == customer.getAge() &&
                    customerEntity.getAnnualIncome() == customer.getAnnualIncome());

            return isValidScoring ? creditScoreDao.queryForId(customerEntity.getId()) : null;
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public CreditScore performCustomerScoring(Customer customer) {
        Validate.notNull(customer);

        try {
            var customerEntity = findPersistedCustomer(customer);
            if (customerEntity != null) {
                customerDao.deleteById(customerEntity.getId());
                creditScoreDao.deleteById(customerEntity.getId());
            }

            return calculateAndPersistScoring(customer);
        } catch (SQLException ex) {
            throw new IllegalStateException("Credit score calculation failed", ex);
        }
    }

    private CreditScore calculateAndPersistScoring(Customer customer) throws SQLException {
        customerDao.create(customer);

        var creditScore = scoringAlgorithm.score(customer.getAge(), customer.getAnnualIncome());
        var creditScoreEntity = new CreditScore();
        creditScoreEntity.setCustomerId(customer.getId());
        creditScoreEntity.setCreditScore(creditScore);
        creditScoreDao.create(creditScoreEntity);

        return creditScoreEntity;
    }

    /**
     * Finds customer entity by it's first name, last name and date of birth.
     *
     * @return Found {@link Customer} or null otherwise.
     */
    @Nullable
    private Customer findPersistedCustomer(Customer customer) throws SQLException {
        var findCustomerQuery = customerDao.queryBuilder().limit(1L)
                .where()
                .eq("firstName", customer.getFirstName())
                .and().eq("lastName", customer.getLastName())
                .and().eq("dateOfBirth", customer.getDateOfBirth())
                .prepare();
        var queryResult = customerDao.query(findCustomerQuery);

        return queryResult == null || queryResult.isEmpty() ? null : queryResult.get(0);
    }

    public void setScoringAlgorithm(ScoringAlgorithm scoringAlgorithm) {
        Validate.notNull(scoringAlgorithm);

        this.scoringAlgorithm = scoringAlgorithm;
    }
}
