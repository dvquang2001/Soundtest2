package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.model.Audio;
import com.example.soundtest2.R;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder>{

    private Context mContext;
    private List<Audio> mListAudio;
    ClickItemListener iClickItemListener;

    public AudioAdapter(Context mContext,ClickItemListener listener) {
        this.mContext = mContext;
        this.iClickItemListener = listener;
    }

    public void setData(List<Audio> list) {
        this.mListAudio = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Audio audio = mListAudio.get(position);
        if(audio == null)
        {
            return;
        }

        holder.btnIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                iClickItemListener.onClickItem(holder.getAdapterPosition());
            }
        });
    }

    //    @Override
//    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
//        Audio audio = mListAudio.get(position);
//       if(audio == null) {
//           return;
//       }
//
//       holder.btnIcon.setOnClickListener(new View.OnClickListener() {
//           @Override
//           public void onClick(View v) {
//               iClickItemListener.onClikItem(position);
//           }
//       });
//    }



    @Override
    public int getItemCount() {
        if(mListAudio != null) {
            return mListAudio.size();
        }
        return 0;
    }

    public class AudioViewHolder  extends RecyclerView.ViewHolder {

        private ImageButton btnIcon;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            btnIcon = itemView.findViewById(R.id.btnIcon);
        }
    }
}
