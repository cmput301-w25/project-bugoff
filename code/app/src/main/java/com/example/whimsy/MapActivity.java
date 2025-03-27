package com.example.whimsy;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String currentUserId;
    private List<String> followedUserIds = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapActivity", "Error: Map fragment is null");
        }

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                        } else {
                            LatLng northAmerica = new LatLng(37.0902, -95.7129);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(northAmerica, 5));
                        }
                    });
        }

        if (currentUserId != null && !currentUserId.isEmpty()) {
            fetchFollowedUsers();
            fetchCurrentUserMoods();
        } else {
            Log.e("MapActivity", "Error: currentUserId is not initialized");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                Toast.makeText(this, "Permission denied! Showing default location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchFollowedUsers() {
        db.collection("users").document(currentUserId).collection("following")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String userId = doc.getId();
                        followedUserIds.add(userId);
                    }
                    fetchLatestMoodsOfFollowedUsers();
                })
                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching followed users", e));
    }

    private void fetchLatestMoodsOfFollowedUsers() {
        for (String userId : followedUserIds) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String username = userDoc.getString("name");
                        String profilePictureUrl = userDoc.getString("profilePictureUrl");

                        db.collection("users").document(userId).collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        showMoodsOnMap(querySnapshot, username, profilePictureUrl);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching moods", e));
                    })
                    .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching user details", e));
        }
    }

    private void fetchCurrentUserMoods() {
        db.collection("users").document(currentUserId).collection("moods")
                .whereNotEqualTo("locationLat", null)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    showMoodsOnMap(querySnapshot, "You", null);
                })
                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching current user moods", e));
    }

    private void loadProfilePictureMarker(String imageUrl, LatLng location, String username, String mood) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .circleCrop()
                .into(new CustomTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(username + " - " + mood)
                                .icon(icon));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void showMoodsOnMap(QuerySnapshot querySnapshot, String username, String profilePictureUrl) {
        int index = 0;

        for (QueryDocumentSnapshot doc : querySnapshot) {
            double lat = doc.getDouble("locationLat");
            double lng = doc.getDouble("locationLng");
            String mood = doc.getString("mood");

            double offset = 0.0002 * (index % 5);
            LatLng location = new LatLng(lat + offset, lng + offset);

            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                loadProfilePictureMarker(profilePictureUrl, location, username, mood);
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(username + " - " + mood)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            index++;
        }
    }
}
