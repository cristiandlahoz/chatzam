package com.wornux.chatzam.data.services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.database.FirebaseDatabase;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirebaseManager {
    
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final FirebaseMessaging messaging;
    private final FirebaseDatabase database;
    
    @Inject
    public FirebaseManager() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.messaging = FirebaseMessaging.getInstance();
        this.database = FirebaseDatabase.getInstance();
    }
    
    public FirebaseAuth getAuth() {
        return firebaseAuth;
    }
    
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
    
    public FirebaseStorage getStorage() {
        return storage;
    }
    
    public FirebaseMessaging getMessaging() {
        return messaging;
    }
    
    public FirebaseDatabase getDatabase() {
        return database;
    }
}