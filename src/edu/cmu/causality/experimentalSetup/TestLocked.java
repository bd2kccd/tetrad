package edu.cmu.causality.experimentalSetup;

import edu.cmu.causality.experimentalSetup.manipulation.Locked;

/**
 * @author Adrian Tang
 */
public class TestLocked extends junit.framework.TestCase {

    public TestLocked(String name) {
        super(name);
    }

    public void testLocked() {
        boolean except = false;
        Locked var = new Locked();
        try {
            var.setLockedAt("A");
        } catch (IllegalArgumentException e) {
            assertTrue(false);
        }
        assertFalse(except);

        try {
            var.setLockedAt("foo");
        } catch (IllegalArgumentException ignored) {
        }
    }
}
