package edu.cmu.causalityApp;

import edu.cmu.causality.ConsoleOutputStream;
import edu.cmu.causalityApp.exercise.*;
import edu.cmu.causalityApp.exerciseBuilder.ExerciseBuilderFrame;
import edu.cmu.causalityApp.exerciseBuilder.XmlFileFilter;
import edu.cmu.command.Command;
import edu.cmu.command.ExerciseHistory;
import edu.cmu.oli.CausalityActivity;
import edu.cmu.oli.superactivity.model.ActivityMode;
import nu.xom.Document;
import nu.xom.Element;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * updated: ming yang koh - mainly superActivity stuff
 * <p/>
 * This is the main Causality Lab which runs as an applet or application.
 *
 * @author Matthew Easterday
 * @author Adrian Tang
 */
public class CausalityLab extends Applet implements ActionListener {

    private static boolean isApplication;
    private static JFileChooser fileChooser = null;

    private String open;
    private String newExercise;
    private String edit;
    private String saveExercise;
    private String saveExerciseAs;
    private String submitExercise;

    private boolean debug;
    private JFrame frame;
    private static CausalityLabPanel causalityLabPanel;


    private CausalityLabToolBar causalityLabToolBar;
    private static JTextField statusBar;
    private ExerciseBuilderFrame exerciseBuilder;
    private JMenuItem saveExerciseMenuItem;
    private JMenuItem saveExerciseAsMenuItem;
    private JMenuItem submitExerciseMenuItem;
    private static Exercise currentlyLoadedExercise;

    //===================================================
    //
    //  STATIC METHODS
    //
    //===================================================

    /**
     * Returns true if we are running as an application or false as an applet
     */
    public static boolean isApplication() {
        return isApplication;
    }

    public static int getGodMode() {
        return currentlyLoadedExercise.getIsGodMode();
    }

    //===================================================
    //
    //  APPLET METHODS
    //
    //===================================================

    /**
     * Sets up the Causality Lab applet and uses the following applet tag parameters.
     * userGuid:           a user id from OLI required for talking to servlets
     * authTokenServlet:   an authorization number required for talking to servlets
     * mode:               deliverUserActivity loads an exercise =authorUserActivity creates an exercise
     * parentGuid:         an id required to save an exercise to the servlet if mode = authoerUserActivity
     * userActivityGuid:   an exercise id to load an exercise from the servlet if mode = deliverUserActivity
     */
    public void init() {
        ConsoleOutputStream.initialize(System.out, System.err);
        // the following two lines tries to pipe all the printlns
        // into the java console so that debugging is easier.
        // but gives an exception on the ODIN side.
        isApplication = false;


        String debugParam = getParameter("debug");
        debug = "true".equalsIgnoreCase(debugParam);

        // moved
        CausalityActivity.getInstance().initialize(this);
        CausalityActivity.getInstance().beginWork();

        //set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Causality Lab class - setLookAndFeel method exception:" + e.toString());
        }

        this.launchFrame();

        if (CausalityActivity.getInstance().getSession().getActivityMode() == null) {
            printDebugMsg("no mode");
            // opening the causality lab to read exercises from some OLI url
        } else if (CausalityActivity.getInstance().getSession().getActivityMode().equals(ActivityMode.DELIVERY)) {
            printDebugMsg("opening CL to read exercise from OLI");
//            System.out.println("\nmode: loadExerciseFromUrl\n");
            // disables button if student opens up exercise
            causalityLabToolBar.disableButton(CausalityLabToolBar.FEEDBACK);


            InputStream savedExercise;
            InputStream exerciseIn = null;
            //checking if student work is empty
            try {
                // using the getInstance() function to get the InputStream representing the exercise.
                exerciseIn = CausalityActivity.getInstance().getSession().readFromContentFile(CausalityActivity.getInstance().getFile());
                if (exerciseIn == null) {
//                    System.out.println("exerciseIn is NULL.");
                }

                if (CausalityActivity.getInstance().getSavedExerciseString() == null) {
                    savedExercise = null;
                } else {
                    // use the ByteArrayInputStream method to convert the String representing saved exercise to INputStream
                    savedExercise = new ByteArrayInputStream(CausalityActivity.getInstance().getSavedExerciseString().getBytes());
                }
//                System.out.println("after initialising exerciseIn and savedExercise");
//                printDebugMsg("Loading Student Work: <" + savedExercise.available() + " byte(s)>");

                // the case when there is an existing saved exercise, then that exercise will be loaded
                if (savedExercise != null && isThereSavedWork(savedExercise)) {

                    printDebugMsg("  Student work found... ");
                    BufferedReader buffered_in = new BufferedReader(
                            new InputStreamReader(savedExercise));
                    String ln;
                    while ((ln = buffered_in.readLine()) != null) {
                        System.out.println("In CausalityLab: saved = " + ln);
                    }

                    Object[] options = {"Start from scratch", "Load saved exercise"};

                    int n = JOptionPane.showOptionDialog(this,
                            "An exercise was previously saved. Do you want to start from scratch or load this saved exercise?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);

                    if (n == 1) {
                        printDebugMsg("  Loading saved exercise...");

                        // exerciseIn is assigned to be savedExercise here
                        exerciseIn = new ByteArrayInputStream(CausalityActivity.getInstance().getSavedExerciseString().getBytes());
                    }

                } else {
                    printDebugMsg("  No student work found... ");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                return;
            }

            // If deadline is not set or deadline has not passed, save function enabled
            // configuring which buttons on the panel to disable/enable.
            if (!CausalityActivity.getInstance().getDrilldown()) {
                saveExerciseMenuItem.setEnabled(true);
                causalityLabToolBar.enableButton(CausalityLabToolBar.SAVE_EXERCISE);

                if (!causalityLabPanel.isHistoryShown()) {
                    submitExerciseMenuItem.setEnabled(true);
                    causalityLabToolBar.enableButton(CausalityLabToolBar.SUBMIT_EXERCISE);
                }
            }
            readInExerciseForApplet(exerciseIn, CausalityActivity.getInstance().getDateDue(), false);

            // some modes are does not seem to be supported now in version 4.3,
            // but commented code below just in case, or for reference:

//        // opening the causality lab to save exercise to willies content service
//        } else if(appletParams.getMode().equals(OLIAppletParams.authorActivityMode)){
//            printDebugMsg("opening CL to author exercise for content author activity service");
//            System.out.println("\nMode: authorActivityMode\n");
//
//
//            authoringServiceServletIo = new AuthoringServiceServletIO(appletParams);
//            newExerciseMenuItem.setEnabled(true);

            // opening the causality lab to read exercise to willies content service
//        } else if (appletParams.getMode().equals(OLIAppletParams.deliverMode)) {
//            printDebugMsg("opening CL to read exercise from content delivery service");
//            System.out.println("\nMode: deliverMode\n");
//
//            authoringServiceServletIo = new AuthoringServiceServletIO(appletParams);
//            readInExerciseForApplet(authoringServiceServletIo.getExercise(), null, false);


            // user (i.e. TA or instructor) is reviewing an exercise completed by a
            // student in the course -- this user may alter the student's score or enter feedback
        } else if (CausalityActivity.getInstance().getUserGrading()) {
            printDebugMsg("opening CL to view work in gradebook and give feedback");
            System.out.println("\nMode: gradeExerciseMode\n");

            saveExerciseMenuItem.setEnabled(true);
            causalityLabToolBar.enableButton(CausalityLabToolBar.SAVE_EXERCISE);

            // new addition to solve the bug where instructor cannot open up the exercise properly
            InputStream savedExercise;
            InputStream exerciseIn = null;
            //checking if student work is empty

            try {
                exerciseIn = CausalityActivity.getInstance().getSession().readFromContentFile(CausalityActivity.getInstance().getFile());

                if (CausalityActivity.getInstance().getSavedExerciseString() == null) {
                    savedExercise = null;
                } else {
                    savedExercise = new ByteArrayInputStream(CausalityActivity.getInstance().getSavedExerciseString().getBytes());
                }
//                System.out.println("after initialising exerciseIn and savedExercise");

                if (savedExercise != null && isThereSavedWork(savedExercise)) {

                    printDebugMsg("  Student work found... ");
                    BufferedReader buffered_in = new BufferedReader(
                            new InputStreamReader(savedExercise));
                    String ln;
                    while ((ln = buffered_in.readLine()) != null) {
                        System.out.println("In CausalityLab: saved = " + ln);
                    }

//                    System.out.println("exerciseIn is assigned to be savedExercise here");

                    exerciseIn = new ByteArrayInputStream(CausalityActivity.getInstance().getSavedExerciseString().getBytes());
//                    }

                } else {
                    printDebugMsg("  No student work found... ");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                return;
            }


            try {
                // since exerciseIn is already 'loaded' with the saved exercise now if there exist one,
                // just call the readIn function on exerciseIn variable.
                readInExerciseForApplet(exerciseIn, null, true);

            } catch (Exception ignored) {

            }
            causalityLabToolBar.enableButton(CausalityLabToolBar.FEEDBACK);
        }
    }

    /*
     * Checks to see if there is an exercise saved for a given activity.
     * @return true if there is saved work, false otherwise.
     */
    private boolean isThereSavedWork(InputStream iStream) {
        Exercise exercise = ExerciseFileReader.read(iStream);
        return exercise != null;
    }

    /**
     * Called when the applet is destroyed - does nothing special.
     */
    public void destroy() {
    }

    /**
     * Called when
     * 1) the window loading the applet is closed before the applet is explicitly closed.
     * 2) applet is closed explicitly by the user.
     * <p/>
     * This distinction is made so that the lab can detect whether the user closes
     * the browser window first before closing the lab.
     */
    public void stop() {
    }

    //===================================================
    //
    //  APPLICATION MAIN METHOD
    //
    //===================================================

    /**
     * Main run function.
     */
    public static void main(String[] argv) {
        ConsoleOutputStream.initialize(System.out, System.err);
        System.setOut(ConsoleOutputStream.getOutStream());
        System.setErr(ConsoleOutputStream.getErrStream());

        isApplication = true;

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        fileChooser = new JFileChooser();
        CausalityLab cl = new CausalityLab();
        boolean showSplash = true;
        if (argv.length > 0) {
            showSplash = Boolean.parseBoolean(argv[0]);
        }
        if (showSplash) SplashScreen.showSplashScreen();

        cl.launchFrame();

    }

    //===================================================
    //
    //  PUBLIC METHODS
    //
    //===================================================

    /**
     * Listen for events from the "Open" and "New Exercise" menu items.
     */
    public void actionPerformed(ActionEvent e) {
        boolean isExerciseSaved = true;

        //if user clicks on the save exercise button in the exercise builder
        if (e == null || e.getSource() instanceof JButton) {
            JButton finishButton = null;
            if (e != null) {
                finishButton = (JButton) e.getSource();
            }
            if (finishButton == null || finishButton.getText().equals(ExerciseBuilderFrame.FINISH)) {
                Exercise exercise = exerciseBuilder.getExercise();

                if (isApplication) {//TODO: add open file
                    isExerciseSaved = saveExerciseToDisk(exercise, null);

                } else { //RUNNING AS AN APPLET
                    if (CausalityActivity.getInstance().getUserGrading()) {
                        String s = (String) JOptionPane.showInputDialog(
                                this,
                                "Enter a name for your exercise",
                                "Save Exercise",
                                JOptionPane.PLAIN_MESSAGE,
                                null,
                                null,
                                "myExercise.xml");

                        s = ensureXmlExtension(s);
                        String name = s.substring(0, s.length() - getExtension(s).length() - 1);
                        exercise.setTitle(name);
                        exercise.setId(name);

                        Element exerciseElement = ExerciseXmlRenderer.getElement(exercise);
//                        Document document = new Document(exerciseElement);
                        //DocType doctype = new DocType(Exercise.EXERCISE, "exercise.dtd");
                        //document.insertChild(doctype, 0);

//                        authoringServiceServletIo.submitExercise(s, document);      // superactivity - substitute with storeStudentWork()??
//                          todo: come back and check whether the below function call is needed
                        CausalityActivity.getInstance().storeStudentWork();
                    }
                }
                if (isExerciseSaved) exerciseBuilder.setVisible(false);
            }
        }
    }

    /**
     * Allows interface classes to get the main panel and make changes to it.
     *
     * @return the main panel
     */
    CausalityLabPanel getCausalityLabPanel() {
        return causalityLabPanel;
    }

    /**
     * Save the exercise to either disk or repository.
     * This method is called when the 'Save exercise' menu item or toolbar button
     * is pressed.
     * <p/>
     * todo: did not do a save for student's opening the exercise mode
     */
    public void saveExercise() {
        if (isApplication) {
            saveExerciseToDisk(currentlyLoadedExercise, ExerciseHistory.getInstance());
        } else {
            if (CausalityActivity.getInstance().getSession().getActivityMode() == null) {
                System.out.println("CausalityLab: getActivityMode() == null");

            } else if (CausalityActivity.getInstance().getSession().getActivityMode().equals(ActivityMode.REVIEW) ||
                    CausalityActivity.getInstance().getSession().getActivityMode().equals(ActivityMode.DELIVERY)) {
                System.out.println("CausalityLab: getActivityMode() is REVIEW");

                try {
                    InputStream savedExercise = CausalityActivity.getInstance().getSession().readFromContentFile(CausalityActivity.getInstance().getFile());

                    if (savedExercise.available() != 0) {
                        int response = JOptionPane.showConfirmDialog(causalityLabPanel,
                                "There is previous saved work. Do you want to replace it?",
                                "Replace old saved work",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                        if (response == JOptionPane.YES_OPTION) {
                            String exerciseXml = getCurrentExerciseXml(currentlyLoadedExercise);
//                            System.out.println("CausalityLab: Saving work");
                            System.out.println(exerciseXml);
                            CausalityActivity.getInstance().save(exerciseXml);


                            setStatusBarText("  Exercise saved in repository:" + " \"" + currentlyLoadedExercise.getId() + "\"");

                        } else {
                            JOptionPane.showMessageDialog(
                                    causalityLabPanel,
                                    "Exercise is not saved!");
                        }
                    } else {
                        String exerciseXml = getCurrentExerciseXml(currentlyLoadedExercise);
//                        System.out.println("CausalityLab: Saving work");
                        System.out.println(exerciseXml);
                        CausalityActivity.getInstance().save(exerciseXml);

                        setStatusBarText("  Exercise saved in repository:" + " \"" + currentlyLoadedExercise.getId() + "\"");
                    }

                } catch (IOException ioE) {
                    ioE.printStackTrace();
                } catch (Exception ignored) {

                }

            } else if (CausalityActivity.getInstance().getUserGrading()) {

                String exerciseXml = getCurrentExerciseXml(currentlyLoadedExercise);
//                System.out.println("CausalityLab: Saving work");
                System.out.println(exerciseXml);

                // the exercise is saved, as well as the indication of the prescene of a feedback
                // and the grade if there exist one.
                CausalityActivity.getInstance().save(exerciseXml);
                if (currentlyLoadedExercise.getInstructorFeedback() != null && currentlyLoadedExercise.getInstructorFeedback().length() != 0) {
                    CausalityActivity.getInstance().score("feedback", "true");
                } else {
                    CausalityActivity.getInstance().score("feedback", "false");
                }
                if (currentlyLoadedExercise.getGrade() != null) {
                    // THIS IS THE PART I CHANGED: FORMERLY THE SCOREID IS 'SCORE' BUT BILL CHANGED IT.                     
                    CausalityActivity.getInstance().score("grade", currentlyLoadedExercise.getGrade());
                }

                setStatusBarText("  Exercise saved in repository:" + " \"" + currentlyLoadedExercise.getId() + "\"");

            }
        }
    }


    /**
     * Submit the exercise to the repository.
     * This method is called when the 'Submit exercise' menu item or toolbar button
     * is pressed.
     */
    public void submitExercise() {
        JOptionPane.showMessageDialog(
                causalityLabPanel,
                "Submitting exercise to repository...");

        // To submit exercise, send the completed exercise and the progress status
        String exerciseXml = getCurrentExerciseXml(currentlyLoadedExercise);

        System.out.println("CausalityLab: Submitting work... ");
        System.out.println(exerciseXml);
//        System.out.println("CausalityLab: Status='Submitted' ");

        CausalityActivity.getInstance().save(exerciseXml);
        CausalityActivity.getInstance().storeStudentWork();

        CausalityActivity.getInstance().score("status", "Submitted");


        setStatusBarText("  Exercise submitted to repository:" + " \"" + currentlyLoadedExercise.getId() + "\"");
        causalityLabPanel.getModel().setExerciseSubmitted(true);
    }


    /**
     * Save instructor feedback.  Call this when an instructor has opened an
     * exercise in the gradebook and now wants to save feedback for the student.
     * <p/>
     * on top of that, the score (percent) of the exercise is to be saved as well
     * this method is very similar to the submit exercise function.
     */
    public static void saveInstructorFeedback() {
        JOptionPane.showMessageDialog(causalityLabPanel, "Saving instructor feedback");

        // To submit exercise, send the completed exercise and the progress status
        String exerciseXml = getCurrentExerciseXml(currentlyLoadedExercise);


        CausalityActivity.getInstance().save(exerciseXml);
        if (currentlyLoadedExercise.getInstructorFeedback() != null && currentlyLoadedExercise.getInstructorFeedback().length() != 0) {
            CausalityActivity.getInstance().score("feedback", "true");
        } else {
            CausalityActivity.getInstance().score("feedback", "false");
        }
        if (currentlyLoadedExercise.getGrade() != null) {


            // THIS IS THE PART I CHANGED: FORMERLY THE SCOREID IS 'SCORE' BUT BILL CHANGED IT.
            CausalityActivity.getInstance().score("grade", currentlyLoadedExercise.getGrade());
        }

        setStatusBarText("Causality lab saved instructor feedback ");
    }


    /**
     * Refresh status bar with given text.
     */
    private static void setStatusBarText(String text) {
        statusBar.setText(text);
    }

    //===================================================
    //
    //  PRIVATE METHODS
    //
    //===================================================


    // Read properties file.
    private static void getBuildInfo() {
        Properties aboutProperties = new Properties();
        try {
            InputStream is;
            is = CausalityLab.class.getResourceAsStream("/resources/build.properties");
            aboutProperties.load(is);
            String version = aboutProperties.getProperty("version");
            String buildNumber = aboutProperties.getProperty("build.number");
            String buildDate = aboutProperties.getProperty("build.date");
            System.out.println("version: " + version);
            System.out.println("build number: " + buildNumber);
            System.out.println("build date: " + buildDate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Launches the frame. (This is left as a separate method for an applet.)
    private void launchFrame() {
        getBuildInfo();

        //initializeGlobalProps();

        // Initialize strings
        String mainTitle = "Causality 4.3";
        open = "Open excercise";
        newExercise = "Make new exercise";
        edit = "Edit exercise";
        saveExercise = "Save exercise";
        saveExerciseAs = "Save exercise as";
        submitExercise = "Submit Exercise";

        // Set up the causalityLabPanel.
        causalityLabPanel = new CausalityLabPanel(this, null, true);

        // Set up the toolbar for the lab
        this.causalityLabToolBar = new CausalityLabToolBar();

        this.causalityLabToolBar.addNavigationListener(causalityLabPanel);

        // Set up the frame. Note the order in which the next few steps
        // happen. First, the frame is given a preferred size, so that if
        // someone unmaximizes it it doesn't shrivel up to the top left
        // corner. Next, the content pane is set. Next, it is packed. Finally,
        // it is maximized. For some reason, most of the details of this
        // order are important. jdramsey 12/14/02

        this.frame = new JFrame(mainTitle) {
            public Dimension getPreferredSize() {
                Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                return new Dimension(size.width - 100, size.height - 100);
            }

            public Dimension getMinimumSize() {
                return new Dimension(100, 100);
            }
        };
        this.frame.setLocale(Locale.getDefault());

        //this.frame.setContentPane(getCausalityLabPanel());

        initializeContentPane();

        this.frame.setJMenuBar(createMenuBar());
        this.frame.pack();
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                exitApplication();
            }
        });

        this.frame.setVisible(true);
        this.frame.toFront();
    }

    /**
     * Adds the toolbar, causalityLab panel and the status bar to the frame.
     */
    private void initializeContentPane() {
        statusBar = new JTextField(50);
        statusBar.setBackground(Color.LIGHT_GRAY);
        statusBar.setEditable(false);

        this.frame.getContentPane().add(causalityLabToolBar, BorderLayout.PAGE_START);
        this.frame.getContentPane().add(getCausalityLabPanel(), BorderLayout.CENTER);
        this.frame.getContentPane().add(statusBar, BorderLayout.PAGE_END);
    }

    /**
     * Enable 'save as' function for application and read in the exercise.
     */
    private void readInExerciseForApplication(File file) {
        if (!isApplication) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Applets can't use this method");
            e.printStackTrace();
        }
        System.out.println("reading file");

        // For application, allow user to select a different filename and save exercise
        saveExerciseAsMenuItem.setEnabled(true);
        causalityLabToolBar.enableButton(CausalityLabToolBar.SAVE_EXERCISE);

        // should be always true but just in case.
        if (isApplication()) {
            causalityLabToolBar.enableButton(CausalityLabToolBar.FEEDBACK);
        }
        Exercise exerciseIn = ExerciseFileReader.read(file);

        System.out.println(exerciseIn);

        readInExercise(exerciseIn, true);//, true, null, true);
    }

    /**
     * Enable the 'save' and 'submit' options based on the deadline and then
     * read in the exercise file.
     *
     * @param allowFeedback true if user allowed to leave feedback on exercise.
     */
    private void readInExerciseForApplet(InputStream iStream, Date submitDeadline, boolean allowFeedback) {
        if (isApplication) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Applications can't use this method");
            e.printStackTrace();
        }

        if ((submitDeadline != null) && (submitDeadline.getTime() > 0)) {
            // If deadline is set and the deadline has passed
            JOptionPane.showMessageDialog(
                    causalityLabPanel,
                    "Submit deadline has passed!");
        }

        Exercise exercise = ExerciseFileReader.read(iStream);
        readInExercise(exercise, allowFeedback);//, iAmAuthor, submitDeadline, canSave);
    }


    /*
     * If loading an exercise, must go through this method so that
     * currentlyLoadedExercise is set.
     *
     * @param exerciseIn the exercise to load
     * @param iAmExerciseAuthor true if opening your own exercise, i.e. which
     *  implies that we can overwrite the exercise
     * @param submitDeadline if opening a homework exercise, the deadline past
     *   which we can't submit the exercise for credit
     * @param canSave true if we can save the exercise
     */
    private void readInExercise(Exercise exerciseIn, boolean allowFeedback) {
        if (exerciseIn == null) {
            JOptionPane.showMessageDialog(frame, "Could not open exercise");
            return;
        }

        if (exerciseIn.getVersion() == null) {
            throw new NullPointerException("The version the exercise may not be null.");
        }

        if (!ExerciseXmlParserV33.VERSION.equals(exerciseIn.getVersion()) &&
                !ExerciseXmlParserV40.VERSION.equals(exerciseIn.getVersion()) &&
                !ExerciseXmlParserV41.VERSION.equals(exerciseIn.getVersion()) &&
                !ExerciseXmlParserV42.VERSION.equals(exerciseIn.getVersion()) &&
                !ExerciseXmlParserV43.VERSION.equals(exerciseIn.getVersion())) {
            JOptionPane.showMessageDialog(frame,
                    "This exercise is not marked as version 3.3. Editing and re-saving will fix the problem.");
            return;
        }

        currentlyLoadedExercise = exerciseIn;

        causalityLabToolBar.removeNavigationListener(causalityLabPanel);

        causalityLabPanel = new CausalityLabPanel(this, exerciseIn, allowFeedback);
        causalityLabToolBar.addNavigationListener(causalityLabPanel);

        frame.getContentPane().removeAll();
        initializeContentPane();
        frame.pack();

        // Enable the necessary buttons/views on toolbar
        // if so activate the feedback panel automatically
        causalityLabToolBar.enableButton(CausalityLabToolBar.ESSAY);
        causalityLabToolBar.enableButton(CausalityLabToolBar.INSTRUCT);
        causalityLabToolBar.enableButton(CausalityLabToolBar.SHOW_ANSWER);

        causalityLabToolBar.enableButton(CausalityLabToolBar.CHECK_ANSWER);

        if (currentlyLoadedExercise.isLimitResource()) {
            causalityLabToolBar.enableButton(CausalityLabToolBar.FINANCES);
        } else {
            causalityLabToolBar.disableButton(CausalityLabToolBar.FINANCES);
        }

        causalityLabToolBar.initializeFinancesBalanceView(causalityLabPanel.getModel());

        // If this is a new exercise, reset exercise history
        if (exerciseIn.getInitialCommands() == null) {
            ExerciseHistory.resetInstance();
        } else {
            Object[] options = {"Continue working", "replay work"};
            int n;

            if (!causalityLabPanel.getModel().isExerciseSubmitted()) {

                n = JOptionPane.showOptionDialog(this,
                        "This exercise has saved work. Would you like to continue working or replay what was done?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
            } else {
                n = 1;
            }

            boolean runCommands = (n == 0);

            ExerciseHistory eh = ExerciseHistory.getInstance();
            eh.clear();
            Command[] cmds = ExerciseXmlParserV43.parse(exerciseIn.getInitialCommands(), this.getCausalityLabPanel());

            eh.add(cmds, runCommands);

            if ((exerciseIn.getInstructorFeedback() != null) && (exerciseIn.getInstructorFeedback().length() != 0)) {
                causalityLabPanel.setVisibleEditor();
                causalityLabToolBar.enableButton(CausalityLabToolBar.FEEDBACK);
            }

            // If the student wants to continue working...
            if (n == 0) {
                //read in all the commands
                //maybe because the switchToNavigator will switch off everything?
                causalityLabPanel.switchToNavigator();

                // If the teacher wants to replay work...
            } else if (n == 1) {
                causalityLabPanel.switchToHistory();
            }
        }

    }

    /**
     * Creates and return the main menu bar.
     *
     * @return the main menu bar.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu exercise = new JMenu("Exercise");

        JMenuItem openExerciseMenuItem = new JMenuItem(this.open);
        JMenuItem newExerciseMenuItem = new JMenuItem(this.newExercise);
        saveExerciseMenuItem = new JMenuItem(saveExercise);
        saveExerciseAsMenuItem = new JMenuItem(saveExerciseAs);
        JMenuItem editExerciseMenuItem = new JMenuItem(edit);
        submitExerciseMenuItem = new JMenuItem(submitExercise);

        openExerciseMenuItem.setEnabled(false);
        openExerciseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fileChooser.addChoosableFileFilter(new XmlFileFilter());
                fileChooser.setAcceptAllFileFilterUsed(false);
                int returnVal = fileChooser.showOpenDialog(CausalityLab.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    readInExerciseForApplication(file);
                }
            }
        });

        newExerciseMenuItem.setEnabled(false);
        newExerciseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                File file = null;
                exerciseBuilder = new ExerciseBuilderFrame(CausalityLab.this);
            }
        });

        editExerciseMenuItem.setEnabled(false);
        editExerciseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(CausalityLab.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    Exercise exercise = ExerciseFileReader.read(file);
                    if (exercise == null) {
                        JOptionPane.showMessageDialog(frame, "Could not open exercise for editing");
                    } else
                        exerciseBuilder = new ExerciseBuilderFrame(CausalityLab.this, exercise);
                }
            }
        });

        saveExerciseMenuItem.setEnabled(false);
        saveExerciseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveExercise();
            }
        });

        saveExerciseAsMenuItem.setEnabled(false);
        saveExerciseAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isApplication) {
                    saveExerciseToDisk(currentlyLoadedExercise, ExerciseHistory.getInstance());
                } else {

                    //if (appletParams.getMode() == null) {

                    //} else {
                    // do oliservlet code to save as

                    /*Element exerciseElement = ExerciseXmlRenderer.getElement(currentlyLoadedExercise);
                    Document document = new Document(exerciseElement);
                    servletIo.save(document.toXML());*/
                    //}

                }
            }
        });

        submitExerciseMenuItem.setEnabled(false);
        submitExerciseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitExercise();
            }
        });


        JMenu help = new JMenu("Help");

        JMenuItem aboutMenuItem = new JMenuItem("About Causality Lab");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutScreen.showAboutScreen();
            }
        });

        exercise.add(newExerciseMenuItem);
        exercise.add(openExerciseMenuItem);
        exercise.add(editExerciseMenuItem);
        exercise.add(saveExerciseMenuItem);
        exercise.add(saveExerciseAsMenuItem);
        exercise.add(submitExerciseMenuItem);

//        help.add(helpMenuItem);
        help.add(aboutMenuItem);

        menuBar.add(exercise);
        menuBar.add(help);

        if (isApplication) {
            newExerciseMenuItem.setEnabled(true);
            openExerciseMenuItem.setEnabled(true);
            editExerciseMenuItem.setEnabled(true);
        }

        JMenuItem bugReportItem = new JMenuItem("Bug Report Console");
        bugReportItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BugReportConsole.showScreen();
            }
        });
        help.add(new JSeparator());
        help.add(bugReportItem);

        return menuBar;
    }


    /**
     * Takes a currently loaded exercise and generates ann xml representation
     * of that exercise with all the getModel history of actions so far.
     *
     * @return a string xml representation of the exercise and its action history
     */
    private static String getCurrentExerciseXml(Exercise currentExercise) {
        Element exerciseElement;
        Element exerciseHistory = ExerciseHistory.getInstance().render();
        exerciseElement = ExerciseXmlRenderer.getElement(currentExercise, exerciseHistory);

        Document document = new Document(exerciseElement);

        return document.toXML();
    }

    /**
     * Save the getModel exercise with any given history to a file.
     *
     * @return true if the file is saved. If user clicks cancel, returns false.
     */
    private boolean saveExerciseToDisk(Exercise exercise, ExerciseHistory history) {
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            file = new File(ensureXmlExtension(file.getPath()));
            String name = file.getName();
            name = name.substring(0, name.length() - getExtension(name).length() - 1);
            exercise.setTitle(name);
            exercise.setId(name);

            ExerciseFileWriter.write(exercise, history, file);

            setStatusBarText("  Exercise saved as" + " \"" + file.getAbsoluteFile() + "\"");
            readInExerciseForApplication(file); //Opens the file after saving it
            return true;
        }
        return false;

    }

    /*
     * Takes a filepath string and makes sure it ends with .xml
     * @param filepath
     * @return
     */
    private String ensureXmlExtension(String filepath) {
        String newpath;
        //get prefix and extension
        String extension = getExtension(filepath);
        if (extension == null) {
            newpath = filepath + ".xml";
            return newpath;
        } else if (extension.equals("xml")) {
            return filepath;
        } else {
            newpath = filepath.substring(0, filepath.length() - extension.length() - 1) + ".xml";
            JOptionPane.showMessageDialog(frame, "Changing file to" + newpath);
            return newpath;
        }
    }

    /*
     * Gets the extension of a filepath
     * @param filepath e.g. "somedir/filename.xml"
     * @return the extension, e.g. "xml"
     */
    private String getExtension(String filepath) {
        String ext = null;
        int i = filepath.lastIndexOf('.');
        if (i > 0 && i < filepath.length() - 1) {
            ext = filepath.substring(i + 1).toLowerCase();
        }
        return ext;
    }


    /*
     * Exits the application gracefully.
     */
    private void exitApplication() {
        int response = JOptionPane.showConfirmDialog(this.getCausalityLabPanel(),
                "Really exit application?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {

            if (CausalityLab.isApplication) {
                try {
                    this.frame.setVisible(false);
                    this.frame.dispose();
                    System.exit(0);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else { //if this is an applet...

                finish();
                //stop();

                this.frame.setVisible(false);
                this.frame.dispose();

                destroy();
            }
        }
    }

    /**
     * When exiting the exercise, ask the user if he wants to submit the score
     * if the lab is not completed yet.
     */
    private void finish() {

        // If non-null deadline not passed, asked if student wants to submit
        // exercise. If not, indicate "In progress" status to oli

        if (submitExerciseMenuItem.isEnabled() &&
                !causalityLabPanel.getModel().isExerciseSubmitted() &&
                !causalityLabPanel.isHistoryShown() &&
                !CausalityActivity.getInstance().getDrilldown()) {

            int response = JOptionPane.showConfirmDialog(this.getCausalityLabPanel(),
                    "Do you want to submit exercise before exiting?",
                    "Confirm submit exercise",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                submitExerciseMenuItem.doClick();
            } else {
                System.out.println("CausalityLab: Indicating 'In Progress'... ");

                CausalityActivity.getInstance().score("status", "In progress");

                response = JOptionPane.showConfirmDialog(this.getCausalityLabPanel(),
                        "Do you want to save exercise before exiting?",
                        "Confirm save exercise",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (response == JOptionPane.YES_OPTION) {
                    saveExerciseMenuItem.doClick();
                }
            }
        }

    }

    /**
     * Helper print debug msg.
     */
    private void printDebugMsg(String msg) {
        if (debug) {
            System.out.println("[debug] " + msg);
        }
    }

    /**
     * Get the mode of the exercise running.
     */
    public static ActivityMode getMode() {
        return CausalityActivity.getInstance().getMode();
    }
}
