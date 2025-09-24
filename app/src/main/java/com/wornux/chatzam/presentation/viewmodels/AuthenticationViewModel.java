package com.wornux.chatzam.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;
import com.wornux.chatzam.data.services.AuthenticationManager;
import com.wornux.chatzam.domain.entities.UserProfile;
import com.wornux.chatzam.domain.enums.UserStatus;
import com.wornux.chatzam.domain.repositories.UserRepository;
import com.wornux.chatzam.presentation.base.BaseViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class AuthenticationViewModel extends BaseViewModel {
    
    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    
    private final MutableLiveData<FirebaseUser> _loginResult = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> _registrationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoginMode = new MutableLiveData<>(true);
    
    @Inject
    public AuthenticationViewModel(AuthenticationManager authManager, 
                                 UserRepository userRepository) {
        this.authManager = authManager;
        this.userRepository = userRepository;
    }
    
    public LiveData<FirebaseUser> getLoginResult() {
        return _loginResult;
    }
    
    public LiveData<FirebaseUser> getRegistrationResult() {
        return _registrationResult;
    }
    
    public LiveData<Boolean> getIsLoginMode() {
        return _isLoginMode;
    }
    
    public void login(String email, String password) {
        if (!isValidInput(email, password)) {
            return;
        }
        
        setLoading(true);
        clearError();
        
        authManager.loginUser(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    _loginResult.setValue(authResult.getUser());
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError(exception.getMessage());
                });
    }
    
    public void register(String email, String password, String displayName) {
        if (!isValidInput(email, password) || displayName.trim().isEmpty()) {
            setError("All fields are required");
            return;
        }
        
        setLoading(true);
        clearError();
        
        authManager.registerUser(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        createUserProfile(user, displayName);
                    }
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError(exception.getMessage());
                });
    }
    
    private void createUserProfile(FirebaseUser firebaseUser, String displayName) {
        UserProfile userProfile = UserProfile.builder()
                .userId(firebaseUser.getUid())
                .email(firebaseUser.getEmail())
                .displayName(displayName)
                .isOnline(true)
                .status(UserStatus.ONLINE)
                .build();
        
        userRepository.createUserProfile(userProfile)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    _registrationResult.setValue(firebaseUser);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    setError("Failed to create user profile: " + exception.getMessage());
                });
    }
    
    private boolean isValidInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            setError("Email is required");
            return false;
        }
        
        if (password == null || password.trim().isEmpty()) {
            setError("Password is required");
            return false;
        }
        
        if (password.length() < 6) {
            setError("Password must be at least 6 characters");
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError("Please enter a valid email address");
            return false;
        }
        
        return true;
    }
    
    public void switchMode() {
        _isLoginMode.setValue(!Boolean.TRUE.equals(_isLoginMode.getValue()));
        clearError();
    }
}