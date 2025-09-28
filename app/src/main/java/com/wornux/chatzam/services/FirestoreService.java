package com.wornux.chatzam.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreService {
    
    private final FirebaseFirestore firestore;
    
    @Inject
    public FirestoreService(FirebaseManager firebaseManager) {
        this.firestore = firebaseManager.getFirestore();
    }
    
    public Task<DocumentReference> addDocument(String collection, Map<String, Object> data) {
        return firestore.collection(collection).add(data);
    }
    
    public Task<DocumentSnapshot> getDocument(String collection, String documentId) {
        return firestore.collection(collection).document(documentId).get();
    }
    
    public Task<Void> updateDocument(String collection, String documentId, Map<String, Object> data) {
        return firestore.collection(collection).document(documentId).update(data);
    }
    
    public Task<Void> deleteDocument(String collection, String documentId) {
        return firestore.collection(collection).document(documentId).delete();
    }
    
    public LiveData<QuerySnapshot> getCollectionRealtime(String collection) {
        MutableLiveData<QuerySnapshot> liveData = new MutableLiveData<>();
        
        firestore.collection(collection)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        liveData.setValue(value);
                    }
                });
        
        return liveData;
    }
    
    public ListenerRegistration addSnapshotListener(String collection, EventListener<QuerySnapshot> listener) {
        return firestore.collection(collection).addSnapshotListener(listener);
    }
    
    public Task<QuerySnapshot> getCollection(String collection) {
        return firestore.collection(collection).get();
    }
    
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}