package com.hikmal.agfarm.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.hikmal.agfarm.data.model.Land

class LandRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("lands")

    suspend fun getAllLands(): List<Land> {
        return try {
            val snapshot = dbRef.get().await()
            val landList = mutableListOf<Land>()
            for (childState in snapshot.children) {
                val land = childState.getValue(Land::class.java)
                if (land != null) landList.add(land)
            }
            landList
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addLand(land: Land): Boolean {
        return try {
            val id = dbRef.push().key ?: return false
            val newLand = land.copy(id = id)
            dbRef.child(id).setValue(newLand).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateLand(land: Land): Boolean {
        return try {
            dbRef.child(land.id).setValue(land).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun deleteLand(landId: String): Boolean {
        return try {
            dbRef.child(landId).removeValue().await()
            true
        } catch (e: Exception) { false }
    }
}