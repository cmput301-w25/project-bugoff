package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FollowingAdapterTest {

    private List<User> userList;
    private FollowingAdapter adapter;

    @Before
    public void setUp() {
        userList = new ArrayList<>();
        userList.add(new User("u1", "Alice", "alice@example.com", "https://example.com/alice.jpg"));
        userList.add(new User("u2", "Bob", "bob@example.com", "https://example.com/bob.jpg"));

        adapter = new FollowingAdapter(userList, "testUserId");
    }

    @Test
    public void testItemCount() {
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testAdapterNotNull() {
        assertNotNull(adapter);
    }

    @Test
    public void testItemCountAfterClear() {
        userList.clear();
        adapter = new FollowingAdapter(userList, "testUserId");
        assertEquals(0, adapter.getItemCount());
    }
}