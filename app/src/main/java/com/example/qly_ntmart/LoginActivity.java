package com.example.qly_ntmart;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private CheckBox cbRememberMe;
    private SharedPreferences sharedPreferences;

    interface ApiService {
        @POST("auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        
        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);

        checkRememberedAccount();

        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                performLogin(user, pass);
            }
        });
    }

    private void checkRememberedAccount() {
        boolean isRemembered = sharedPreferences.getBoolean("remember", false);
        if (isRemembered) {
            String savedUser = sharedPreferences.getString("username", "");
            String savedPass = sharedPreferences.getString("password", "");
            etUsername.setText(savedUser);
            etPassword.setText(savedPass);
            cbRememberMe.setChecked(true);
        }
    }

    private void performLogin(String user, String pass) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.56.1:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        LoginRequest request = new LoginRequest(user, pass);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    saveAccountInfo(user, pass);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    showErrorDialog("Tài khoản hoặc mật khẩu\nkhông đúng vui lòng nhập lại");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LOGIN_ERROR", t.getMessage());
                Toast.makeText(LoginActivity.this, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showErrorDialog(String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_login_error);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMessage = dialog.findViewById(R.id.tv_error_message);
        tvMessage.setText(message);

        ImageView ivClose = dialog.findViewById(R.id.iv_close_dialog);
        ivClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveAccountInfo(String user, String pass) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (cbRememberMe.isChecked()) {
            editor.putString("username", user);
            editor.putString("password", pass);
            editor.putBoolean("remember", true);
        } else {
            editor.clear();
        }
        editor.apply();
    }
}
