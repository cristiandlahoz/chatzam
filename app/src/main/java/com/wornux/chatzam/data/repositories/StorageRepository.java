package com.wornux.chatzam.data.repositories;

import android.net.Uri;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wornux.chatzam.services.FirebaseManager;

import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StorageRepository {
    
    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    
    @Inject
    public StorageRepository(FirebaseManager firebaseManager) {
        this.storage = firebaseManager.getStorage();
        this.storageRef = storage.getReference();
    }
    
    public Task<Uri> uploadImage(Uri imageUri, String fileName) {
        StorageReference imageRef = storageRef.child("images/" + fileName + "_" + UUID.randomUUID().toString());
        
        return imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        Tasks.forException(Objects.requireNonNull(task.getException()));
                    }
                    return imageRef.getDownloadUrl();
                });
    }
}
