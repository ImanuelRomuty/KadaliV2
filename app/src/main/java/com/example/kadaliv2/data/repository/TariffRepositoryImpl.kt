package com.example.kadaliv2.data.repository

import com.example.kadaliv2.data.remote.FirestoreService
import com.example.kadaliv2.domain.model.Tariff
import com.example.kadaliv2.domain.repository.TariffRepository
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default PLN residential tariff (Rp/kWh), used when no tariff has been stored yet. */
private const val DEFAULT_TARIFF_PER_KWH = 1444.70

class TariffRepositoryImpl(
    private val firestoreService: FirestoreService
) : TariffRepository {

    private val collection = "config"
    private val document = "global"

    override fun getTariff(): Flow<Tariff?> {
        return firestoreService.getDocumentFlow(collection, document).map { data ->
            // If the document doesn't exist yet, fall back to the default PLN rate
            // so the dashboard always shows a meaningful cost estimate.
            Tariff(
                pricePerKwh = (data?.get("tariffPerKwh") as? Number)?.toDouble()
                    ?: DEFAULT_TARIFF_PER_KWH
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
