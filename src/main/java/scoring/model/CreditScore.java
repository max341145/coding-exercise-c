package scoring.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static scoring.DatabaseManager.PRIMITIVE_INT_NULL_VALUE;

//@DatabaseTable(tableName = "credit_score")
@DatabaseTable(tableName = "CREDIT_SCORE")
public class CreditScore {

    @DatabaseField(id = true)
    private int customerId;

    @DatabaseField
    private int creditScore = PRIMITIVE_INT_NULL_VALUE;

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }
}
