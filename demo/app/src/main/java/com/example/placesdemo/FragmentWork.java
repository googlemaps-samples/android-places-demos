package com.example.placesdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentWork extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AHAutocompleteSupportFragment mAutocompleteFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                // fragment to inflate
                R.layout.fragment_enter_work_address,
                // container to inflate into
                container,
                // always false
                false
        );


        Button okButton = rootView.findViewById(R.id.button_ok);
        okButton.setText("OK");
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        okButton.setEnabled(false);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getContext());
        // Add a listener to handle the response.

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Activity activity = getActivity();
        if (activity == null) { // No Activity in a ui lifecycle event handler? (?)
            return;             // Don't setup the map or address search.
        }

        // Check the childFragmentManager to see if these fragments already exist (in the case this activity was recreated). If not, create them.
        mAutocompleteFragment = (AHAutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        FragmentManager supportFragmentManager = getChildFragmentManager();
        FragmentTransaction ft = supportFragmentManager.beginTransaction();
        if(mAutocompleteFragment == null) {
            mAutocompleteFragment = new AHAutocompleteSupportFragment();
            ft.add(R.id.places_container, mAutocompleteFragment, AutocompleteSupportFragment.class.getSimpleName());

        }

        // Let's setup the map only if this device has an up to date Google Play Services.
        if (mMapFragment == null ) {
            mMapFragment = new SupportMapFragment();
            ft.add(R.id.map_container, mMapFragment, SupportMapFragment.class.getSimpleName());
        }
        ft.commit();

        // Setup map if we created a map fragment:
        if (mMapFragment != null) {
            mMapFragment.getMapAsync(this);
        }

        // Now, configure those fragments
        //
        mAutocompleteFragment.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        Log.d(getClass().getSimpleName(),"OnPlaceSelected");

                    }

                    @Override
                    public void onError(Status status) {

                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int reasonID) {
                if (reasonID == REASON_API_ANIMATION || reasonID == REASON_GESTURE) {

                }
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

            }
        });

    }
}
