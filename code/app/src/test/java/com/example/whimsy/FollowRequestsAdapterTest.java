package com.example.whimsy;

import static org.junit.Assert.assertEquals;

import android.graphics.Color;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FollowRequestsAdapterTest {

    private List<User> requests;
    private FollowRequestsAdapter adapter;

    @Before
    public void setUp() {
        requests = new ArrayList<>();
        requests.add(new User("u1", "Alice", "alice@example.com", ""));
        requests.add(new User("u2", "Bob", "bob@example.com", ""));

        adapter = new FollowRequestsAdapter(requests, Color.BLACK, null);
    }

    @Test
    public void testItemCount() {
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testEmptyList() {
        FollowRequestsAdapter emptyAdapter = new FollowRequestsAdapter(new ArrayList<>(), Color.BLACK, null);
        assertEquals(0, emptyAdapter.getItemCount());
    }
}