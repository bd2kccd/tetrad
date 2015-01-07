package edu.cmu.oli;

import edu.cmu.oli.content.model.ResourceInfo;
import edu.cmu.oli.superactivity.SuperActivityException;
import edu.cmu.oli.superactivity.client.*;
import edu.cmu.oli.superactivity.model.*;

import java.applet.Applet;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author mykoh
 */
public class CausalityActivity {

    private static final CausalityActivity ACTIVITY = new CausalityActivity();
    private final List<LoggingListener> listeners = new ArrayList<LoggingListener>();
    private Applet causalityLab;

    /**
     * Activity client
     */
    private SuperActivityClient client;
    /**
     * Activity session
     */
    private SuperActivitySession session;
    /**
     * Logging service
     */
    private LoggingServices logging;
    /**
     * Grading service
     */
    private GradingServices grading;
    /**
     * Storage service
     */
    private static StorageServices storage;
    /**
     * User ID
     */
    private static String userGuid;
    /**
     * Activity ID
     */
    private static String actGuid;

    /**
     * Resource file
     */
    private edu.cmu.oli.content.model.File file;
    /**
     * Are we in drilldown?
     */
    private boolean drilldown;
    /**
     * Can user override grade?
     */
    private boolean userGrading;

    /**
     * Current attempt
     */
    private ActivityAttempt attempt;

    /**
     * Date due
     */
    private Date dateDue;

    public static CausalityActivity getInstance() {
        return ACTIVITY;
    }

    /**
     * Mark the attempt as started, or determine that if it
     * already had been, decide what to do
     */
    public void beginWork() {
//        System.out.println("start of beginWork() function");

        attempt = grading.getCurrentAttempt();
        if (attempt == null) {
            try {
                attempt = grading.startNewAttempt();
            } catch (SuperActivityException e) {
                errorReport(e);
            } catch (RuntimeException e) {
                errorReport(e);
                throw e;
            }
        } else if (attempt.isCompleted()) {
            // Start new attempt...
            try {
                attempt = grading.startNewAttempt();
            } catch (SuperActivityException e) {
                errorReport(e);
            } catch (RuntimeException e) {
                errorReport(e);
                throw e;
            }
        } else {
            // Prompt user to restore saved work, etc..
//            showMsg("Cannot start new attempt.", "Cannot start new attempt. Current attempt has not been ended.");
//            System.out.println("Cannot start new attempt");

        }
    }

    /**
     * Set the score for this attempt
     *
     * @param scoreId    Id of the score type ("column")
     * @param scoreValue Score for this student/attempt
     */
    public void score(String scoreId, String scoreValue) {
        System.out.println("start of score function");
        String originalScore = scoreValue;
        try {
            double realScore = Double.parseDouble(scoreValue) / 100.;
            NumberFormat nf = new DecimalFormat("0.00");
            scoreValue = nf.format(realScore);
        } catch (NumberFormatException e) {
            scoreValue = originalScore;
        }

        try {
            if (drilldown) {
                System.out.println(attempt.getNumber());
                System.out.println(session.getAttemptNumber());
                attempt = grading.scoreReviewedAttempt(scoreId, scoreValue);
            } else {
                attempt = grading.scoreCurrentAttempt(scoreId, scoreValue);
            }
        } catch (SuperActivityException e) {
            errorReport(e);
        } catch (RuntimeException e) {
            System.out.println(e);
            errorReport(e);
        }
    }

    public void save(String exerciseXML) {
        System.out.println("start of save function");
        RecordContext recCtx = new RecordContext(userGuid, null, actGuid, null);
        try {
            storage.storeAsFileRecord(recCtx, "work.xml", exerciseXML);
        } catch (Exception e) {
            errorReport(e);
        }
    }

    public void logAction(String action, String infoType, String info) {
        for (LoggingListener i : listeners) {
            i.logPerformed(action, infoType, info);
        }
        if (logging != null)
            logging.logActionLog(action, infoType, info);
    }


    /**
     * Get the mode of the exercise running.
     */
    public ActivityMode getMode() {
        if (session == null)
            return null;

        ActivityMode tempMode = session.getActivityMode();

        if (tempMode != null)
            return tempMode;
        else
            return null;
    }

    public void initialize(Applet causalityLab) {
        this.causalityLab = causalityLab;
        try {
            client = SuperActivityClientFactory.getSuperActivityClient(causalityLab);
            session = client.beginActivitySession(causalityLab);
        } catch (Exception e) {
            System.out.println("client or session variable not created properly, met ");
            e.printStackTrace();
        }

        logging = client.getLoggingServices();
        logging.setAsynchronous(true);

        grading = session.getGradingServices();
        Problem problem = grading.getProblem();
        System.out.println("Identifying grading attributes...");
        for (Iterator i = problem.getGradingAttributeIds(); i.hasNext(); ) {
            String id = (String) i.next();
            String val = problem.getGradingAttributeValue(id);
            System.out.println("grading attribute: id=" + id + ", value=" + val);
        }

        System.out.println("settling storage services now");
        storage = session.getStorageServices();


        SessionMetadata md = session.getSessionMetadata();
        userGuid = md.getUserGuid();
        actGuid = md.getActivityGuid();
//        sectionGuid = md.getSectionGuid();
//        applicationBase = client.getApplicationBaseURL();
        ResourceInfo rsrcInfo = md.getResourceInfo();
        file = rsrcInfo.getFile();
//        maxAttempts = grading.getMaxAttempts();
//        isHighStakes = md.isHighStakes();
//        dateAssigned = md.getDateAssigned();
        dateDue = md.getDateDue();
//        isJustInTime = md.isJustInTime();
//        status = md.getActivityStatus();

        ActivityMode actMode = session.getActivityMode();
        if (ActivityMode.REVIEW == actMode) {
            if (md.getAuthorizations().getGradeResponses()) {
// in review mode, user can grade (i.e. change scores)
                userGrading = true;
            } else if (md.getAuthorizations().getViewResponses()) {
// in review mode, user can review respones but not grade
                userGrading = false;
            }
            drilldown = true;
        } else {
            drilldown = false;
            userGrading = false;
        }
        System.out.println("done with all initilization");
    }

    /**
     * Add the oli logger listener.
     */
    public void addLoggingListener(LoggingListener oll) {
        listeners.add(oll);
    }

    public SuperActivitySession getSession() {
        return session;
    }

    public edu.cmu.oli.content.model.File getFile() {
        return file;
    }

    public boolean getUserGrading() {
        return userGrading;
    }


    public boolean getDrilldown() {
        return drilldown;
    }

    public Date getDateDue() {
        return dateDue;
    }

    public String getSavedExerciseString() {
        RecordContext recCtx = new RecordContext(userGuid, null, actGuid, null);

        String work = "";
        try {
            work = storage.loadFileRecordAsString(recCtx, "work.xml");
        } catch (Exception e) {
            errorReport(e);
        }
        System.out.println("printing out the stuff for the input stream: work variable:" + work);
        return work;
    }

    //  Storing files keyed on student and activity, such as saved partial solutions
    public void storeStudentWork() {
        String work = "Some string of saved work for this student for just this problem";


        RecordContext recCtx;
        if (attempt != null) {
            work = work + ": attempt=" + attempt.getNumber();
            recCtx = new RecordContext(userGuid, null, actGuid, attempt.getNumber());
        } else {
            recCtx = new RecordContext(userGuid, null, actGuid, null);
        }
        try {
            storage.storeAsFileRecord(recCtx, "work.txt", work);
        } catch (Exception e) {
            errorReport(e);
        }
    }

    private void errorReport(Exception e) {
        // Report exception to user
        e.printStackTrace();

        if (e instanceof SuperActivityException) {
            SuperActivityException sae = (SuperActivityException) e;
            showMsg("SuperActivityException", "SuperActivityException: message=" + sae.getMessage() + ", errorCondition=" + sae.toString());
        } else {
            showMsg("Some other error...", "ERROR: " + e.toString());
        }
    }

    private void showMsg(String title, String msg) {
        Frame f = findParentFrame();

        if (f != null) {
            Dialog d = new Dialog(f, title, false);
            d.setLayout(new FlowLayout());
            d.add(new Label("" + msg));
            d.pack();
            d.setLocation(100, 100);
            d.setVisible(true);
        }
    }

    private Frame findParentFrame() {
        Container c = causalityLab;

        while (c != null) {
            if (c instanceof Frame)
                return (Frame) c;
            c = c.getParent();
        }

        return null;
    }


}



