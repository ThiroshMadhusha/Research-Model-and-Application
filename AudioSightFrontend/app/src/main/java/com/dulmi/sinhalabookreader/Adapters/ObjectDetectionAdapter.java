package com.dulmi.sinhalabookreader.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dulmi.sinhalabookreader.Interfaces.DetectionClickEvent;
import com.dulmi.sinhalabookreader.R;
import com.dulmi.sinhalabookreader.databinding.ObjectViewBinding;

import java.util.List;

public class ObjectDetectionAdapter extends RecyclerView.Adapter<ObjectDetectionAdapter.ObjectDetectionViewHolder> {

    private Context context;
    private List<String>objectList;
    private DetectionClickEvent clickEvent;

    public ObjectDetectionAdapter(Context context, List<String> objectList, DetectionClickEvent clickEvent) {
        this.context = context;
        this.objectList = objectList;
        this.clickEvent = clickEvent;
    }

    @NonNull
    @Override
    public ObjectDetectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ObjectDetectionViewHolder(LayoutInflater.from(context).inflate(R.layout.object_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectDetectionViewHolder holder, int position) {

        holder.binding.detectedObjectText.setText(objectList.get(position));

    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    public class ObjectDetectionViewHolder extends RecyclerView.ViewHolder {

        public ObjectViewBinding binding;

        public ObjectDetectionViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ObjectViewBinding.bind(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickEvent.onObjectClick(getAdapterPosition());
                }
            });

        }
    }

}
