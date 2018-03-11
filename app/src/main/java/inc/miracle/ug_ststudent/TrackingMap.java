package inc.miracle.ug_ststudent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

@SuppressLint("Registered")
public class TrackingMap extends FragmentActivity implements OnMapReadyCallback{
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    private static final String TAG = MainActivity.class.getSimpleName();
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    public Marker searchMarker;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    Dialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_content);
        alertDialog = new Dialog(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    PlaceAutocompleteFragment autocompleteFragment;
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {
            // TODO: Get info about the selected place.
               if (searchMarker == null) {
                   searchMarker = mMap.addMarker(new MarkerOptions()
                           .title(place.getName().toString())
                           .position((place.getLatLng()))
                           .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_student_icon))
                    );
                   searchMarker.setVisible(true);
                   searchMarker.showInfoWindow();
                   mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));

               } else {

                   searchMarker.remove();
                   searchMarker = mMap.addMarker(new MarkerOptions()
                           .title(place.getName().toString())
                           .position((place.getLatLng()))
                           .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_student_icon))
                   );
                   searchMarker.setVisible(true);
                   searchMarker.showInfoWindow();
                   mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));

               }

            Log.i(TAG, "Place: " + place.getName());
        }

        @Override
        public void onError(Status status) {
            // TODO: Handle the error.
            Log.i(TAG, "An error occurred: " + status);
        }
    });

    autocompleteFragment.setBoundsBias(new LatLngBounds(
            new LatLng(5.6504881, -0.205416),
            new LatLng(5.6505026, -0.180481)));

    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
            .setCountry("GH")
            .build();

    autocompleteFragment.setFilter(typeFilter);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Authenticate with Firebase when the Google map is loaded
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);
        loginToFirebase();

    }


    private void loginToFirebase() {
        String email = getString(R.string.firebase_email);
        String password = getString(R.string.firebase_password);
        // Authenticate with Firebase and subscribe to updates
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    subscribeToUpdates();
                    Log.d(TAG, "firebase auth success");
                } else {
                    Log.d(TAG, "firebase auth failed");
                }
            }
        });
    }

    private void subscribeToUpdates() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_path));
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                setMarker(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void setMarker(DataSnapshot dataSnapshot) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        String key = dataSnapshot.getKey();
        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();
        assert value != null;
        double lat = Double.parseDouble(value.get("latitude").toString());
        double lng = Double.parseDouble(value.get("longitude").toString());
        LatLng location = new LatLng(lat, lng);
        if (!mMarkers.containsKey(key)) {
            mMarkers.put(key, mMap.addMarker(new MarkerOptions()
                    .title(key)
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_icon))
                      ));
        } else {
            mMarkers.get(key).setPosition(location);
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());

        }
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public void shuttleView(View v) {

    }

    public void SearchedLocation(View v) {

    }


    public void alertDialog(View v) {
        //alert dialog when the image is clicked

        Switch alertSwitch;
        final Spinner alertSpinner;
        final TextView alertText;

        alertDialog.setContentView(R.layout.alert_dialog);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        alertSwitch = alertDialog.findViewById(R.id.alertSwitch);
        alertSpinner = alertDialog.findViewById(R.id.alertSpinner);
        alertText = alertDialog.findViewById(R.id.alertText);



        alertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    alertText.setVisibility(View.VISIBLE);
                    alertSpinner.setVisibility(View.VISIBLE);

                } else {
                    alertText.setVisibility(View.GONE);
                    alertSpinner.setVisibility(View.GONE);
                }
            }
        });

        alertDialog.show();

    }
}
