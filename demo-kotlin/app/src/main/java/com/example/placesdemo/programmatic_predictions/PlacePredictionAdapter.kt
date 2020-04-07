package com.example.placesdemo.programmatic_predictions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.placesdemo.R
import com.example.placesdemo.programmatic_predictions.PlacePredictionAdapter.PlacePredictionViewHolder
import com.google.android.libraries.places.api.model.AutocompletePrediction
import java.util.*

/**
 * A [RecyclerView.Adapter] for a [com.google.android.libraries.places.api.model.AutocompletePrediction].
 */
class PlacePredictionAdapter : RecyclerView.Adapter<PlacePredictionViewHolder>() {
    private val predictions: MutableList<AutocompletePrediction> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacePredictionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlacePredictionViewHolder(
            inflater.inflate(R.layout.place_prediction_item, parent, false))
    }

    override fun onBindViewHolder(holder: PlacePredictionViewHolder, position: Int) {
        holder.setPrediction(predictions[position])
    }

    override fun getItemCount(): Int {
        return predictions.size
    }

    fun setPredictions(predictions: List<AutocompletePrediction>?) {
        this.predictions.clear()
        this.predictions.addAll(predictions!!)
        notifyDataSetChanged()
    }

    class PlacePredictionViewHolder(itemView: View) : ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_view_title)
        private val address: TextView = itemView.findViewById(R.id.text_view_address)

        fun setPrediction(prediction: AutocompletePrediction) {
            title.text = prediction.getPrimaryText(null)
            address.text = prediction.getSecondaryText(null)
        }
    }
}