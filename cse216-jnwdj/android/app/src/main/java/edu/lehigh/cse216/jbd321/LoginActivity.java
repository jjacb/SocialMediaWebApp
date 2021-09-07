package edu.lehigh.cse216.jbd321;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    EditText _username, _password;
    Button _login_btn;
    Context ctx;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    //int counter = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        ctx = this;
        _username = (EditText) findViewById(R.id._username);
        _password = (EditText) findViewById(R.id._password);
        _login_btn = (Button) findViewById(R.id._login_btn);

        //if user presses on login calling the method login
        findViewById(R.id._login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
//                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                LoginActivity.this.finish();
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));

            }
        });

        //if user presses on cancel stopping the activity
        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                //finish();
            }
        });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void userLogin() {
        //first getting the values
        final String username = String.valueOf(_username.getText());
        final String password = String.valueOf(_password.getText());
        Log.d("UserInput", username);
        Log.d("UserInput", password);

        //validating inputs
        if (TextUtils.isEmpty(username)) {
            _username.setError("Please enter your username");
            _username.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            _password.setError("Please enter your password");
            _password.requestFocus();
            return;
        }

        //parameter
        String url = "https://radiant-spire-42063.herokuapp.com/userLogin";
        JSONObject jsonObject1 = new JSONObject();
        try {
            jsonObject1.put("uUsername", username);
            jsonObject1.put("uPassword", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Map User", jsonObject1.toString());

        /*JsonObjectRequest logRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject1, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Login Volley Result", ""+response);

                try {
                    //converting response to json object
                    JSONObject obj = new JSONObject(String.valueOf(response));
                    Log.d("Response for Login", String.valueOf(response));

                    //if no error in response
                    if (obj.has("mData")*//*!obj.getBoolean("error")*//*) {

                        //getting the session key from the response
                        String key = obj.getString("mData");
                        Log.d("Session Key", key);

                        String id = obj.getString("mMessage");
                        int uid = Integer.parseInt(id);

                        //creating a new user object
                        User user = new User(
                                //store the session key getting from response
                                *//*userJson.getString("mMessage")*//*
                                uid,
                                key
                        );
                        Log.d("User", user.getUsername());
                        Log.d("User", String.valueOf(user.getId()));
                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                        //starting the Main activity
                        finish();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        //error message
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("jdb321", "Login Fail");
                //Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
            }
        });

        VolleySingleton.getInstance(this).addToRequestQueue(logRequest);*/

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            // Signed in successfully, show authenticated UI.
            //TODO send idtoken o jacob's route

            String url = "https://radiant-spire-42063.herokuapp.com/userLogin";
            JSONObject jsonObject1 = new JSONObject();
            try {
                jsonObject1.put("id_token", idToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest logRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject1, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("Login Volley Result", ""+response);

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(String.valueOf(response));
                        Log.d("Response for Login", String.valueOf(response));

                        //if no error in response
                        if (obj.has("mData")/*!obj.getBoolean("error")*/) {

                            //getting the session key from the response
                            String key = obj.getString("mData");
                            Log.d("Session Key", key);

                            String id = obj.getString("mMessage");
                            int uid = Integer.parseInt(id);

                            //creating a new user object
                            User user = new User(
                                    //store the session key getting from response
                                    uid,
                                    key
                            );
                            Log.d("User", user.getUsername());
                            Log.d("User", String.valueOf(user.getId()));
                            //storing the user in shared preferences
                            SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);

                            //starting the Main activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            //error message
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("jdb321", "Login Fail");
                    Toast.makeText(LoginActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                }
            });

            VolleySingleton.getInstance(this).addToRequestQueue(logRequest);

            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }
    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }
}