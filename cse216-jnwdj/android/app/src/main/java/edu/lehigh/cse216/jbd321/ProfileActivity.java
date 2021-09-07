package edu.lehigh.cse216.jbd321;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity {

    ArrayList<Datum> mData = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        RequestQueue queue = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        // Get the parameter from the calling activity, and put it in the TextView
        Intent input = getIntent();
        String label_contents = input.getStringExtra("label_contents");

        TextView tv = (TextView) findViewById(R.id.specialMessage);
        tv.append(label_contents + "\n\n");

        int uid = input.getIntExtra("uid", -1);
        String session = input.getStringExtra("session");
        if (uid != -1){
            String url = "https://radiant-spire-42063.herokuapp.com/getusers/" + uid;
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("session", session); //pass the session key into url
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        JSONObject jsonData = response.getJSONObject("mData");
                        TextView tv = (TextView) findViewById(R.id.specialMessage);
                        tv.append("Username: "+ jsonData.getString("uUsername")+"\nEmail: " + jsonData.getString("uEmail")
                            + "\nLocation: " + jsonData.getString("uLocation"));
                    } catch (final JSONException e) {
                        Log.d("jbd321", "Error parsing JSON file: " + e.getMessage());
                        return;
                    }

                    Log.d("response","Get user Success");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("jbd321", "That didn't work! - getUser Error!");
                }
            });

            // Add the request to the RequestQueue.
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }

        // The Cancel button returns to the caller without sending any data
        Button bCancel = (Button) findViewById(R.id.buttonCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        String url = "https://radiant-spire-42063.herokuapp.com/getusers/" + uid + "/messages";
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("session", session);
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
                Log.e("Profile OnCreate", "That didn't work!");
            }
        });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    private void populateListFromVolley(JSONObject response){
        try {
            /**
             * Created an array to hold the messages from the Database
             */
            JSONArray jsonData = response.getJSONArray("mData");

            for (int i = 0; i < jsonData.length(); ++i) {
                String str = jsonData.getString(i);
                mData.add(new Datum(0,str,0));
            }
        } catch (final JSONException e) {
            Log.d("jbd321", "Error parsing JSON file: " + e.getMessage());
            return;
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.datum_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ProfileMsgAdapter adapter = new ProfileMsgAdapter(this, mData, ProfileActivity.this);
        rv.setAdapter(adapter);
        adapter.setClickListener(new ProfileMsgAdapter.ClickListener() {
            @Override
            public void onClick(Datum d) {
                Toast.makeText(ProfileActivity.this,  " Message id:" + d.mIndex + " --> " + d.mText, Toast.LENGTH_LONG).show();
            }
        });

    }
}
