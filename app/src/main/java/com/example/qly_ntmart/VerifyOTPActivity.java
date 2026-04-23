package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerify;
    private FirebaseAuth auth;
    private String verificationId, phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        auth = FirebaseAuth.getInstance();
        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phone");

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerify = findViewById(R.id.btnVerify);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tự động nhảy sang ô tiếp theo khi nhập xong
        autoNextOTP(otp1, null, otp2);
        autoNextOTP(otp2, otp1, otp3);
        autoNextOTP(otp3, otp2, otp4);
        autoNextOTP(otp4, otp3, otp5);
        autoNextOTP(otp5, otp4, otp6);
        autoNextOTP(otp6, otp5, null);

        btnVerify.setOnClickListener(v -> {
            String otp = otp1.getText().toString()
                    + otp2.getText().toString()
                    + otp3.getText().toString()
                    + otp4.getText().toString()
                    + otp5.getText().toString()
                    + otp6.getText().toString();

            if (otp.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP!", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOTP(otp);
        });

        // Gửi lại OTP
        TextView tvResend = findViewById(R.id.tvResend);
        tvResend.setOnClickListener(v -> {
            finish(); // Quay lại màn nhập SĐT để gửi lại
        });
    }

    private void verifyOTP(String otp) {
//        btnVerify.setEnabled(false);
//        btnVerify.setText("Đang xác thực...");

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // OTP đúng → lấy Firebase token → gửi sang màn đổi mật khẩu
                        auth.getCurrentUser().getIdToken(true)
                                .addOnSuccessListener(result -> {
                                    String token = result.getToken();
                                    Log.d("FIREBASE_TOKEN", token); //Lấy token từ Logcat Android Studio
                                    Intent intent = new Intent(VerifyOTPActivity.this, ResetPasswordActivity.class);
                                    intent.putExtra("token", token);
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        Toast.makeText(this, "OTP sai hoặc đã hết hạn!", Toast.LENGTH_SHORT).show();
                        btnVerify.setEnabled(true);
                        btnVerify.setText("Tiếp tục");
                    }
                });
    }

    private void autoNextOTP(EditText current, EditText prev, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                }
                if (s.length() == 0 && prev != null) {
                    prev.requestFocus();
                }
            }
        });
    }
}