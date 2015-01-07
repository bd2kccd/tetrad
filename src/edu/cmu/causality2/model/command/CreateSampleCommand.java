package edu.cmu.causality2.model.command;

import edu.cmu.causality.CausalityLabModel;
import edu.cmu.command.AbstractCommand;
import nu.xom.Attribute;

import java.text.NumberFormat;

/**
 * If sampleCost is -1, the resources information is irrelevant.
 *
 * @author mattheweasterday
 */
public class CreateSampleCommand extends AbstractCommand {

    /**
     * Unique string id of this moves.
     */
    private static final String MY_NAME = "createSampleCommand";

    private static final String EXP_SETUP_NAME = "experimentalSetupName";
    private static final String SAMPLE_NAME = "sampleName";
    private static final String SAMPLE_SIZE = "sampleSize";
    private static final String SAMPLE_SEED = "sampleSeed";
    private static final String SAMPLE_COST = "sampleCost";

    private final String experimentalSetupName;
    private final int sampleSize;
    private final String sampleName;
    private int id;
    private Long sampleSeed;
    private final int sampleCost;

    final private static NumberFormat nf;

    static {
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMinimumFractionDigits(0);
        nf1.setMaximumFractionDigits(0);
        nf = nf1;
    }

    /**
     * Constructor.
     *
     * @param experimentalSetupName the name of the experiment the sample is created from
     * @param sampleSize            size of the sample
     * @param sampleName            name of the sample
     */
    public CreateSampleCommand(String experimentalSetupName, int sampleSize, String sampleName, int sampleCost) {
        this.experimentalSetupName = experimentalSetupName;
        this.sampleSize = sampleSize;
        this.sampleName = sampleName;
        this.sampleSeed = null;
        this.sampleCost = sampleCost;
    }

    /**
     * Constructor.
     *
     * @param experimentalSetupName the name of the experiment the sample is created from
     * @param sampleSize            size of the sample
     * @param sampleName            name of the sample
     */
    public CreateSampleCommand(String experimentalSetupName, int sampleSize, String sampleName, Long sampleSeed, int sampleCost) {
        this.experimentalSetupName = experimentalSetupName;
        this.sampleSize = sampleSize;
        this.sampleName = sampleName;
        this.sampleSeed = sampleSeed;
        this.sampleCost = sampleCost;
    }

    /**
     * Executes moves by creating a sample.
     */
    public void justDoIt() {
        id = CausalityLabModel.getModel().makeNewSample(experimentalSetupName, sampleSize, sampleName, sampleSeed);
        sampleSeed = CausalityLabModel.getModel().getSampleSeed(id).getSeed();

        if (sampleCost != -1) {
            CausalityLabModel.getModel().addFinanceTransaction(experimentalSetupName, sampleName, sampleSize, sampleCost);
        }
    }

    /**
     * Undoes the moves by deleting the sample.
     */
    public void undo() {
        CausalityLabModel.getModel().deleteSample(id);

        if (sampleCost != -1) {
            CausalityLabModel.getModel().removeFinanceTransaction(experimentalSetupName, sampleName, sampleSize, sampleCost);
        }
    }

    /**
     * String representing the moves used for display in moves history.
     *
     * @return "Sample created"
     */
    public String toString() {
        if (sampleCost == -1) {
            return "Sample created" + " (n = " + sampleSize + ")";
        } else {
            return "Sample created" + " (n = " + sampleSize + ", Cost = " + nf.format(sampleCost) + ")";
        }
    }

    /**
     * String used in xml represetnation.
     *
     * @return "createSampleCommand"
     */
    public String getCommandName() {
        return MY_NAME;
    }


    /**
     * Attributes used in xml representation of the moves.
     *
     * @return the xml representation of this moves.
     */
    protected Attribute[] renderAttributes() {
        Attribute[] atts = new Attribute[5];
        atts[0] = new Attribute(EXP_SETUP_NAME, experimentalSetupName);
        atts[1] = new Attribute(SAMPLE_NAME, sampleName);
        atts[2] = new Attribute(SAMPLE_SIZE, (new Integer(sampleSize)).toString());
        atts[3] = new Attribute(SAMPLE_SEED, (sampleSeed).toString());
        atts[4] = new Attribute(SAMPLE_COST, (new Integer(sampleCost)).toString());
        return atts;
    }
}
