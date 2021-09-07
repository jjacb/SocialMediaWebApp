package edu.lehigh.cse216.jbd321;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * mData holds the data we get from Volley
     */
    ArrayList<Datum> mData = new ArrayList<>();

    /**
     * mMessages holds the list of messages
     */
    static ArrayList<String>mMessages = new ArrayList<>();


    //ArrayList<CommentData> cData = new ArrayList<>();
    //static ArrayList<String>mComments = new ArrayList<>();

    //get the session key
    User user = SharedPrefManager.getInstance(this).getUser();
    String sessionK = user.getUsername();
    int uid = user.getId();

    private void populateListFromVolley(JSONObject response){
        try {
            /**
             * Created an array to hold the messages from the Database
             */
            //JSONObject jsonObj = new JSONObject(response);
            //JSONArray jsonData = jsonObj.getJSONArray("mData");
            JSONArray jsonData = response.getJSONArray("mData");

            for (int i = 0; i < jsonData.length(); ++i) {
                String str = jsonData.getJSONObject(i).getString("mMessage");
                //int votes = jsonData.getJSONObject(i).getInt("votes");
                int mId = jsonData.getJSONObject(i).getInt("mMid");
                int uId = jsonData.getJSONObject(i).getInt("mUid");
                mData.add(new Datum(mId,str,uId));
                //String comment = jsonData.getJSONObject(i).getString("mComment");
            }
            Log.d("Message Size", Integer.toString(mMessages.size()));
            Log.d("Data Size", Integer.toString(mData.size()));

        } catch (final JSONException e) {
            Log.d("jbd321", "Error parsing JSON file: " + e.getMessage());
            return;
        }

        Log.d("Message Size", Integer.toString(mMessages.size()));
        Log.d("jbd321", "Successfully parsed JSON file.");
        RecyclerView rv = (RecyclerView) findViewById(R.id.datum_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ItemListAdapter adapter = new ItemListAdapter(this, mData, MainActivity.this);
        rv.setAdapter(adapter);
        adapter.setClickListener(new ItemListAdapter.ClickListener() {
            @Override
            public void onClick(Datum d) {
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                i.putExtra("label_contents", "User Profile for user " + d.mUid);
                i.putExtra("uid", d.mUid);
                i.putExtra("session", sessionK);
                startActivityForResult(i, 678);
                //Toast.makeText(MainActivity.this,  " Message id:" + d.mIndex + " --> " + d.mText, Toast.LENGTH_LONG).show();
                //getVote(d);
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("jbd231", "Debug Message from onCreate");
        // Instantiate the RequestQueue.
        //RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        // Request a JsonObject response from the provided URL.
        String url = "https://radiant-spire-42063.herokuapp.com/getmessages";
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("session", sessionK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jb321", parameters.toString());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                        populateListFromVolley(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Main OnCreate", "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SecondActivity.class);
            i.putExtra("label_contents", "Create New Message");
            startActivityForResult(i, 789); // 789 is the number that will come back to us
            return true;
        }
        else if (id == R.id.user_profile) {
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            i.putExtra("label_contents", "User Profile for user " + uid);
            i.putExtra("uid", uid);
            i.putExtra("session", sessionK);
            startActivityForResult(i, 678);
            return true;
        }
        else if(id == R.id.logout){
            //sign out function (authorize end)
            SharedPrefManager.getInstance(getApplicationContext()).logout(); //when the user presses logout button calling the logout method
            Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(logoutIntent);
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 789) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the "extra" string of data
                //Toast.makeText(MainActivity.this, data.getStringExtra("result"), Toast.LENGTH_LONG).show();
                sendPostRequest(data);
                recreate(); //refresh page
            }
        }
    }


    /**
        sendPostRequest sends the message to the database
    */
    private void sendPostRequest(Intent data){
        String url = "https://radiant-spire-42063.herokuapp.com/messages";
        //Map<String, String> params = new HashMap();
        //params.put("mMessage", data.getStringExtra("result"));
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("mMessage", data.getStringExtra("result"));
            parameters.put("mUid", uid);
            parameters.put("session", sessionK); //pass the session key into url
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("jbd321", parameters.toString());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                // response
                Log.d("response","Success");
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("jbd321", "sendPostRequest didn't work!");
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(postRequest);
    }

    /**
     * Gets the number of votes of the message that was clicked on
     */
    public void getVote(String sMid){
        final int mid = Integer.parseInt(sMid);
        Log.d("Message id for get vote",sMid);

        String url = "https://radiant-spire-42063.herokuapp.com/getvotes";
        //final String finalUrl = url + sMid + "/votes";

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("session", sessionK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jb321", parameters.toString());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonData = response.getJSONArray("mData");

                    for (int i = 0; i < jsonData.length(); ++i) {
                        int cid = jsonData.getJSONObject(i).getInt("vMid");
                        String comt = jsonData.getJSONObject(i).getString("votes");
                        if (cid == mid) {
                            Log.d("Vote's MessageId", String.valueOf(cid));
                            Log.d("vote number", comt);
                            //Toast.makeText(MainActivity.this, "V: "  + comt, Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "Vote: "+ comt, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (final JSONException e) {
                    Log.d("jbd321", "Error parsing JSON file: " + e.getMessage());
                    return;
                }

                Log.d("response","Get vote Success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("jbd321", "That didn't work! - getVote Error!");
            }
        });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    public void addVote(String mid, String mUrl){
        String finalUrl = mUrl + "/votes";
        Log.d("jbd321", finalUrl);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("votes", 1);
            parameters.put("vMid", Integer.parseInt(mid));
            parameters.put("session", sessionK);
            parameters.put("vUid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jb321", parameters.toString());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, parameters, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                // response
                Log.d("response","Success");
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("jbd321", "Upvote didn't work!");
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(postRequest);
        recreate(); //refresh page
    }

    public void downVote(String mid,/*int votes,*/ String mUrl){
        String finalUrl = mUrl + "/votes";
        //int newVotes = votes - 1;
        //params.put("mData", newVotes);
        //Log.d("jbd321", Integer.toString(newVotes));
        Log.d("jbd321", finalUrl);
        JSONObject parameters = new JSONObject();
        try {
            //parameters.put("mVotes", newVotes);
            parameters.put("vMid", Integer.parseInt(mid));
            parameters.put("vUid", uid);
            parameters.put("votes", -1);
            parameters.put("session", sessionK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jb321", parameters.toString());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // response
                Log.d("response","Success");
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("jbd321", "Downvote didn't work!");
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(postRequest);
        recreate(); //refresh page
    }

    public void addComment(String comment, String mUrl, int mid, int uid){
        String finalUrl = mUrl;
        //Log.d("jbd321", comment);
        //Log.d("jbd321", finalUrl);
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("cComment", comment); //change the name mComments to the thing in the backend
            parameters.put("cUid", uid);
            parameters.put("cMid", mid);
            parameters.put("session", sessionK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jb321", parameters.toString());
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, finalUrl, parameters, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                // response
                Log.d("response","Post Comments Success");
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.e("jbd321", "Post Comment didn't work!");
                    }
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(postRequest);
        recreate(); //refresh page
    }

    public void getComment(String sMid/*CommentData d*/){
        //final int cid = d.cIndex;
        final int mid = Integer.parseInt(sMid);
        Log.d("Message id for comment",sMid);

        String url = "https://radiant-spire-42063.herokuapp.com/getcomments";
        final String finalUrl = url;

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("session", sessionK);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("jb321", parameters.toString());
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    //JSONObject jsonObj = new JSONObject(response);
                    JSONArray jsonData = response.getJSONArray("mData");

                    for (int i = 0; i < jsonData.length(); ++i) {
                        int cid = jsonData.getJSONObject(i).getInt("cMid");
                        String comt = jsonData.getJSONObject(i).getString("cComment");
                        //cData.add(new CommentData(cid, comt));
                        if (cid == mid) {
                            Log.d("MessageId", String.valueOf(cid));
                            Log.d("Related comment", comt);
                            Toast.makeText(MainActivity.this, "Comment: "  + comt, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (final JSONException e) {
                    Log.d("jbd321", "Error parsing JSON file: " + e.getMessage());
                    return;
                }

                Log.d("response","Get comments Success");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("jbd321", "That didn't work! - getComment Error!");
            }
        });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


}
