package com.example.whimsy;

import com.google.firebase.Timestamp;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommentTest {

    private Comment comment;
    private Timestamp timestamp;

    @Before
    public void setUp() {
        timestamp = new Timestamp(1678901234, 0);  // seconds, nanoseconds
        comment = new Comment(
                "user123",
                "John Doe",
                "Nice post!",
                timestamp,
                "https://example.com/profile.jpg"
        );
    }

    @Test
    public void testGetCommenterId() {
        assertEquals("user123", comment.getCommenterId());
    }

    @Test
    public void testSetCommenterId() {
        comment.setCommenterId("user456");
        assertEquals("user456", comment.getCommenterId());
    }

    @Test
    public void testGetCommenterName() {
        assertEquals("John Doe", comment.getCommenterName());
    }

    @Test
    public void testSetCommenterName() {
        comment.setCommenterName("Jane Smith");
        assertEquals("Jane Smith", comment.getCommenterName());
    }

    @Test
    public void testGetCommentText() {
        assertEquals("Nice post!", comment.getCommentText());
    }

    @Test
    public void testSetCommentText() {
        comment.setCommentText("Updated comment");
        assertEquals("Updated comment", comment.getCommentText());
    }

    @Test
    public void testGetTimestamp() {
        assertEquals(timestamp, comment.getTimestamp());
    }

    @Test
    public void testSetTimestamp() {
        Timestamp newTimestamp = new Timestamp(1678910000, 0);
        comment.setTimestamp(newTimestamp);
        assertEquals(newTimestamp, comment.getTimestamp());
    }

    @Test
    public void testGetProfileImageUrl() {
        assertEquals("https://example.com/profile.jpg", comment.getProfileImageUrl());
    }
}