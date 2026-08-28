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

private class PurchaseDb(context: Context) : SQLiteOpenHelper(context, "purchases.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE purchase_attempt (
            purchaseId TEXT PRIMARY KEY NOT NULL, eventId TEXT NOT NULL, quantity INTEGER NOT NULL,
            totalInCents INTEGER NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL)""")
        createItemsTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createItemsTable(db)
    }

    private fun createItemsTable(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS purchase_item (
            purchaseId TEXT NOT NULL, eventId TEXT NOT NULL, title TEXT NOT NULL,
            quantity INTEGER NOT NULL, unitPriceInCents INTEGER NOT NULL,
            PRIMARY KEY(purchaseId, eventId))""")
    }
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
                items = request.items,
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
            items = findItems(purchaseId),
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
        helper.writableDatabase.useTransaction { database ->
            database.insertWithOnConflict("purchase_attempt", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            database.delete("purchase_item", "purchaseId = ?", arrayOf(attempt.purchaseId))
            attempt.items.forEach { item ->
                database.insertOrThrow(
                    "purchase_item",
                    null,
                    ContentValues().apply {
                        put("purchaseId", attempt.purchaseId)
                        put("eventId", item.eventId)
                        put("title", item.title)
                        put("quantity", item.quantity)
                        put("unitPriceInCents", item.unitPriceInCents)
                    }
                )
            }
        }
    }

    private fun findItems(purchaseId: String) = helper.readableDatabase.query(
        "purchase_item", null, "purchaseId = ?", arrayOf(purchaseId), null, null, "rowid"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) {
                add(
                    com.marlena.martins.sellcieapplication.domain.model.PurchasedTicket(
                        eventId = cursor.getString(cursor.getColumnIndexOrThrow("eventId")),
                        title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        unitPriceInCents = cursor.getLong(cursor.getColumnIndexOrThrow("unitPriceInCents"))
                    )
                )
            }
        }
    }
}

private inline fun SQLiteDatabase.useTransaction(block: (SQLiteDatabase) -> Unit) {
    beginTransaction()
    try {
        block(this)
        setTransactionSuccessful()
    } finally {
        endTransaction()
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
