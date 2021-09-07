package edu.lehigh.cse216.jbd321;

import android.content.Context;
import android.content.Intent;
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

class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    interface ClickListener{
        void onClick(Datum d);
    }
    private ClickListener mClickListener;
    ClickListener getClickListener() {return mClickListener;}
    void setClickListener(ClickListener c) { mClickListener = c;}

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mIndex;
        TextView mText;
        //TextView mVotes;
        TextView mUid;

        ViewHolder(View itemView) {
            super(itemView);
            this.mIndex = (TextView) itemView.findViewById(R.id.listItemIndex);
            this.mText = (TextView) itemView.findViewById(R.id.listItemText);
            //this.mVotes = (TextView) itemView.findViewById(R.id.listItemVotes);
            this.mUid = (TextView) itemView.findViewById(R.id.listItemUid);
        }
    }

    private ArrayList<Datum> mData;
    private LayoutInflater mLayoutInflater;
    Context c;
    MainActivity act;

    ItemListAdapter(Context context, ArrayList<Datum> data, MainActivity activity) {
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
        View view = mLayoutInflater.inflate(R.layout.list_item, null);

        User user = SharedPrefManager.getInstance(c).getUser();
        final int uid = user.getId();

        /*
         * Click on Vote button to upvote the message
         */
        Button like = (Button) view.findViewById(R.id.buttonUVote);//get the id for button
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup row = (ViewGroup) view.getParent();
                TextView listMid = (TextView) row.findViewById(R.id.listItemIndex);
                TextView listUid = (TextView) row.findViewById(R.id.listItemUid);
                Log.i("Upvote ID", listMid.getText().toString());
                String sMid = listMid.getText().toString(); //set the message id

//                Intent i = new Intent(c, MainActivity.class);
//                i.putExtra("messageId", sMid);
//                c.startActivity(i);

                //act.getVote("https://radiant-spire-42063.herokuapp.com/getmessages/" + uid);
                //act.addVote("https://radiant-spire-42063.herokuapp.com/messages/" + sMid); //upvote by 1
                act.addVote(sMid,"https://radiant-spire-42063.herokuapp.com");
                act.getVote(/*"https://radiant-spire-42063.herokuapp.com/getmessages/" + sMid,*/sMid);
                String text = "Upvote message by 1";
                Toast.makeText(c.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

        /*
         * Click on Vote button to downvote the message
         */
        Button dislike = (Button) view.findViewById(R.id.buttonDVote);//get the id for button
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup row = (ViewGroup) view.getParent();
                TextView listMid = (TextView) row.findViewById(R.id.listItemIndex);
                TextView listUid = (TextView) row.findViewById(R.id.listItemUid);
                Log.i("DownVote ID", listMid.getText().toString());
                String sMid = listMid.getText().toString(); //set the message id
                //act.downVote("https://radiant-spire-42063.herokuapp.com/messages/" + sMid); //downvote by 1
                act.downVote(sMid,"https://radiant-spire-42063.herokuapp.com");
                String text = "Downvote message by 1";
                Toast.makeText(c.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                act.getVote(/*"https://radiant-spire-42063.herokuapp.com/getmessages/" + sMid,*/ sMid);
            }
        });


        //show the comment for the message
        Button commentM = (Button) view.findViewById(R.id.buttonComment);//get the id for button
        commentM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup row = (ViewGroup) view.getParent();
                TextView listMid = (TextView) row.findViewById(R.id.listItemIndex);
                TextView listUid = (TextView) row.findViewById(R.id.listItemUid);
                //Log.d("Message ID",((TextView) view).getText().toString());
                String sMid = listMid.getText().toString(); //set the message id
                act.getComment(sMid);
                Toast.makeText(c.getApplicationContext(), "List of Comment for message " + sMid, Toast.LENGTH_SHORT).show();
            }
        });

        // get edittext component for adding one comment
        final EditText edittext = (EditText) view.findViewById(R.id.editText);
        // add a keylistener to keep track user input
         edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // if keydown and "enter" is pressed
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ViewGroup row = (ViewGroup) v.getParent();
                    TextView listMid = (TextView) row.findViewById(R.id.listItemIndex);
                    TextView listUid = (TextView) row.findViewById(R.id.listItemUid);
                    String sMid = listMid.getText().toString(); //set the message id
                    //String sUid = listUid.getText().toString(); //set the user id
                    //String str = sUid.substring(5,6); //get message's user id
                    int mid = Integer.parseInt(sMid);
                    //int uid = Integer.parseInt(str);
                    String comment = edittext.getText().toString();
                    //int cid = count + 1;
                    act.addComment(comment, "https://radiant-spire-42063.herokuapp.com/comments", mid, uid);
                    Toast.makeText(c.getApplicationContext(), "Comment: " + edittext.getText(), Toast.LENGTH_LONG).show(); // display a floating message
                    return true;
                }
                //test the function
                else if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_1)) {
                    // display a floating message
                    Toast.makeText(c.getApplicationContext(),
                            "Number 1 is pressed!", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
         });


        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Datum d = mData.get(position);
        holder.mIndex.setText(Integer.toString(d.mIndex));
        holder.mText.setText(d.mText);
        //holder.mVotes.setText("Votes: " + Integer.toString(d.mVotes));
        holder.mUid.setText("Uid: " + Integer.toString(d.mUid));

        // Attach a click listener to the view we are configuring
        final View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mClickListener.onClick(d);
            }
        };
        holder.mIndex.setOnClickListener(listener);
        holder.mText.setOnClickListener(listener);
        //holder.mVotes.setOnClickListener(listener);
        holder.mUid.setOnClickListener(listener);

    }
}