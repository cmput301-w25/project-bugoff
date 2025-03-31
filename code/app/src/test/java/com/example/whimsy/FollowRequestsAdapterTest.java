package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FollowRequestsAdapterTest {

    private List<DocumentSnapshot> requestDocs;
    private FollowRequestsAdapter.RequestActionListener mockListener;
    private FollowRequestsAdapter adapter;

    @Before
    public void setUp() {
        // Empty list of DocumentSnapshots for now — Firebase cannot be unit tested directly
        requestDocs = new ArrayList<>();

        // Mock listener (no-op for unit testing)
        mockListener = new FollowRequestsAdapter.RequestActionListener() {
            @Override
            public void onAccept(DocumentSnapshot requestDoc) {
                // no-op
            }

            @Override
            public void onReject(DocumentSnapshot requestDoc) {
                // no-op
            }
        };

        adapter = new FollowRequestsAdapter(requestDocs, mockListener);
    }



}