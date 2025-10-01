package com.wornux.chatzam.data.di;

import android.content.Context;
import android.content.SharedPreferences;
import com.wornux.chatzam.utils.PreferenceConstants;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(@ApplicationContext Context ctx){
        return ctx.getSharedPreferences(PreferenceConstants.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }
}