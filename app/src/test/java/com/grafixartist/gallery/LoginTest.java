package com.grafixartist.gallery;

import android.widget.EditText;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import java.util.regex.Pattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.junit.Assert.*;

/**
 * Created by Clement on 11/30/2015.
 */
public class LoginTest {

    private String username;
    private String password;
    private Login user;

    @Before
    public void accountSetup(){
        username = "test@gmail.com";
        password = "test";
        user = new Login();
        user.dh = new DatabaseHelper(user);
        user.dh.insert(username, password);
    }

    @Test
    public void testSuccessCheckLogin() throws Exception {
        user.emailEditableField.setText("test@gmail.com");
        user.passwordEditableField.setText("test");
        assertTrue(user.checkLogin());
    }

    @Test
    public void testFailureCheckLogin() throws Exception {
        user.emailEditableField.setText("test@gmail.com");
        user.passwordEditableField.setText("testt");
        assertFalse(user.checkLogin());
    }
}