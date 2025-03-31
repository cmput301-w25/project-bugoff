package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class FollowRequestsAdapterTest {

    private List<DocumentSnapshot> requestDocs;
    private FollowRequestsAdapter.RequestActionListener mockListener;
    private FollowRequestsAdapter adapter;

    @Before
    public void setUp() {
        requestDocs = new ArrayList<>();

        mockListener = new FollowRequestsAdapter.RequestActionListener() {
            @Override
            public void onAccept(DocumentSnapshot requestDoc) {
                // no-op for testing
            }

            @Override
            public void onReject(DocumentSnapshot requestDoc) {
                // no-op for testing
            }
        };

        adapter = new FollowRequestsAdapter(requestDocs, mockListener);
    }

    @Test
    public void testGetItemCount_emptyList_returnsZero() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAdapterNotNull() {
        assertNotNull(adapter);
    }


}