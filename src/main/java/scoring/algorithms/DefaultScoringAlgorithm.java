package scoring.algorithms;

public class DefaultScoringAlgorithm implements ScoringAlgorithm {

    public static final int AGE_THRESHOLD = 99;
    public static final int INCOME_THRESHOLD = 120000;

    @Override
    public int score(int age, int annualIncome) {
        double ageNormalized = (double) Math.min(age, AGE_THRESHOLD) / AGE_THRESHOLD;
        double annualIncomeNormalized = (double) Math.min(annualIncome, INCOME_THRESHOLD) / INCOME_THRESHOLD;

        double scoreFactor = 0.4 * ageNormalized + 0.6 * annualIncomeNormalized;
        return (int) Math.round(scoreFactor * SCORE_MAX_VALUE);
    }
}
