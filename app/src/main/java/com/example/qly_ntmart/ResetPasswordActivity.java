package com.example.qly_ntmart;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText edtNewPassword;
    private Button btnResetPassword;
    private String token, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        token = getIntent().getStringExtra("token");
        phone = getIntent().getStringExtra("phone");

        edtNewPassword = findViewById(R.id.edtNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = edtNewPassword.getText().toString().trim();
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }
            resetPassword(newPassword);
        });
    }

    private void resetPassword(String newPassword) {
//        btnResetPassword.setEnabled(false);
//        btnResetPassword.setText("Đang xử lý...");

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                json.put("token", token);
                json.put("phone", phone);
                json.put("new_password", newPassword);

                RequestBody body = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        json.toString()
                );

                Request request = new Request.Builder()
                        .url("http://192.168.56.1:8000/auth/reset-password")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                // IN KẾT QUẢ RA LOGCAT Ở ĐÂY
                Log.d("API_RESULT", "Server trả về: " + responseBody);

                JSONObject result = new JSONObject(responseBody);

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        showSuccessDialog();
                    } else {
                        String error = result.optString("error", "Đổi mật khẩu thất bại!");
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        btnResetPassword.setEnabled(true);
                        btnResetPassword.setText("Đổi mật khẩu");
                    }
                });

            } catch (Exception e) {
                Log.e("API_ERROR", "Lỗi khi gọi API: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnResetPassword.setEnabled(true);
                    btnResetPassword.setText("Đổi mật khẩu");
                });
            }
        }).start();
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCancelable(false);

        ImageView ivClose = dialog.findViewById(R.id.iv_close_dialog);
        ivClose.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToLogin();
        });

        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                navigateToLogin();
            }
        }, 2500);

        dialog.show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
