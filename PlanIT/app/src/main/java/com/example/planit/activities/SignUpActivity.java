package com.example.planit.activities;

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
import com.example.planit.service.AuthService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.Utils;

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

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard();
                if (isEmpty(password) || isEmpty(email) || isEmpty(name) || isEmpty(lastName)) {
                    Toast t = Toast.makeText(SignUpActivity.this, "You must enter email all fields!", Toast.LENGTH_SHORT);
                    t.show();
                } else if (!isValidEmail(email.getText().toString())) {
                    Toast t = Toast.makeText(SignUpActivity.this, "You must enter valid email address!", Toast.LENGTH_SHORT);
                    t.show();
                } else {

                    RegisterDTO registerDTO = new RegisterDTO(email.getText().toString(), password.getText().toString(), name.getText().toString(), lastName.getText().toString(), Utils.getRandomColor());

                    AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
                    Call<ResponseBody> call = apiService.register(registerDTO);
                    call.enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.code() == 200) {
                                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                startActivity(intent);
                            } else {
                                Toast t = Toast.makeText(SignUpActivity.this, "User with same email already exists!", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
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
