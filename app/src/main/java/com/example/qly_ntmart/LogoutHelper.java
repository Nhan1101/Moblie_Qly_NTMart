package com.example.qly_ntmart;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

public class LogoutHelper {
    public static void setupLogout(Activity activity, View ivUserProfile) {
        if (ivUserProfile == null) return;
        
        ivUserProfile.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, ivUserProfile);
            popupMenu.getMenu().add("Đăng xuất");
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Đăng xuất")) {
                    performLogout(activity);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private static void performLogout(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        // Xóa stack các activity trước đó để không quay lại được bằng nút back
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        Toast.makeText(activity, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }
}
