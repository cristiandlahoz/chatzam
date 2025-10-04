package com.wornux.chatzam.data.repositories.base;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.wornux.chatzam.utils.FirestoreNamingUtils;
import java.util.Map;

public abstract class BaseRepository<T> {
    
    protected final FirebaseFirestore firestore;
    protected final String collectionName;
    protected final Class<T> entityClass;
    
    protected BaseRepository(FirebaseFirestore firestore, Class<T> entityClass) {
        this.firestore = firestore;
        this.entityClass = entityClass;
        this.collectionName = FirestoreNamingUtils.toCollectionName(entityClass.getSimpleName());
    }
    
    public Task<DocumentReference> addDocument(Map<String, Object> data) {
        return firestore.collection(collectionName).add(data);
    }
    
    public Task<DocumentSnapshot> getDocument(String documentId) {
        return firestore.collection(collectionName).document(documentId).get();
    }
    
    public Task<Void> updateDocument(String documentId, Map<String, Object> data) {
        return firestore.collection(collectionName).document(documentId).update(data);
    }
    
    public Task<Void> deleteDocument(String documentId) {
        return firestore.collection(collectionName).document(documentId).delete();
    }
    
    public LiveData<QuerySnapshot> getCollectionRealtime() {
        MutableLiveData<QuerySnapshot> liveData = new MutableLiveData<>();
        
        firestore.collection(collectionName)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        liveData.setValue(value);
                    }
                });
        
        return liveData;
    }
    
    public void addSnapshotListener(EventListener<QuerySnapshot> listener) {
        firestore.collection(collectionName).addSnapshotListener(listener);
    }
    
    public Task<QuerySnapshot> getCollection() {
        return firestore.collection(collectionName).get();
    }
}
