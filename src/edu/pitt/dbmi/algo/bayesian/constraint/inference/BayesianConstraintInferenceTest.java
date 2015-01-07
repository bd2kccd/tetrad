package edu.pitt.dbmi.algo.bayesian.constraint.inference;

/**
 *
 * Feb 22, 2014 3:35:38 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class BayesianConstraintInferenceTest {

    public BayesianConstraintInferenceTest() {
    }

    /**
     * Test of main method, of class BayesianConstraintInference.
     */
    public void testMain() {
        String casFile = "sample_data/cooper.data/small_data.cas";
        String[] args = {
            "--cas", casFile
        };
        BayesianConstraintInference.main(args);
    }

    public static void main(String[] args) {
        new BayesianConstraintInferenceTest().testMain();
    }
}