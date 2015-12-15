package com.grafixartist.gallery;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

/**
 * Created by Clement on 11/30/2015.
 */
public class LoginTest extends ActivityInstrumentationTestCase2<Login> {

    private String username;
    private String password;
    private Instrumentation mInstrumentation;
    private Login mLogin;
    private final String PREFS_NAME = "MyPrefsFile";

    public LoginTest(){
        super(Login.class);
    }

    public void setUp() throws Exception{
        super.setUp();

        // Fetch shared preferences file
        SharedPreferences settings = getInstrumentation().getTargetContext().getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putBoolean("first_time_login", true).apply();

        mInstrumentation = getInstrumentation();
        mLogin = getActivity();

        username = "test@gmail.com";
        password = "test";

        mLogin.dh = new DatabaseHelper(mLogin);
        mLogin.dh.insert(username, password);
    }

    public void testPreconditions(){
        assertNotNull(mLogin);
    }

    @UiThreadTest
    public void testSuccessCheckLogin() throws Exception {
        mLogin.emailEditableField.setText("test@gmail.com");
        mLogin.passwordEditableField.setText("test");
        assertTrue(mLogin.checkLogin());
    }

    @UiThreadTest
    public void testFailureCheckLogin() throws Exception {
        mLogin.emailEditableField.setText("test@gmail.com");
        mLogin.passwordEditableField.setText("testt");
        assertFalse(mLogin.checkLogin());
    }

    protected void tearDown() throws Exception {
        mLogin.finish();
    }
}