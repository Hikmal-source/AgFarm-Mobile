package com.hikmal.agfarm.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.hikmal.agfarm.data.model.Harvest

class HarvestRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("harvests")

    suspend fun getHarvestHistory(): List<Harvest> {
        return try {
            val snapshot = dbRef.get().await()
            val harvestList = mutableListOf<Harvest>()
            for (childState in snapshot.children) {
                val harvest = childState.getValue(Harvest::class.java)
                if (harvest != null) harvestList.add(harvest)
            }
            harvestList.sortByDescending { it.dateMillis }
            harvestList
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addHarvest(harvest: Harvest): Boolean {
        return try {
            val id = dbRef.push().key ?: return false
            val newHarvest = harvest.copy(id = id)
            dbRef.child(id).setValue(newHarvest).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateHarvest(harvest: Harvest): Boolean {
        return try {
            dbRef.child(harvest.id).setValue(harvest).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteHarvest(harvestId: String): Boolean {
        return try {
            dbRef.child(harvestId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}