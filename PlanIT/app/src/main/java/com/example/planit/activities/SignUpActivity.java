package com.example.planit.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.example.planit.service.AuthService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import model.RegisterDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextView signInLink;
    private Button signUpBtn;
    private EditText email;
    private EditText password;
    private EditText name;
    private EditText lastName;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String tag = "SignUpActivity";
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.signUpEmailInput);
        password = findViewById(R.id.signUpPasswordInput);
        name = findViewById(R.id.signUpNameInput);
        lastName = findViewById(R.id.signUpLastNameInput);

        signInLink = findViewById(R.id.signInLink);
        signUpBtn = findViewById(R.id.signUpButton);

        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard();
                if (isEmpty(password) || isEmpty(email) || isEmpty(name) || isEmpty(lastName)) {
                    Toast t = Toast.makeText(SignUpActivity.this, "You must enter email all fields!", Toast.LENGTH_SHORT);
                    t.show();
                } else if (!isValidEmail(email.getText().toString())) {
                    Toast t = Toast.makeText(SignUpActivity.this, "You must enter valid email address!", Toast.LENGTH_SHORT);
                    t.show();
                } else if (password.getText().toString().length() < 6) {
                    Toast t = Toast.makeText(SignUpActivity.this, "Password should be at least 6 characters!", Toast.LENGTH_SHORT);
                    t.show();
                } else {

                    loadingBar.setTitle("Creating new account");
                    loadingBar.setMessage("Pleas wait while we were creating new account for you...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    RegisterDTO registerDTO = new RegisterDTO(email.getText().toString(), password.getText().toString(), name.getText().toString(), lastName.getText().toString(), Utils.getRandomColor());

                    AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
                    Call<ResponseBody> call = apiService.register(registerDTO);
                    call.enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.code() == 200) {
                                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    Toast t = Toast.makeText(SignUpActivity.this, "Successfully registered!", Toast.LENGTH_SHORT);
                                                    t.show();
                                                    String currentUserId = mAuth.getCurrentUser().getUid();
                                                    rootRef.child("Users").child(currentUserId).setValue("");

                                                    loadingBar.dismiss();
                                                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    loadingBar.dismiss();
                                                    Log.e(tag, "An error occurred");
                                                }
                                            }
                                        });

                            } else {
                                loadingBar.dismiss();

                                Toast t = Toast.makeText(SignUpActivity.this, "User with same email already exists!", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            loadingBar.dismiss();
                            Log.e("tag", "Failed");
                        }

                    });

                }
            }
        });

        signInLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

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
}
