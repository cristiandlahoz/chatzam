package com.wornux.chatzam.ui.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel extends ViewModel {
    
    protected final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    protected final MutableLiveData<String> error = new MutableLiveData<>();
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    protected void setLoading(boolean loading) {
        this.loading.setValue(loading);
    }
    
    protected void setError(String error) {
        this.error.setValue(error);
    }
    
    protected void clearError() {
        error.setValue(null);
    }
}