package com.wornux.chatzam.data.di;

import com.wornux.chatzam.data.repositories.ChatRepositoryImpl;
import com.wornux.chatzam.data.repositories.MessageRepositoryImpl;
import com.wornux.chatzam.data.repositories.UserRepositoryImpl;
import com.wornux.chatzam.domain.repositories.ChatRepository;
import com.wornux.chatzam.domain.repositories.MessageRepository;
import com.wornux.chatzam.domain.repositories.UserRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public abstract class DatabaseModule {
    
    @Binds
    @Singleton
    public abstract UserRepository bindUserRepository(UserRepositoryImpl userRepositoryImpl);
    
    @Binds
    @Singleton
    public abstract MessageRepository bindMessageRepository(MessageRepositoryImpl messageRepositoryImpl);
    
    @Binds
    @Singleton
    public abstract ChatRepository bindChatRepository(ChatRepositoryImpl chatRepositoryImpl);
}