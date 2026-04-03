package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    interface ApiService {
        // Cập nhật route chính xác theo Backend cung cấp
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

    private void performLogin(String user, String pass) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        LoginRequest request = new LoginRequest(user, pass);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    // Đăng nhập thành công
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Sai tài khoản hoặc mật khẩu (Lỗi 401 hoặc 404 nếu sai route)
                    Log.e("LOGIN_FAIL", "Code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LOGIN_ERROR", t.getMessage());
                Toast.makeText(LoginActivity.this, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
