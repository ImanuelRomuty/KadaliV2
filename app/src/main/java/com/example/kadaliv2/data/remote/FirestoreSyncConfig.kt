package com.example.kadaliv2.data.remote

/**
 * Feature flag for controlled Firestore sync rollout.
 * Set [isEnabled] to false to instantly disable all remote sync operations.
 */
object FirestoreSyncConfig {
    var isEnabled: Boolean = true
}
