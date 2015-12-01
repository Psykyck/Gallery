package com.grafixartist.gallery;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Clement on 11/30/2015.
 */
public class ChangeEmailTest extends TestCase {
    @Test
    public void testIsValidEmailTrue() throws Exception {
        assertTrue(ChangeEmail.isValidEmail("test@gmail.com"));
    }

    @Test
    public void testIsValidEmailFalse() throws Exception {
        assertFalse(ChangeEmail.isValidEmail("test"));
    }
}