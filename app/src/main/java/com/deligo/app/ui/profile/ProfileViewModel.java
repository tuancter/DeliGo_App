package com.deligo.app.ui.profile;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.deligo.app.data.UserSession;
import com.deligo.app.data.local.DeliGoDatabase;
import com.deligo.app.data.local.entity.UserEntity;
import com.deligo.app.data.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private LiveData<UserEntity> userLiveData;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(DeliGoDatabase.getInstance(application));
        Long id = UserSession.getCurrentUserId();
        if (id != null) {
            userLiveData = repository.getUserLive(id);
        }
    }

    public LiveData<UserEntity> getUser() {
        return userLiveData;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void updateProfile(@NonNull final UserEntity updated) {
        // Basic validation here; detailed validation is handled in UI as well
        if (TextUtils.isEmpty(updated.getFullName())) {
            message.setValue("Vui lòng nhập họ và tên");
            return;
        }
        if (!TextUtils.isEmpty(updated.getEmail()) && !Patterns.EMAIL_ADDRESS.matcher(updated.getEmail()).matches()) {
            message.setValue("Email không hợp lệ");
            return;
        }
        String phone = updated.getPhone();
        if (!TextUtils.isEmpty(phone)) {
            String digits = phone.replaceAll("[^0-9]", "");
            if (digits.length() < 10 || digits.length() > 11) {
                message.setValue("Số điện thoại phải có 10–11 chữ số");
                return;
            }
        }
        repository.updateUser(updated,
                new Runnable() {
                    @Override
                    public void run() {
                        message.postValue("Cập nhật hồ sơ thành công.");
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        message.postValue("Không thể cập nhật thông tin, vui lòng thử lại sau.");
                    }
                });
    }
}
