package com.deligo.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
    private TextView textFullName, textEmail, textPhone, textAddress, textStoreName, textRoleStatus, textError;
    private ImageView imageAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user, container, false);
        imageAvatar = root.findViewById(R.id.imageAvatar);
        textFullName = root.findViewById(R.id.textFullName);
        textEmail = root.findViewById(R.id.textEmail);
        textPhone = root.findViewById(R.id.textPhone);
        textAddress = root.findViewById(R.id.textAddress);
        textStoreName = root.findViewById(R.id.textStoreName);
        textRoleStatus = root.findViewById(R.id.textRoleStatus);
        textError = root.findViewById(R.id.textError);
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

        buttonChangePassword.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Tính năng sẽ sớm có mặt", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            // Clear in-memory session and navigate to Login
            UserSession.clear();
            Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return root;
    }

    private void bindUser(@NonNull UserEntity user) {
        textFullName.setText(user.getFullName());
        textEmail.setText(user.getEmail());
        textPhone.setText(user.getPhone());

        String role = user.getRole();
        String status = user.getStatus();
        textRoleStatus.setText("Vai trò: " + (role != null ? role : "?") + " • Trạng thái: " + (status != null ? status : "?"));

        // Role-based fields
        String currentRole = UserSession.getCurrentUserRole();
        if (currentRole == null && role != null) currentRole = role;
        if (currentRole != null) currentRole = currentRole.toLowerCase();

        if ("customer".equals(currentRole) || "shipper".equals(currentRole)) {
            textAddress.setVisibility(View.VISIBLE);
            textAddress.setText("Địa chỉ: " + (user.getAddress() == null ? "" : user.getAddress()));
            textStoreName.setVisibility(View.GONE);
        } else if ("owner".equals(currentRole)) {
            textStoreName.setVisibility(View.VISIBLE);
            textStoreName.setText("Tên cửa hàng: " + (user.getStoreName() == null ? "" : user.getStoreName()));
            textAddress.setVisibility(View.GONE);
        } else {
            textAddress.setVisibility(View.GONE);
            textStoreName.setVisibility(View.GONE);
        }

        // Avatar: using default icon; if we had an image loader, we could load from URI
    }

    private void showError(String msg) {
        textError.setVisibility(View.VISIBLE);
        textError.setText(msg);
    }
}
