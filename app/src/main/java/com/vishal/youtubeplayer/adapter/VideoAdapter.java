package com.vishal.youtubeplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.vishal.youtubeplayer.R;
import com.vishal.youtubeplayer.model.VideoModel;
import com.vishal.youtubeplayer.ui.MainActivity;
import java.io.IOException;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

  private Context context;
  private ArrayList<VideoModel> videoList;

  public VideoAdapter(Context context,
      ArrayList<VideoModel> videoList) {
    this.context = context;
    this.videoList = videoList;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.row_item_video_data, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
    holder.tvTitle.setText(videoList.get(position).getTitle());
    holder.tvDescription.setText(videoList.get(position).getDescription());
    Glide.with(context).load(videoList.get(position).getThumb().replace("http://", "https://"))
        .into(holder.ivThumbnail);
    holder.itemView.setOnClickListener(view -> {
      try {
        ((MainActivity) (context)).playNextVideo(videoList.get(position).getSources());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

  }


  @Override
  public int getItemCount() {
    return videoList.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {

    RelativeLayout relativeLayout;
    TextView tvTitle, tvDescription;
    ImageView ivThumbnail;

    ViewHolder(@NonNull View itemView) {
      super(itemView);
      relativeLayout = itemView.findViewById(R.id.main_layout);
      ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
      tvTitle = itemView.findViewById(R.id.tvTitle);
      tvDescription = itemView.findViewById(R.id.tvDesc);
    }
  }

}
