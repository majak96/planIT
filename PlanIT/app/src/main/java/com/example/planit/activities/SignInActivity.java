package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planit.MainActivity;
import com.example.planit.R;
import com.example.planit.database.Contract;
import com.example.planit.service.AuthService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.SharedPreference;
import com.example.planit.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import model.LoginDTO;
import model.RegisterDTO;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private TextView signUpLink;
    private Button signInBtn;
    private EditText email;
    private EditText password;
    private SignInButton googleSignInButton;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email = findViewById(R.id.signInEmailInput);
        password = findViewById(R.id.signInPasswordInput);
        signUpLink = findViewById(R.id.signInLink);
        signInBtn = findViewById(R.id.signInButton);

        //google sign in
        googleSignInButton = findViewById(R.id.googleSignInButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

        //link to sign up activity
        signUpLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        signInBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard();
                if (isEmpty(password) || isEmpty(email)) {
                    Toast t = Toast.makeText(SignInActivity.this, "You must enter email and password!", Toast.LENGTH_SHORT);
                    t.show();
                } else if (!isValidEmail(email.getText().toString())) {
                    Toast t = Toast.makeText(SignInActivity.this, "You must enter valid email address!", Toast.LENGTH_SHORT);
                    t.show();
                } else {
                    LoginDTO loginDTO = new LoginDTO();
                    loginDTO.setEmail(email.getText().toString());
                    loginDTO.setPassword(password.getText().toString());

                    AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
                    Call<ResponseBody> call = apiService.login(loginDTO);
                    call.enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            String name = "";
                            String lastName = "";
                            String colour = "";
                            String emailString = email.getText().toString();
                            if (response.code() == 200) {
                                String resStr = null;
                                try {
                                    resStr = response.body().string().toString();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    JSONObject json = new JSONObject(resStr);
                                    name = json.get("firstName").toString();
                                    lastName = json.get("lastName").toString();
                                    colour = json.get("colour").toString();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                User newUser = new User(name, lastName, emailString);
                                newUser.setColour(colour);
                                Uri uri = createUser(newUser);
                                String id = uri.getLastPathSegment();

                                SharedPreference.setLoggedId(SignInActivity.this, Integer.parseInt(id));
                                SharedPreference.setLoggedEmail(getApplicationContext(), emailString);
                                SharedPreference.setLoggedName(getApplicationContext(), name);
                                SharedPreference.setLoggedLastName(getApplicationContext(), lastName);
                                SharedPreference.setLoggedColour(getApplicationContext(), colour);

                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {
                                Toast t = Toast.makeText(SignInActivity.this, "Credentials does not match!", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("tag", "Error in login");
                        }
                    });
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            String firstNameLastName = result.getSignInAccount().getDisplayName();
            String email = result.getSignInAccount().getEmail();
            String[] parts = firstNameLastName.split(" ");
            String colour = Utils.getRandomColor();
            String firstName = parts[0];
            String lastName = parts[1];

            RegisterDTO googleRegisterDTO = new RegisterDTO(email, null, firstName, lastName, colour);
            AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
            Call<ResponseBody> call = apiService.googleLogin(googleRegisterDTO);
            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.code() == 200) {

                        User newUser = new User(firstName, lastName, email);
                        newUser.setColour(colour);
                        Uri uri = createUser(newUser);
                        String id = uri.getLastPathSegment();

                        SharedPreference.setLoggedId(SignInActivity.this, Integer.parseInt(id));
                        SharedPreference.setLoggedEmail(SignInActivity.this, email);
                        SharedPreference.setLoggedColour(SignInActivity.this, colour);
                        SharedPreference.setLoggedName(SignInActivity.this, firstName);
                        SharedPreference.setLoggedLastName(SignInActivity.this, lastName);

                        gotoHomePage();
                    } else {
                        Toast t = Toast.makeText(SignInActivity.this, "An error occured!", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("tag", "Failed");
                }

            });

        } else {
            Toast.makeText(getApplicationContext(), "Sign in cancel", Toast.LENGTH_LONG).show();
        }
    }

    private void gotoHomePage() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public boolean isValidEmail(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    //inserts a new user into the database
    public Uri createUser(User user) {

        ContentValues values = new ContentValues();

        values.put(Contract.User.COLUMN_EMAIL, user.getEmail());
        values.put(Contract.User.COLUMN_NAME, user.getName());
        values.put(Contract.User.COLUMN_LAST_NAME, user.getLastName());
        values.put(Contract.User.COLUMN_COLOUR, user.getColour());

        Uri uri = getContentResolver().insert(Contract.User.CONTENT_URI_USER, values);

        return uri;

    }

}