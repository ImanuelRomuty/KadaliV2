package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.remote.FirestoreService
import com.example.kadaliv2.domain.model.Tariff
import com.example.kadaliv2.domain.repository.TariffRepository
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TariffRepositoryImpl(
    private val firestoreService: FirestoreService
) : TariffRepository {

    private val collection = "config"
    private val document = "global"

    override fun getTariff(): Flow<Tariff?> {
        return firestoreService.getDocumentFlow(collection, document).map { data ->
            if (data == null) return@map null
            Tariff(
                pricePerKwh = (data["tariffPerKwh"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }

    override suspend fun saveTariff(tariff: Tariff) {
        val data = mapOf(
            "tariffPerKwh" to tariff.pricePerKwh,
            "currency" to "IDR",
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestoreService.updateDocument(collection, document, data)
    }
}
