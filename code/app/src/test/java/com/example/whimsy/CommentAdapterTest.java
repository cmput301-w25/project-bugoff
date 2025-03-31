package com.example.whimsy;

import android.graphics.Color;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CommentAdapterTest {

    private List<Comment> comments;
    private CommentAdapter adapter;

    @Before
    public void setUp() {
        comments = new ArrayList<>();
        comments.add(new Comment("u1", "Alice", "Nice!", Timestamp.now(), ""));
        comments.add(new Comment("u2", "Bob", "Great post!", Timestamp.now(), ""));
        comments.add(new Comment("u3", "", "", null, null));  // edge case: empty name/text
        comments.add(new Comment("u4", null, null, null, null));  // edge case: nulls
        adapter = new CommentAdapter(comments, Color.BLACK);
    }

    @Test
    public void testGetItemCount() {
        assertEquals(4, adapter.getItemCount());
    }

    @Test
    public void testGetItemCountEmptyList() {
        CommentAdapter emptyAdapter = new CommentAdapter(new ArrayList<>(), Color.BLACK);
        assertEquals(0, emptyAdapter.getItemCount());
    }

}