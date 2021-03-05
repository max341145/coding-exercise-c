package scoring.services;

import com.j256.ormlite.dao.Dao;
import org.apache.commons.lang3.Validate;
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

    private Customer findPersistedCustomer(Customer customer) throws SQLException {
        var findCustomerQuery = customerDao.queryBuilder().limit(1L)
                .where()
                .eq("firstName", customer.getFirstName()).and()
                .eq("lastName", customer.getLastName()).and()
                .eq("dateOfBirth", customer.getDateOfBirth())
                .prepare();
        var result = customerDao.query(findCustomerQuery);

        return result == null || result.isEmpty() ? null : result.get(0);
    }

    public void setScoringAlgorithm(ScoringAlgorithm scoringAlgorithm) {
        Validate.notNull(scoringAlgorithm);

        this.scoringAlgorithm = scoringAlgorithm;
    }
}
