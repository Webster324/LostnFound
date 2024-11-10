package com.joe.assignment2;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Database {

    private FirebaseFirestore db;

    public Database() {
        db = FirebaseFirestore.getInstance();
    }


    /**
     * Create Record in firestore
     * @param collection - Collection Name
     * @param data - Data of the record
     * @param documentId - The document id of the record
     * @param onSuccess - Success listener
     * @param onFailure - Failure listener
     */
    public void createDocument(String collection, Map<String, Object> data, String documentId,
                               OnSuccessListener<Void> onSuccess,
                               OnFailureListener onFailure) {
        CollectionReference collectionReference = db.collection(collection);
        DocumentReference documentReference;

        documentReference = collectionReference.document(documentId);


        documentReference.set(data)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    /**
     * Update Record in firestore
     * @param collection - Collection Name
     * @param data - Data of the record
     * @param documentId - The document id of the record
     * @param onSuccess - Success listener
     * @param onFailure - Failure listener
     */
    public void updateDocument(String collection, String documentId, Map<String, Object> data,
                               OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        DocumentReference documentReference = db.collection(collection).document(documentId);

        documentReference.update(data)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Delete Record from firestore
     * @param collection - Collection Name
     * @param documentId - The document id of the record
     * @param onSuccess - Success listener
     * @param onFailure - Failure listener
     */
    public void deleteDocument(String collection, String documentId,
                               OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        DocumentReference documentReference = db.collection(collection).document(documentId);

        documentReference.delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Get Record from firestore
     * @param collectionName - Collection Name
     * @param documentId - The document id of the record
     * @param listener - Listener
     */
    public void getDocument(String collectionName, String documentId, final OnDocumentLoadedListener listener) {
        DocumentReference docRef = db.collection(collectionName).document(documentId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    listener.onDocumentLoaded(document.getData());
                } else {
                    listener.onDocumentNotFound();
                }
            } else {
                listener.onDocumentLoadFailed(task.getException().getMessage());
            }
        });
    }

    /**
     * Get Record from firestore with condition
     */
    public void getDocumentsWithCondition(String collectionName, String fieldName, Object value, OnDocumentsLoadedListener listener) {
        CollectionReference collectionRef = FirebaseFirestore.getInstance().collection(collectionName);


        Query query = collectionRef.whereEqualTo(fieldName, value);


        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                List<Map<String, Object>> dataList = new ArrayList<>();

                for (DocumentSnapshot document : documents) {
                    String docId = document.getId();
                    Map<String, Object> data = document.getData();
                    data.put("docId", docId);
                    dataList.add(data);
                }

                listener.onDocumentLoaded(dataList);
            } else {
                listener.onDocumentLoadFailed(task.getException().getMessage());
            }
        });
    }


    // Listener interface for document loading events
    public interface OnDocumentLoadedListener {
        void onDocumentLoaded(Map<String, Object> data);
        void onDocumentNotFound();
        void onDocumentLoadFailed(String errorMessage);
    }
    public interface OnDocumentsLoadedListener {
        void onDocumentLoaded(List<Map<String, Object>> documents);
        void onDocumentNotFound();
        void onDocumentLoadFailed(String errorMessage);
    }
}

