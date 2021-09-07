package edu.lehigh.cse216.jbd321;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class ProfileMsgAdapter extends RecyclerView.Adapter<ProfileMsgAdapter.ViewHolder> {

    interface ClickListener{
        void onClick(Datum d);
    }
    private ClickListener mClickListener;
    ClickListener getClickListener() {return mClickListener;}
    void setClickListener(ClickListener c) { mClickListener = c;}

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;

        ViewHolder(View itemView) {
            super(itemView);
            this.mText = (TextView) itemView.findViewById(R.id.listItemText);
        }
    }

    private ArrayList<Datum> mData;
    private LayoutInflater mLayoutInflater;
    Context c;
    ProfileActivity act;

    ProfileMsgAdapter(Context context, ArrayList<Datum> data, ProfileActivity activity) {
        act = activity;
        c = context;
        mData = data;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.profile_list, null);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Datum d = mData.get(position);
        holder.mText.setText(d.mText);

        // Attach a click listener to the view we are configuring
        final View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mClickListener.onClick(d);
            }
        };
        holder.mText.setOnClickListener(listener);

    }
}