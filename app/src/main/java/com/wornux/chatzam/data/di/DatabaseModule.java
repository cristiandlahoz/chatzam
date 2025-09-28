package com.wornux.chatzam.data.di;

import com.wornux.chatzam.data.repositories.ChatRepository;
import com.wornux.chatzam.data.repositories.MessageRepository;
import com.wornux.chatzam.data.repositories.UserRepository;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {
}