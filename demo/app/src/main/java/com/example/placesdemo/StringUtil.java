/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.placesdemo;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Utility class for converting objects to viewable strings and back.
 */
public final class StringUtil {

  private static final String FIELD_SEPARATOR = "\n\t";
  private static final String RESULT_SEPARATOR = "\n---\n\t";

  static void prepend(TextView textView, String prefix) {
    textView.setText(prefix + "\n\n" + textView.getText());
  }

  @Nullable
  static LatLngBounds convertToLatLngBounds(
      @Nullable String southWest, @Nullable String northEast) {
    LatLng soundWestLatLng = convertToLatLng(southWest);
    LatLng northEastLatLng = convertToLatLng(northEast);
    if (soundWestLatLng == null || northEast == null) {
      return null;
    }

    return new LatLngBounds(soundWestLatLng, northEastLatLng);
  }

  @Nullable
  static LatLng convertToLatLng(@Nullable String value) {
    if (TextUtils.isEmpty(value)) {
      return null;
    }

    List<String> split = Splitter.on(',').splitToList(value);
    if (split.size() != 2) {
      return null;
    }

    try {
      return new LatLng(Double.parseDouble(split.get(0)), Double.parseDouble(split.get(1)));
    } catch (NullPointerException | NumberFormatException e) {
      return null;
    }
  }

  static String stringify(FindAutocompletePredictionsResponse response, boolean raw) {
    StringBuilder builder = new StringBuilder();

    builder
        .append(response.getAutocompletePredictions().size())
        .append(" Autocomplete Predictions Results:");

    if (raw) {
      builder.append(RESULT_SEPARATOR);
      Joiner.on(RESULT_SEPARATOR)
          .useForNull("-")
          .appendTo(builder, response.getAutocompletePredictions());
    } else {
      for (AutocompletePrediction autocompletePrediction : response.getAutocompletePredictions()) {
        builder
            .append(RESULT_SEPARATOR)
            .append(autocompletePrediction.getFullText(/* matchStyle */ null));
      }
    }

    return builder.toString();
  }

  static String stringify(FetchPlaceResponse response, boolean raw) {
    StringBuilder builder = new StringBuilder();

    builder.append("Fetch Place Result:").append(RESULT_SEPARATOR);
    if (raw) {
      builder.append(response.getPlace());
    } else {
      builder.append(stringify(response.getPlace()));
    }

    return builder.toString();
  }

  static String stringify(FindCurrentPlaceResponse response, boolean raw) {
    StringBuilder builder = new StringBuilder();

    builder.append(response.getPlaceLikelihoods().size()).append(" Current Place Results:");

    if (raw) {
      builder.append(RESULT_SEPARATOR);
      Joiner.on(RESULT_SEPARATOR).useForNull("-").appendTo(builder, response.getPlaceLikelihoods());
    } else {
      for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
        builder
            .append(RESULT_SEPARATOR)
            .append("Likelihood: ")
            .append(placeLikelihood.getLikelihood())
            .append(FIELD_SEPARATOR)
            .append("Place: ")
            .append(stringify(placeLikelihood.getPlace()));
      }
    }

    return builder.toString();
  }

  static String stringify(Place place) {
    return place.getName() + " (" + place.getAddress() + ")";
  }

  static String stringify(Bitmap bitmap) {
    StringBuilder builder = new StringBuilder();

    builder
        .append("Photo size (width x height)")
        .append(RESULT_SEPARATOR)
        .append(bitmap.getWidth())
        .append(", ")
        .append(bitmap.getHeight());

    return builder.toString();
  }

  public static String stringifyAutocompleteWidget(Place place, boolean raw) {
    StringBuilder builder = new StringBuilder();

    builder.append("Autocomplete Widget Result:").append(RESULT_SEPARATOR);

    if (raw) {
      builder.append(place);
    } else {
      builder.append(stringify(place));
    }

    return builder.toString();
  }
}
