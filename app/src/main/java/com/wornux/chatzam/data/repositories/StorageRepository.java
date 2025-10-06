package com.wornux.chatzam.data.repositories;

import android.net.Uri;
import com.google.android.gms.tasks.Task;
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
                        throw Objects.requireNonNull(task.getException());
                    }
                    return imageRef.getDownloadUrl();
                });
    }
    
    public Task<Uri> uploadVideo(Uri videoUri, String fileName) {
        StorageReference videoRef = storageRef.child("videos/" + fileName + "_" + UUID.randomUUID().toString());
        
        return videoRef.putFile(videoUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return videoRef.getDownloadUrl();
                });
    }
    
    public Task<Uri> uploadDocument(Uri documentUri, String fileName) {
        StorageReference docRef = storageRef.child("documents/" + fileName + "_" + UUID.randomUUID().toString());
        
        return docRef.putFile(documentUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return docRef.getDownloadUrl();
                });
    }
    
    public Task<byte[]> downloadFile(String url) {
        StorageReference fileRef = storage.getReferenceFromUrl(url);
        return fileRef.getBytes(Long.MAX_VALUE);
    }
    
    public Task<Void> deleteFile(String url) {
        StorageReference fileRef = storage.getReferenceFromUrl(url);
        return fileRef.delete();
    }
    
    public Task<Uri> getDownloadUrl(String path) {
        return storageRef.child(path).getDownloadUrl();
    }
}
