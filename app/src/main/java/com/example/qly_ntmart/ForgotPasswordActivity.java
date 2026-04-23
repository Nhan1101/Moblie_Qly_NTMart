package com.example.qly_ntmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtPhone;
    private Button btnSendOTP;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();
        edtPhone = findViewById(R.id.edtPhone);
        btnSendOTP = findViewById(R.id.btnSendOTP);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSendOTP.setOnClickListener(v -> {
            String phone = edtPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
                return;
            }
            String phoneFormatted = "+84" + phone.substring(1);
            sendOTP(phoneFormatted);
        });
    }

    private void sendOTP(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Gửi OTP thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnSendOTP.setEnabled(true);
                        btnSendOTP.setText("Gửi mã OTP");
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "OTP đã được gửi!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOTPActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phone", edtPhone.getText().toString().trim());
                        startActivity(intent);

                        btnSendOTP.setEnabled(true);
                        btnSendOTP.setText("Gửi mã OTP");
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
