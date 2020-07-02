package com.example.planit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import model.LoginDTO;
import model.UserInfoDTO;
import model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1;
    private String tag = "SignInActivity";
    private TextView signUpLink;
    private Button signInBtn;
    private EditText email;
    private EditText password;
    private SignInButton googleSignInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ProgressDialog loadingBar;
    private String firebaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email = findViewById(R.id.signInEmailInput);
        password = findViewById(R.id.signInPasswordInput);
        signUpLink = findViewById(R.id.signInLink);
        signInBtn = findViewById(R.id.signInButton);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        googleSignInButton = findViewById(R.id.googleSignInButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
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

                    loadingBar.setTitle("Sing in");
                    loadingBar.setMessage("Pleas wait...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        LoginDTO loginDTO = new LoginDTO();
                                        loginDTO.setEmail(email.getText().toString());
                                        loginDTO.setPassword(password.getText().toString());

                                        AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
                                        Call<UserInfoDTO> call = apiService.login(loginDTO);
                                        call.enqueue(new Callback<UserInfoDTO>() {

                                            @Override
                                            public void onResponse(Call<UserInfoDTO> call, Response<UserInfoDTO> response) {

                                                String name = "", lastName = "", colour = "";
                                                String emailString = email.getText().toString();
                                                firebaseId = "";

                                                if (response.code() == 200) {
                                                    UserInfoDTO userInfo = response.body();
                                                    name = userInfo.getFirstName();
                                                    lastName = userInfo.getLastName();
                                                    colour = userInfo.getColour();
                                                    firebaseId = userInfo.getFirebaseId();

                                                    User newUser = new User(name, lastName, emailString, firebaseId);
                                                    newUser.setColour(colour);
                                                    createUser(newUser);

                                                    SharedPreference.setLoggedEmail(getApplicationContext(), emailString);
                                                    SharedPreference.setLoggedName(getApplicationContext(), name);
                                                    SharedPreference.setLoggedLastName(getApplicationContext(), lastName);
                                                    SharedPreference.setLoggedColour(getApplicationContext(), colour);

                                                    loadingBar.dismiss();

                                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);

                                                } else {
                                                    loadingBar.dismiss();
                                                    Toast t = Toast.makeText(SignInActivity.this, "Credentials does not match!", Toast.LENGTH_SHORT);
                                                    t.show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<UserInfoDTO> call, Throwable t) {
                                                loadingBar.dismiss();
                                                Toast toast = Toast.makeText(SignInActivity.this, "Connection error!", Toast.LENGTH_SHORT);
                                                toast.show();
                                                Log.e(tag, "Error in login");
                                            }
                                        });
                                    } else {
                                        loadingBar.dismiss();
                                        Toast t = Toast.makeText(SignInActivity.this, "Credentials does not match!", Toast.LENGTH_SHORT);
                                        t.show();
                                    }
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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(tag, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(), data);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(tag, "Google sign in failed", e);
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken, Intent data) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        loadingBar.setTitle("Sing in");
        loadingBar.setMessage("Pleas wait...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(tag, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                            handleSignInResult(result);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(tag, "signInWithCredential:failure", task.getException());
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            String firstNameLastName = result.getSignInAccount().getDisplayName();
            String email = result.getSignInAccount().getEmail();
            String[] parts = firstNameLastName.split(" ");
            String colour = Utils.getRandomColor();
            String firstName = parts[0];
            String lastName = parts[1];

            UserInfoDTO googleUserInfoDTO = new UserInfoDTO(email, null, firstName, lastName, colour, mAuth.getCurrentUser().getUid());
            AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
            Call<ResponseBody> call = apiService.googleLogin(googleUserInfoDTO);
            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    if (response.code() == 200) {

                        User newUser = new User(firstName, lastName, email, mAuth.getCurrentUser().getUid());
                        newUser.setColour(colour);
                        createUser(newUser);

                        SharedPreference.setLoggedEmail(SignInActivity.this, email);
                        SharedPreference.setLoggedColour(SignInActivity.this, colour);
                        SharedPreference.setLoggedName(SignInActivity.this, firstName);
                        SharedPreference.setLoggedLastName(SignInActivity.this, lastName);
                        loadingBar.dismiss();
                        gotoHomePage();
                    } else {
                        loadingBar.dismiss();
                        Toast t = Toast.makeText(SignInActivity.this, "An error occurred!", Toast.LENGTH_SHORT);
                        t.show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    loadingBar.dismiss();
                    Log.e("tag", "Failed");
                }

            });

        } else {
            Log.e(tag, result.getStatus().toString());
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
        values.put(Contract.User.COLUMN_FIREBASE_ID, user.getFirebaseId());

        Uri uri = getContentResolver().insert(Contract.User.CONTENT_URI_USER, values);

        return uri;

    }

}