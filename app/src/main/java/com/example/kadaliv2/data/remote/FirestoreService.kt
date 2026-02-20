package com.example.kadaliv2.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Wrapper around [FirebaseFirestore] providing generic CRUD helpers and real-time Flow listeners.
 */
class FirestoreService {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Get all documents from a collection as a real-time Flow.
     */
    fun getDocumentsFlow(collection: String): Flow<List<Pair<String, Map<String, Any>>>> = callbackFlow {
        val subscription = firestore.collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val data = snapshot.documents.mapNotNull { doc ->
                        val docData = doc.data
                        if (docData != null) doc.id to docData else null
                    }
                    trySend(data)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Get documents filtered by a field as a real-time Flow.
     */
    fun getDocumentsByFieldFlow(
        collection: String,
        field: String,
        value: Any
    ): Flow<List<Pair<String, Map<String, Any>>>> = callbackFlow {
        val subscription = firestore.collection(collection)
            .whereEqualTo(field, value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val data = snapshot.documents.mapNotNull { doc ->
                        val docData = doc.data
                        if (docData != null) doc.id to docData else null
                    }
                    trySend(data)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Get a single document as a real-time Flow.
     */
    fun getDocumentFlow(collection: String, documentId: String): Flow<Map<String, Any>?> = callbackFlow {
        val subscription = firestore.collection(collection).document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.data)
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Add a document to a collection. Returns the generated Firestore document ID.
     */
    suspend fun addDocument(collection: String, data: Map<String, Any?>): String {
        val docRef = firestore.collection(collection).add(data).await()
        return docRef.id
    }

    /**
     * Update an existing document or create it if it doesn't exist (using set with documentId).
     */
    suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any?>) {
        firestore.collection(collection).document(documentId).set(data).await()
    }

    /**
     * Delete a document by its Firestore document ID.
     */
    suspend fun deleteDocument(collection: String, documentId: String) {
        firestore.collection(collection).document(documentId).delete().await()
    }

    /**
     * Get a single document by its Firestore document ID (one-shot).
     */
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? {
        val snapshot = firestore.collection(collection).document(documentId).get().await()
        return snapshot.data
    }

    /**
     * Get all documents from a collection as a list of maps (one-shot).
     */
    suspend fun getDocuments(collection: String): List<Pair<String, Map<String, Any>>> {
        val snapshot = firestore.collection(collection).get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data
            if (data != null) doc.id to data else null
        }
    }
}
