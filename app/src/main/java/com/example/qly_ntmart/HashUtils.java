package com.example.qly_ntmart;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class HashUtils {
    /**
     * Băm chuỗi văn bản thành mã SHA-256
     */
    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            // Nếu lỗi thì trả về chuỗi gốc (không nên xảy ra)
            return base;
        }
    }
}
