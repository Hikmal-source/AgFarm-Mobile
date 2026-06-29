package com.hikmal.agfarm.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.hikmal.agfarm.data.model.Sale

class SaleRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("sales")

    suspend fun getSaleHistory(): List<Sale> {
        return try {
            val snapshot = dbRef.get().await()
            val saleList = mutableListOf<Sale>()
            for (childState in snapshot.children) {
                val sale = childState.getValue(Sale::class.java)
                if (sale != null) saleList.add(sale)
            }
            saleList.sortByDescending { it.dateMillis }
            saleList
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSale(sale: Sale): Boolean {
        return try {
            val id = dbRef.push().key ?: return false
            val newSale = sale.copy(id = id)
            dbRef.child(id).setValue(newSale).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateSale(sale: Sale): Boolean {
        return try {
            dbRef.child(sale.id).setValue(sale).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun deleteSale(saleId: String): Boolean {
        return try {
            dbRef.child(saleId).removeValue().await()
            true
        } catch (e: Exception) { false }
    }
}