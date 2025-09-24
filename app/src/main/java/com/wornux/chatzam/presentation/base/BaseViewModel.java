package com.wornux.chatzam.presentation.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {
    
    protected final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> _error = new MutableLiveData<>();
    
    public LiveData<Boolean> getLoading() {
        return _loading;
    }
    
    public LiveData<String> getError() {
        return _error;
    }
    
    protected void setLoading(boolean loading) {
        _loading.setValue(loading);
    }
    
    protected void setError(String error) {
        _error.setValue(error);
    }
    
    protected void clearError() {
        _error.setValue(null);
    }
}