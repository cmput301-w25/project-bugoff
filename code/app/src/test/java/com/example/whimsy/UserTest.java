package com.example.whimsy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserTest {

    private User user;

    @Before
    public void setUp() {
        // Corrected order: id, name (display name), username, profilePictureUrl
        user = new User("user123", "Test User", "testuser", "https://example.com/profile.jpg");
    }

    @Test
    public void testDefaultConstructor() {
        User emptyUser = new User();
        assertNull("ID should be null with default constructor", emptyUser.getId());
        assertNull("Name should be null with default constructor", emptyUser.getName());
        assertNull("Username should be null with default constructor", emptyUser.getUsername());
        assertNull("Profile picture URL should be null with default constructor", emptyUser.getProfilePictureUrl());
        assertNull("Gmail should be null with default constructor", emptyUser.getGmail());
    }

    @Test
    public void testFourArgConstructor() {
        assertEquals("ID should match constructor value", "user123", user.getId());
        assertEquals("Name should match constructor value", "Test User", user.getName());
        assertEquals("Username should match constructor value", "testuser", user.getUsername());
        assertEquals("Profile picture URL should match constructor value", "https://example.com/profile.jpg", user.getProfilePictureUrl());
        assertNull("Gmail should be null by default", user.getGmail());
    }

    @Test
    public void testThreeArgConstructor() {
        User threeArgUser = new User("user456", "anotheruser", "Another User");
        assertEquals("ID should match constructor value", "user456", threeArgUser.getId());
        assertEquals("Name should match constructor value", "Another User", threeArgUser.getName());
        assertEquals("Username should match constructor value", "anotheruser", threeArgUser.getUsername());
        assertNull("Profile picture URL should be null", threeArgUser.getProfilePictureUrl());
        assertNull("Gmail should be null by default", threeArgUser.getGmail());
    }

    @Test
    public void testSettersAndGetters() {
        user.setId("newId");
        assertEquals("ID should update with setter", "newId", user.getId());

        user.setName("New Name");
        assertEquals("Name should update with setter", "New Name", user.getName());

        user.setUsername("newusername");
        assertEquals("Username should update with setter", "newusername", user.getUsername());

        user.setProfilePictureUrl("https://newurl.com/pic.jpg");
        assertEquals("Profile picture URL should update with setter", "https://newurl.com/pic.jpg", user.getProfilePictureUrl());

        user.setGmail("test@gmail.com");
        assertEquals("Gmail should update with setter", "test@gmail.com", user.getGmail());
    }

    @Test
    public void testNullValues() {
        User nullUser = new User(null, null, null, null);
        assertNull("ID should be null", nullUser.getId());
        assertNull("Name should be null", nullUser.getName());
        assertNull("Username should be null", nullUser.getUsername());
        assertNull("Profile picture URL should be null", nullUser.getProfilePictureUrl());

        nullUser.setGmail(null);
        assertNull("Gmail should be null", nullUser.getGmail());
    }
}