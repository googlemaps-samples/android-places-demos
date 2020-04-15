package com.example.placesdemo.programmatic_predictions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.placesdemo.R;
import com.example.placesdemo.programmatic_predictions.PlacePredictionAdapter.PlacePredictionViewHolder;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link RecyclerView.Adapter} for a {@link com.google.android.libraries.places.api.model.AutocompletePrediction}.
 */
public class PlacePredictionAdapter extends RecyclerView.Adapter<PlacePredictionViewHolder> {

    private final List<AutocompletePrediction> predictions = new ArrayList<>();

    @NonNull
    @Override
    public PlacePredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PlacePredictionViewHolder(
            inflater.inflate(R.layout.place_prediction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlacePredictionViewHolder holder, int position) {
        holder.setPrediction(predictions.get(position));
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public void setPredictions(List<AutocompletePrediction> predictions) {
        this.predictions.clear();
        this.predictions.addAll(predictions);
        notifyDataSetChanged();
    }

    public static class PlacePredictionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView address;

        public PlacePredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title);
            address = itemView.findViewById(R.id.text_view_address);
        }

        public void setPrediction(AutocompletePrediction prediction) {
            title.setText(prediction.getPrimaryText(null));
            address.setText(prediction.getSecondaryText(null));
        }
    }
}
