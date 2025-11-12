package com.deligo.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.data.UserSession;
import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.ui.auth.LoginActivity;
import com.deligo.app.ui.profile.ProfileViewModel;

public class UserFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView textFullName, textEmail, textPhone, textAddress, textStoreName,
            textRole, textError, textStatus;
    private ImageView imageAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);

        imageAvatar   = root.findViewById(R.id.imageAvatar);
        textFullName  = root.findViewById(R.id.textFullName);
        textEmail     = root.findViewById(R.id.textEmail);
        textPhone     = root.findViewById(R.id.textPhone);
        textAddress   = root.findViewById(R.id.textAddress);
        textStoreName = root.findViewById(R.id.textStoreName);   // ✅ đúng id
        textStatus    = root.findViewById(R.id.textStatus);
        textRole      = root.findViewById(R.id.textRole);
        textError     = root.findViewById(R.id.textError);

        Button buttonEdit = root.findViewById(R.id.buttonEditProfile);
        Button buttonChangePassword = root.findViewById(R.id.buttonChangePassword);
        Button buttonLogout = root.findViewById(R.id.buttonLogout);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        if (UserSession.getCurrentUserId() == null) {
            showError("Không thể tải thông tin hồ sơ.");
        }

        if (viewModel.getUser() != null) {
            viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                if (user == null) {
                    showError("Không thể tải thông tin hồ sơ.");
                    return;
                }
                bindUser(user);
            });
        } else {
            showError("Không thể tải thông tin hồ sơ.");
        }

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (!TextUtils.isEmpty(msg)) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        buttonEdit.setOnClickListener(v -> {
            EditProfileDialogFragment dialog = EditProfileDialogFragment.newInstance();
            dialog.show(getParentFragmentManager(), "edit_profile");
        });

        buttonChangePassword.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tính năng sẽ sớm có mặt", Toast.LENGTH_SHORT).show()
        );

        buttonLogout.setOnClickListener(v -> {
            UserSession.clear();
            Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return root;
    }

    private void bindUser(@NonNull UserEntity user) {
        // Tên
        String fullName = user.getFullName();
        if (!TextUtils.isEmpty(fullName) && !fullName.trim().isEmpty()) {
            textFullName.setText(fullName.trim());
        } else {
            textFullName.setText("Không có tên");
        }
        Log.d("UserInfo", "Full name = " + user.getFullName());

        // Email / Phone
        textEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        textPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        // Role & status (chip)
        String role = user.getRole();
        String status = user.getStatus();

        String displayRole = (role == null) ? "?" : capitalize(role);
        textRole.setText(displayRole);
        textStatus.setText(status != null ? status : "");

        // Lấy role hiện tại ưu tiên từ session
        String currentRole = UserSession.getCurrentUserRole();
        if (currentRole == null && role != null) currentRole = role;
        if (currentRole != null) currentRole = currentRole.toLowerCase();

        // Ẩn/hiện linh hoạt theo role + dữ liệu
        // Địa chỉ
        String address = user.getAddress();
        if (!TextUtils.isEmpty(address)) {
            textAddress.setText("Địa chỉ: " + address);
        } else {
            textAddress.setText("");
        }

        // Tên cửa hàng
        String storeName = user.getStoreName();
        if (!TextUtils.isEmpty(storeName)) {
            textStoreName.setText("Tên cửa hàng: " + storeName);
        } else {
            textStoreName.setText("");
        }

        // Logic hiển thị:
        // - customer: ưu tiên Địa chỉ. Tên cửa hàng ẩn.
        // - owner: ưu tiên Tên cửa hàng, có thể hiển thị thêm Địa chỉ nếu có.
        if ("customer".equals(currentRole)) {
            if (!TextUtils.isEmpty(address)) {
                textAddress.setVisibility(View.VISIBLE);
            } else {
                textAddress.setVisibility(View.GONE);
            }
            textStoreName.setVisibility(View.GONE);
        } else if ("owner".equals(currentRole)) {
            if (!TextUtils.isEmpty(storeName)) {
                textStoreName.setVisibility(View.VISIBLE);
            } else {
                textStoreName.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(address)) {
                textAddress.setVisibility(View.VISIBLE);
            } else {
                textAddress.setVisibility(View.GONE);
            }
        } else {
            // Role khác / không xác định: ẩn cả hai nếu không có dữ liệu
            textAddress.setVisibility(
                    !TextUtils.isEmpty(address) ? View.VISIBLE : View.GONE);
            textStoreName.setVisibility(
                    !TextUtils.isEmpty(storeName) ? View.VISIBLE : View.GONE);
        }

        // Avatar: hiện dùng icon mặc định; sau này có URL thì load thêm.
    }

    private void showError(String msg) {
        textError.setVisibility(View.VISIBLE);
        textError.setText(msg);
    }

    // Helper: viết hoa chữ cái đầu cho role (customer -> Customer)
    private String capitalize(@NonNull String s) {
        s = s.trim();
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
