package scoring.algorithms;

public interface ScoringAlgorithm {
    double SCORE_MAX_VALUE = 999;

    int score(int age, int annualIncome);
}
