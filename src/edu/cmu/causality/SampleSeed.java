package edu.cmu.causality;

/**
 * This class holds all the information needed to calculate a sample.
 */
public class SampleSeed {
    private final int sampleId;
    private final String expName;
    private final int sampleSize;
    private final String name;
    private final long seed;

    /**
     * Constructor
     *
     * @param experimentName the unique sampleId of the experiment.
     * @param sampleSize     the number of cases to put in the sample.
     * @param name           the name of the sample.
     * @param seed           a number used to recalculate the seed the same way each
     *                       time.
     */
    public SampleSeed(int sampleId, String experimentName, int sampleSize, String name,
                      Long seed) {
        this.sampleId = sampleId;
        this.expName = experimentName;
        this.name = name;
        this.sampleSize = sampleSize;
        if (seed == null) {
            this.seed = System.currentTimeMillis();
        } else {
            this.seed = seed;
        }
    }

    /**
     * Gets sampleId.
     *
     * @return sampleId sample sampleId.
     */
    public int getSampleId() {
        return sampleId;
    }

    /**
     * Gets the experiment sampleId.
     *
     * @return experiment sampleId.
     */
    public String getExpName() {
        return expName;
    }

    /**
     * Gets the name of the sample.
     *
     * @return sample name.
     */
    public String getName() {
        return name;
    }

    /**
     * gets the number of cases in the sample.
     *
     * @return sample size.
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Gets the seed used to create the sample.
     *
     * @return the seed.
     */
    public long getSeed() {
        return seed;
    }
}
