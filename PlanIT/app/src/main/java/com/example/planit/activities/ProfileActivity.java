package com.example.planit.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.planit.R;
import com.example.planit.service.AuthService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.utils.SharedPreference;

import model.ChangeProfileDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView name;
    private TextView lastName;
    private TextView loggedEmail;
    private TextView loggedCredential;
    private EditText editName;
    private EditText editLastName;
    private MenuItem editItem;
    private MenuItem saveChanges;
    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        name = findViewById(R.id.loggedName);
        lastName = findViewById(R.id.loggedLastName);
        loggedCredential = findViewById(R.id.loggedFirstChar);
        loggedEmail = findViewById(R.id.loggedEmail);
        editName = findViewById(R.id.editName);
        editLastName = findViewById(R.id.editLastName);
        name.setText(findLoggedUserName());
        lastName.setText(findLoggedUserLastName());
        loggedCredential.setText(name.getText().toString().substring(0, 1).concat(lastName.getText().toString().substring(0, 1)));
        loggedEmail.setText(SharedPreference.getLoggedEmail(ProfileActivity.this));

        if(savedInstanceState != null){
            isEdit = savedInstanceState.getBoolean("isEdit");
            editName.setText(savedInstanceState.getString("editName"));
            editLastName.setText(savedInstanceState.getString("editLastName"));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        editItem = menu.findItem(R.id.changeProfile);
        saveChanges = menu.findItem(R.id.saveChanges);
        if(isEdit == true){
            lastName.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
            saveChanges.setVisible(true);
            editItem.setVisible(false);
            editLastName.setVisibility(View.VISIBLE);
            editName.setVisibility(View.VISIBLE);
            editLastName.setText(editLastName.getText().toString());
            editName.setText(editName.getText().toString());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.changeProfile:
                isEdit = true;
                lastName.setVisibility(View.GONE);
                name.setVisibility(View.GONE);
                saveChanges.setVisible(true);
                editItem.setVisible(false);
                editLastName.setVisibility(View.VISIBLE);
                editName.setVisibility(View.VISIBLE);
                editLastName.setText(lastName.getText().toString());
                editName.setText(name.getText().toString());
                break;

            case R.id.saveChanges:
                isEdit = false;

                String email = SharedPreference.getLoggedEmail(ProfileActivity.this);
                ChangeProfileDTO changeProfileDTO = new ChangeProfileDTO(email, editName.getText().toString(), editLastName.getText().toString());

                AuthService apiService = ServiceUtils.getClient().create(AuthService.class);
                Call<ResponseBody> call = apiService.changeUser(changeProfileDTO);
                call.enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            lastName.setVisibility(View.VISIBLE);
                            name.setVisibility(View.VISIBLE);
                            saveChanges.setVisible(false);
                            editItem.setVisible(true);
                            editLastName.setVisibility(View.GONE);
                            editName.setVisibility(View.GONE);
                            lastName.setText(editLastName.getText().toString());
                            name.setText(editName.getText().toString());
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(name.getWindowToken(), 0);

                            SharedPreference.setLoggedName(ProfileActivity.this, editName.getText().toString());
                            SharedPreference.setLoggedLastName(ProfileActivity.this, editLastName.getText().toString());

                        } else {
                            Toast t = Toast.makeText(ProfileActivity.this, "An error occured!", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("tag", "Failed");
                    }
                });
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isEdit", isEdit);
        outState.putString("editName", editName.getText().toString());
        outState.putString("editLastName", editLastName.getText().toString());
    }

    public String findLoggedUserName() {
        if (SharedPreference.getLoggedName(ProfileActivity.this) == null) {
            return "";
        }
        return SharedPreference.getLoggedName(ProfileActivity.this);
    }

    public String findLoggedUserLastName() {
        if (SharedPreference.getLoggedLastName(ProfileActivity.this) == null) {
            return "";
        }
        return SharedPreference.getLoggedLastName(ProfileActivity.this);
    }

}
