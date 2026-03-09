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
package com.example.placesdemo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.libraries.places.api.model.Place
import java.util.*

/** Helper class for selecting [Place.Field] values.  */
class FieldSelector(
    enableView: CheckBox,
    outputView: TextView,
    savedState: Bundle?,
    validFields: List<Place.Field> = Place.Field.entries
) {

    private val fieldStates: MutableMap<Place.Field, State> = EnumMap(Place.Field::class.java)
    private val outputView: TextView

    /**
     * Shows dialog to allow user to select [Place.Field] values they want.
     */
    private fun showDialog(context: Context?) {
        val listView = ListView(context)
        val adapter = PlaceFieldArrayAdapter(context, fieldStates.values.toList())
        listView.adapter = adapter
        listView.onItemClickListener = adapter
        AlertDialog.Builder(context!!)
            .setTitle("Select Place Fields")
            .setPositiveButton(
                "Done"
            ) { _, _ -> outputView.text = selectedString }
            .setView(listView)
            .show()
    }

    /**
     * Returns all [Place.Field] that are selectable.
     */
    val allFields: List<Place.Field>
        get() = ArrayList(fieldStates.keys)

    /**
     * Returns all [Place.Field] values the user selected.
     */
    val selectedFields: List<Place.Field>
        get() {
            val selectedList: MutableList<Place.Field> = ArrayList()
            for ((key, value) in fieldStates) {
                if (value.checked) {
                    selectedList.add(key)
                }
            }
            return selectedList
        }

    /**
     * Returns a String representation of all selected [Place.Field] values. See [ ][.getSelectedFields].
     */
    val selectedString: String
        get() {
            val builder = StringBuilder()
            for (eachField in selectedFields) {
                builder.append(eachField).append("\n")
            }
            return builder.toString()
        }

    fun onSaveInstanceState(bundle: Bundle) {
        val fields = selectedFields
        val serializedFields = ArrayList<Int>()
        for (field in fields) {
            serializedFields.add(field.ordinal)
        }
        bundle.putIntegerArrayList(SELECTED_PLACE_FIELDS_KEY, serializedFields)
    }

    private fun restoreState(selectedFields: List<Int>) {
        for (serializedField in selectedFields) {
            val field = Place.Field.entries[serializedField]
            val state = fieldStates[field]
            if (state != null) {
                state.checked = true
            }
        }
    }
    //////////////////////////
    // Helper methods below //
    //////////////////////////
    /**
     * Holds selection state for a place field.
     */
    class State(val field: Place.Field) {
        var checked = false
    }

    private class PlaceFieldArrayAdapter(
        context: Context?,
        states: List<State>?
    ) : ArrayAdapter<State>(
        context!!,
        android.R.layout.simple_list_item_multiple_choice,
        states!!.toMutableList()
    ), OnItemClickListener {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val state = getItem(position)
            updateView(view, state)
            return view
        }

        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            val state = getItem(position)
            state!!.checked = !state.checked
            updateView(view, state)
        }

        companion object {
            private fun updateView(view: View, state: State?) {
                if (view is CheckedTextView && state != null) {
                    view.text = state.field.toString()
                    view.isChecked = state.checked
                }
            }
        }
    }

    companion object {
        private const val SELECTED_PLACE_FIELDS_KEY = "selected_place_fields"

        /**
         * Returns all [Place.Field] values except those passed in.
         * <p>
         * Convenience method for when most [Place.Field] values are desired. Useful for APIs that do
         * no support all [Place.Field] values.
         */
        fun allExcept(vararg placeFieldsToOmit: Place.Field): List<Place.Field> {
            // Arrays.asList is immutable, create a mutable list to allow removing fields
            val placeFields: MutableList<Place.Field> = ArrayList(listOf(*Place.Field.entries.toTypedArray()))
            placeFields.removeAll(placeFieldsToOmit)
            return placeFields
        }
    }

    init {
        for (field in validFields) {
            fieldStates[field] = State(field)
        }
        if (savedState != null) {
            val selectedFields: List<Int>? = savedState.getIntegerArrayList(SELECTED_PLACE_FIELDS_KEY)
            selectedFields?.let { restoreState(it) }
            outputView.text = selectedString
        }
        outputView.setOnClickListener { v: View ->
            if (v.isEnabled) {
                showDialog(v.context)
            }
        }
        enableView.setOnClickListener { view: View ->
            val isChecked = enableView.isChecked
            outputView.isEnabled = isChecked
            if (isChecked) {
                showDialog(view.context)
            } else {
                outputView.text = ""
                for (state in fieldStates.values) {
                    state.checked = false
                }
            }
        }
        this.outputView = outputView
    }
}