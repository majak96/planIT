package com.example.planit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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

import com.example.planit.R;
import com.example.planit.mokaps.Mokap;
import com.example.planit.utils.SharedPreference;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

import model.User;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

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

        email=findViewById(R.id.signInEmailInput);
        password=findViewById(R.id.signInPasswordInput);
        signUpLink = findViewById(R.id.signInLink);
        signInBtn = findViewById(R.id.signInButton);

        //google sign in
        googleSignInButton=(SignInButton)findViewById(R.id.googleSignInButton);

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
        signUpLink.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        //login button
        signInBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                hideKeyboard();
                if(isEmpty(password) || isEmpty(email)){
                    Toast t = Toast.makeText(SignInActivity.this, "You must enter email and passwied!", Toast.LENGTH_SHORT);
                    t.show();
                }
                else if(!isValidEmail(email.getText().toString())) {
                    Toast t = Toast.makeText(SignInActivity.this, "You must enter valid email address!", Toast.LENGTH_SHORT);
                    t.show();
                }
                else if(checkCredentials()==false){
                    Toast t = Toast.makeText(SignInActivity.this, "Credentials does not match!", Toast.LENGTH_SHORT);
                    t.show();
                }
                else{
                    SharedPreference.setLoggedEmail(getApplicationContext(), email.getText().toString());
                    Intent intent = new Intent(SignInActivity.this, ChooseModeActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    public boolean checkCredentials(){
        List<User>users= Mokap.getUsers();
        for(User u:users){
            Log.i("LOGIN", u.getEmail());
            Log.i("LOGIN", u.getPassword());
            Log.i("UNERO", email.getText().toString());
            Log.i("UNETO", password.getText().toString());

            if(u.getEmail().equals(email.getText().toString()) && u.getPassword().equals(password.getText().toString()))
                return true;
        }
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast t = Toast.makeText(SignInActivity.this, "No internet connection!", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            SharedPreference.setLoggedEmail(SignInActivity.this, result.getSignInAccount().getEmail());
            gotoHomePage();
        }else{
            Toast.makeText(getApplicationContext(),"Sign in cancel",Toast.LENGTH_LONG).show();
        }
    }

    private void gotoHomePage(){
        Intent intent=new Intent(SignInActivity.this, ChooseModeActivity.class);
        startActivity(intent);
    }

    public boolean isValidEmail(String target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }

}