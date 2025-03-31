package com.example.whimsy;

import static org.junit.Assert.assertEquals;

import android.graphics.Color;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FollowingAdapterTest {

    private List<User> followingList;
    private FollowingAdapter adapter;

    @Before
    public void setUp() {
        followingList = new ArrayList<>();
        followingList.add(new User("u1", "Alice"));
        followingList.add(new User("u2", "Bob"));

        adapter = new FollowingAdapter(followingList, Color.BLACK, null);
    }

    @Test
    public void testItemCount() {
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testEmptyList() {
        FollowingAdapter emptyAdapter = new FollowingAdapter(new ArrayList<>(), Color.BLACK, null);
        assertEquals(0, emptyAdapter.getItemCount());
    }
}