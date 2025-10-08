package com.wornux.chatzam.services;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;

@Getter
@Singleton
public class FirebaseManager {

  private final FirebaseAuth firebaseAuth;
  private final FirebaseFirestore firestore;
  private final FirebaseStorage storage;
  private final FirebaseMessaging messaging;

  @Inject
  public FirebaseManager(@ApplicationContext Context context) {
    if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context);

    this.firebaseAuth = FirebaseAuth.getInstance();
    this.firestore = FirebaseFirestore.getInstance();
    this.storage = FirebaseStorage.getInstance();
    this.messaging = FirebaseMessaging.getInstance();
  }
}
