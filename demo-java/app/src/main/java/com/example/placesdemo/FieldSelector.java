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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.libraries.places.api.model.Place.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Helper class for selecting {@link Field} values. */
public final class FieldSelector {
  private static final String SELECTED_PLACE_FIELDS_KEY = "selected_place_fields";

  private final Map<Field, State> fieldStates;

  private final TextView outputView;

  /**
   * Returns all {@link Field} values except those passed in.
   *
   * <p>Convenience method for when most {@link Field} values are desired. Useful for APIs that do
   * no support all {@link Field} values.
   */
  static List<Field> allExcept(Field... placeFieldsToOmit) {
    // Arrays.asList is immutable, create a mutable list to allow removing fields
    List<Field> placeFields = new ArrayList<>(Arrays.asList(Field.values()));
    placeFields.removeAll(Arrays.asList(placeFieldsToOmit));

    return placeFields;
  }

  public FieldSelector(CheckBox enableView, TextView outputView, @Nullable Bundle savedState) {
    this(enableView, outputView, Arrays.asList(Field.values()), savedState);
  }

  public FieldSelector(
          CheckBox enableView,
          TextView outputView,
          List<Field> validFields,
          @Nullable Bundle savedState) {
    fieldStates = new HashMap<>();
    for (Field field : validFields) {
      fieldStates.put(field, new State(field));
    }

    if (savedState != null) {
      List<Integer> selectedFields = savedState.getIntegerArrayList(SELECTED_PLACE_FIELDS_KEY);
      if (selectedFields != null) {
        restoreState(selectedFields);
      }
      outputView.setText(getSelectedString());
    }

    outputView.setOnClickListener(
        v -> {
          if (v.isEnabled()) {
            showDialog(v.getContext());
          }
        });

    enableView.setOnClickListener(
            view -> {
              boolean isChecked = enableView.isChecked();
              outputView.setEnabled(isChecked);
              if (isChecked) {
                showDialog(view.getContext());
              } else {
                outputView.setText("");
                for (State state : fieldStates.values()) {
                  state.checked = false;
            }
          }
        });

    this.outputView = outputView;
  }

  /**
   * Shows dialog to allow user to select {@link Field} values they want.
   */
  public void showDialog(Context context) {
    ListView listView = new ListView(context);
    PlaceFieldArrayAdapter adapter = new PlaceFieldArrayAdapter(context, fieldStates.values());
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(adapter);

    new AlertDialog.Builder(context)
            .setTitle("Select Place Fields")
            .setPositiveButton(
                    "Done",
                    (dialog, which) -> {
                      outputView.setText(getSelectedString());
                    })
            .setView(listView)
            .show();
  }

  /**
   * Returns all {@link Field} that are selectable.
   */
  public List<Field> getAllFields() {
    return new ArrayList<>(fieldStates.keySet());
  }

  /**
   * Returns all {@link Field} values the user selected.
   */
  public List<Field> getSelectedFields() {
    List<Field> selectedList = new ArrayList<>();
    for (Map.Entry<Field, State> entry : fieldStates.entrySet()) {
      if (entry.getValue().checked) {
        selectedList.add(entry.getKey());
      }
    }

    return selectedList;
  }

  /**
   * Returns a String representation of all selected {@link Field} values. See {@link
   * #getSelectedFields()}.
   */
  public String getSelectedString() {
    StringBuilder builder = new StringBuilder();
    for (Field field : getSelectedFields()) {
      builder.append(field).append("\n");
    }

    return builder.toString();
  }


  public void onSaveInstanceState(Bundle bundle) {
    List<Field> fields = getSelectedFields();

    ArrayList<Integer> serializedFields = new ArrayList<>();
    for (Field field : fields) {
      serializedFields.add(field.ordinal());
    }
    bundle.putIntegerArrayList(SELECTED_PLACE_FIELDS_KEY, serializedFields);
  }

  private void restoreState(List<Integer> selectedFields) {
    for (Integer serializedField : selectedFields) {
      Field field = Field.values()[serializedField];
      State state = fieldStates.get(field);
      if (state != null) {
        state.checked = true;
      }
    }
  }

  //////////////////////////
  // Helper methods below //
  //////////////////////////

  /**
   * Holds selection state for a place field.
   */
  public static final class State {
    public final Field field;
    public boolean checked;

    public State(Field field) {
      this.field = field;
    }
  }

  private static final class PlaceFieldArrayAdapter extends ArrayAdapter<State>
      implements OnItemClickListener {

    public PlaceFieldArrayAdapter(Context context, Collection<State> states) {
      super(context, android.R.layout.simple_list_item_multiple_choice, new ArrayList<>(states));
    }

    private static void updateView(View view, State state) {
      if (view instanceof CheckedTextView) {
        CheckedTextView checkedTextView = (CheckedTextView) view;
        checkedTextView.setText(state.field.toString());
        checkedTextView.setChecked(state.checked);
      }
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      State state = getItem(position);
      updateView(view, state);

      return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      State state = getItem(position);
      state.checked = !state.checked;
      updateView(view, state);
    }
  }
}
