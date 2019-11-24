package com.fed.androidschool_dictaphone;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordsHolder> {

    private static final int COLOR_CHOSEN = Color.CYAN;

    private File mDir;

    private int mChosen = 0;
    private MainActivity.OnChosenChange mOnChosenChange;

    RecordsAdapter(File dir, MainActivity.OnChosenChange onChosenChange) {
        mDir = new File(dir.getAbsolutePath());
        mOnChosenChange = onChosenChange;
    }

    void updateData(File dir) {
        mDir = new File(dir.getAbsolutePath());
        notifyDataSetChanged();
    }

    MainActivity.OnGetChosenRecord getChosenRecord() {
        return new MainActivity.OnGetChosenRecord() {
            @Override
            public int getChosen() {
                return mChosen;
            }

            @Override
            public void changeChosen(int delta) {
                if (mChosen + delta < getItemCount() && mChosen + delta >= 0) {
                    mChosen += delta;
                    notifyDataSetChanged();
                }
            }
        };
    }

    @NonNull
    @Override
    public RecordsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return
                new RecordsHolder(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.record_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsHolder holder, int position) {
        holder.bind(position);
    }


    @Override
    public int getItemCount() {
        return mDir.list().length;
    }

    class RecordsHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        RecordsHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_view_name);

        }


        void bind(final int position) {
            mTextView.setText(mDir.list()[position]);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mChosen != position) {
                        mChosen = position;
                        mOnChosenChange.onChange();
                        RecordsAdapter.this.notifyDataSetChanged();
                    }
                }
            });
            if (position == mChosen) {
                mTextView.setBackgroundColor(COLOR_CHOSEN);
            } else {
                mTextView.setBackgroundColor(Color.WHITE);
            }
        }
    }


}
