package com.grafixartist.gallery;

import junit.framework.TestCase;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Patterns;
import java.util.regex.Pattern;

/**
 * Created by Clement on 11/30/2015.
 */
public class ChangeEmailTest extends ActivityInstrumentationTestCase2<ChangeEmail> {

    private ChangeEmail changeEmailActivity;
    private Instrumentation changeEmailActivityInstrumentation = null;
    private static final String VALID_EMAIL = "test@gmail.com";
    private static final String INVALID_EMAIL = "testt";

    public ChangeEmailTest(){
        super(ChangeEmail.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        changeEmailActivityInstrumentation = getInstrumentation();
        changeEmailActivity = getActivity();
    }

    public void testPreconditions(){
        assertNotNull(changeEmailActivity);
    }

    public void testIsValidEmailTrue() throws Exception {
        assertTrue(changeEmailActivity.isValidEmail(VALID_EMAIL));
    }

    public void testIsValidEmailFalse() throws Exception {
        assertFalse(changeEmailActivity.isValidEmail(INVALID_EMAIL));
    }

    protected void tearDown() throws Exception {
        changeEmailActivity.finish();
    }
}