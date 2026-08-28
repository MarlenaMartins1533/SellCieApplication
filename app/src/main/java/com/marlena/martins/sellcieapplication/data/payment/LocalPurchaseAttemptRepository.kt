package com.marlena.martins.sellcieapplication.data.payment

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import com.marlena.martins.sellcieapplication.domain.model.PaymentOutcome
import com.marlena.martins.sellcieapplication.domain.model.PaymentRequest
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttempt
import com.marlena.martins.sellcieapplication.domain.model.PurchaseAttemptStatus
import com.marlena.martins.sellcieapplication.domain.model.toAttemptStatus
import com.marlena.martins.sellcieapplication.domain.repository.PurchaseAttemptRepository
import com.marlena.martins.sellcieapplication.domain.repository.StartProcessingResult

private class PurchaseDb(context: Context) : SQLiteOpenHelper(context, "purchases.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE purchase_attempt (
            purchaseId TEXT PRIMARY KEY NOT NULL, eventId TEXT NOT NULL, quantity INTEGER NOT NULL,
            totalInCents INTEGER NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL)""")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

class LocalPurchaseAttemptRepository(context: Context) : PurchaseAttemptRepository {
    private val helper = PurchaseDb(context.applicationContext)

    override suspend fun startProcessing(request: PaymentRequest): StartProcessingResult =
        synchronized(helper) {
            val existing = find(request.purchaseId)
            if (existing != null) {
                return@synchronized if (existing.status == PurchaseAttemptStatus.PROCESSING) {
                    StartProcessingResult.AlreadyProcessing
                } else {
                    StartProcessingResult.AlreadyCompleted(requireNotNull(existing.outcome))
                }
            }
            val attempt = PurchaseAttempt(
                purchaseId = request.purchaseId, totalInCents = request.totalInCents,
                eventId = request.eventId, quantity = request.quantity,
                createdAt = System.currentTimeMillis(), status = PurchaseAttemptStatus.PROCESSING
            )
            save(attempt)
            StartProcessingResult.Started(attempt)
        }

    override suspend fun complete(purchaseId: String, outcome: PaymentOutcome): PurchaseAttempt =
        synchronized(helper) {
            val current = requireNotNull(find(purchaseId)) { "Tentativa não encontrada." }
            check(current.status == PurchaseAttemptStatus.PROCESSING)
            current.copy(status = outcome.toAttemptStatus(), outcome = outcome).also(::save)
        }

    override suspend fun get(purchaseId: String): PurchaseAttempt? = synchronized(helper) { find(purchaseId) }

    private fun find(purchaseId: String): PurchaseAttempt? = helper.readableDatabase.query(
        "purchase_attempt", null, "purchaseId = ?", arrayOf(purchaseId), null, null, null
    ).use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val status = PurchaseAttemptStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status")))
        PurchaseAttempt(
            purchaseId = cursor.getString(cursor.getColumnIndexOrThrow("purchaseId")),
            totalInCents = cursor.getLong(cursor.getColumnIndexOrThrow("totalInCents")),
            eventId = cursor.getString(cursor.getColumnIndexOrThrow("eventId")),
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            status = status,
            outcome = status.toOutcome()
        )
    }

    private fun save(attempt: PurchaseAttempt) {
        val values = ContentValues().apply {
            put("purchaseId", attempt.purchaseId); put("eventId", attempt.eventId)
            put("quantity", attempt.quantity); put("totalInCents", attempt.totalInCents)
            put("status", attempt.status.name); put("createdAt", attempt.createdAt)
        }
        helper.writableDatabase.insertWithOnConflict("purchase_attempt", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}

private fun PurchaseAttemptStatus.toOutcome(): PaymentOutcome? = when (this) {
    PurchaseAttemptStatus.APPROVED -> PaymentOutcome.Approved
    PurchaseAttemptStatus.PENDING -> PaymentOutcome.Pending
    PurchaseAttemptStatus.DECLINED -> PaymentOutcome.Declined
    PurchaseAttemptStatus.CANCELED -> PaymentOutcome.Canceled
    PurchaseAttemptStatus.TECHNICAL_ERROR -> PaymentOutcome.TechnicalError
    PurchaseAttemptStatus.CREATED, PurchaseAttemptStatus.PROCESSING -> null
}
