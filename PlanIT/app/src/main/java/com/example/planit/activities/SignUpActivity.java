package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planit.R;

public class SignUpActivity extends AppCompatActivity {


    private TextView signIpLink;
    private Button signUpBtn;
    private EditText email;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email=findViewById(R.id.SignUpEmailInput);
        password=findViewById(R.id.SignUpPasswordInput);
        signIpLink = findViewById(R.id.signInLink);
        signUpBtn = findViewById(R.id.SignUpButton);

        signUpBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                hideKeyboard();
                if(isEmpty(password) || isEmpty(email)){
                    Toast t = Toast.makeText(SignUpActivity.this, "You must enter email and passwied!", Toast.LENGTH_SHORT);
                    t.show();
                }
                else if(!isValidEmail(email.getText().toString())) {
                    Toast t = Toast.makeText(SignUpActivity.this, "You must enter valid email address!", Toast.LENGTH_SHORT);
                    t.show();
                }
                else{
                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }
        });

        signIpLink.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

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
