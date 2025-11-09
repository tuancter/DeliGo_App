package com.deligo.app.ui.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.data.UserSession;
import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.ui.profile.ProfileViewModel;

public class EditProfileDialogFragment extends DialogFragment {

    public static EditProfileDialogFragment newInstance() {
        return new EditProfileDialogFragment();
        }

    private ProfileViewModel viewModel;

    private EditText editFullName;
    private EditText editEmail;
    private EditText editPhone;
    private EditText editAddress;
    private EditText editStoreName;
    private EditText editAvatarUri;
    private LinearLayout layoutAddress;
    private LinearLayout layoutStoreName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_edit_profile, container, false);
        editFullName = root.findViewById(R.id.editFullName);
        editEmail = root.findViewById(R.id.editEmail);
        editPhone = root.findViewById(R.id.editPhone);
        editAddress = root.findViewById(R.id.editAddress);
        editStoreName = root.findViewById(R.id.editStoreName);
        editAvatarUri = root.findViewById(R.id.editAvatarUri);
        layoutAddress = root.findViewById(R.id.layoutAddress);
        layoutStoreName = root.findViewById(R.id.layoutStoreName);
        Button buttonCancel = root.findViewById(R.id.buttonCancel);
        Button buttonSave = root.findViewById(R.id.buttonSave);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Populate existing values
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                editFullName.setText(nullToEmpty(user.getFullName()));
                editEmail.setText(nullToEmpty(user.getEmail()));
                editPhone.setText(nullToEmpty(user.getPhone()));
                editAddress.setText(nullToEmpty(user.getAddress()));
                editStoreName.setText(nullToEmpty(user.getStoreName()));
                editAvatarUri.setText(nullToEmpty(user.getAvatarUri()));
            }
        });

        // Role-based visibility
        String role = UserSession.getCurrentUserRole();
        if (role != null) role = role.toLowerCase();
        if ("owner".equals(role)) {
            layoutStoreName.setVisibility(View.VISIBLE);
            layoutAddress.setVisibility(View.GONE);
        } else if ("customer".equals(role) || "shipper".equals(role)) {
            layoutAddress.setVisibility(View.VISIBLE);
            layoutStoreName.setVisibility(View.GONE);
        } else {
            layoutAddress.setVisibility(View.GONE);
            layoutStoreName.setVisibility(View.GONE);
        }

        buttonCancel.setOnClickListener(v -> dismiss());

        buttonSave.setOnClickListener(v -> onSave());

        return root;
    }

    private void onSave() {
        UserEntity current = viewModel.getUser() != null ? viewModel.getUser().getValue() : null;
        if (current == null) {
            Toast.makeText(requireContext(), "Không thể cập nhật thông tin, vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            return;
        }
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String storeName = editStoreName.getText().toString().trim();
        String avatarUri = editAvatarUri.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            editFullName.setError("Vui lòng nhập họ và tên");
            return;
        }
        if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Email không hợp lệ");
            return;
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (!TextUtils.isEmpty(phone) && (digits.length() < 10 || digits.length() > 11)) {
            editPhone.setError("Số điện thoại phải có 10–11 chữ số");
            return;
        }

        UserEntity updated = new UserEntity();
        updated.setUserId(current.getUserId());
        updated.setFullName(fullName);
        updated.setEmail(email);
        updated.setPhone(phone);
        updated.setAddress(address);
        updated.setStoreName(storeName);
        updated.setAvatarUri(avatarUri);
        // Preserve non-editable fields
        updated.setPassword(current.getPassword());
        updated.setRole(current.getRole());
        updated.setStatus(current.getStatus());
        updated.setCreatedAt(current.getCreatedAt());

        viewModel.updateProfile(updated);
        dismiss();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
