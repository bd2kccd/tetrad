package edu.cmu.causality.finances;

/**
 * This is a container class for storing the details of a finance transaction
 * when a sample is collected.
 *
 * @author adrian tang
 */
public class FinanceTransaction {
    private String expName;
    private String sampleName;
    private int sampleSize;
    private int expenses;
    private int balance;

    /**
     * Constructor.
     *
     * @param expName    experiment name.
     * @param sampleName sample name.
     * @param sampleSize sample size collected.
     * @param expenses   the cost of collecting that sample.
     * @param balance    the remaining amount of money.
     */
    public FinanceTransaction(String expName, String sampleName, int sampleSize, int expenses, int balance) {
        this.expName = expName;
        this.sampleName = sampleName;
        this.sampleSize = sampleSize;
        this.expenses = expenses;
        this.balance = balance;
    }

    /**
     * @return experiment name.
     */
    public String getExpName() {
        return expName;
    }

    /**
     * @return sample name.
     */
    public String getSampleName() {
        return sampleName;
    }

    /**
     * @return the cost of collecting that sample.
     */
    public int getExpenses() {
        return expenses;
    }

    /**
     * @return remaining money.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * @return get sample size.
     */
    public int getSampleSize() {
        return sampleSize;
    }

}
