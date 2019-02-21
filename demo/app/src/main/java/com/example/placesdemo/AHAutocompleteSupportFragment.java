package com.example.placesdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AHAutocompleteSupportFragment extends AutocompleteSupportFragment  {

    List<Place.Field> places = new ArrayList<>();
    int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        places.add(Place.Field.ID);
        places.add(Place.Field.NAME);
        places.add(Place.Field.ADDRESS);
        this.setPlaceFields(places);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, places)
                        .build(view.getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

            }
        });
    }

}

