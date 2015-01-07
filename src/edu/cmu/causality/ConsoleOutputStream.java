package edu.cmu.causality;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class forks the output of the System.out such that (1)the output is
 * recorded in a buffer for saving to a file or viewing and (2)the output is
 * displayed on the java console.
 *
 * @author adrian tang
 *
 */
public class ConsoleOutputStream extends PrintStream {
    final static private String SYSTEM_OUT = "SYSTEM_OUT";
    final static private String SYSTEM_ERR = "SYSTEM_ERR";

    private static ConsoleOutputStream outStream;
    private static ConsoleOutputStream errStream;
    private static final StringBuffer buffer = new StringBuffer();

    private OutputStream oldOutputStream;
    private String type;

    /**
     * @return the singleton instance of the console output stream.
     */
    public static ConsoleOutputStream getOutStream() {
        if (outStream == null) {
            throw new NullPointerException("outStream not initialized");
        } else {
            return outStream;
        }
    }

    /**
     * @return the singleton instance of the console error stream.
     */
    public static ConsoleOutputStream getErrStream() {
        if (errStream == null) {
            throw new NullPointerException("errStream not initialized");
        } else {
            return errStream;
        }
    }

    /**
     * Initialize the console output stream with the given output stream.
     *
     * @param sysOutStream use the System.out here.
     * @param sysErrStream use the System.err here.
     */
    public static void initialize(OutputStream sysOutStream,
                                  OutputStream sysErrStream) {
        outStream = new ConsoleOutputStream(sysOutStream);
        outStream.setOldOutputStream(sysOutStream);
        outStream.setType(SYSTEM_OUT);

        errStream = new ConsoleOutputStream(sysErrStream);
        errStream.setOldOutputStream(sysErrStream);
        outStream.setType(SYSTEM_ERR);
    }

    /**
     * @return the string message(s) in the buffer now.
     */
    public static String getConsoleOutput() {
        return buffer.toString();
    }

    /**
     * Prints the line to the java console and records the line in the buffer.
     *
     * @param str the line of string to print.
     */
    public void println(String str) {
        buffer.append(str).append("\n");

        if (type.equals(SYSTEM_OUT)) {
            System.setOut((PrintStream) getOldOutputStream());
            System.out.println(str);
            System.setOut(this);
        } else if (type.equals(SYSTEM_ERR)) {
            System.setErr((PrintStream) getOldOutputStream());
            System.err.println(str);
            System.setErr(this);
        }
    }

    /**
     * This method is used by the System.err stream to display error trace like
     * "Exception in thread "main..." which are not displayed by the println
     * method.
     */
    public void write(byte[] b, int off, int len) {
        buffer.append(new String(b, off, len));

        System.setErr((PrintStream) getOldOutputStream());
        System.err.println(new String(b, off, len));
        System.setErr(this);
    }

    /**
     * @return the old outputStream.
     */
    OutputStream getOldOutputStream() {
        return oldOutputStream;
    }

    /**
     * Set a pointer to the old System.out, so that the output stream can be
     * restored.
     *
     * @param oldOut the old outputStream.
     */
    void setOldOutputStream(OutputStream oldOut) {
        this.oldOutputStream = oldOut;
    }

    void setType(String type) {
        this.type = type;
    }

    /**
     * Private constructor.
     */
    private ConsoleOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

}
