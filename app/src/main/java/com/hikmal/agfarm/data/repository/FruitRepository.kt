package com.hikmal.agfarm.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.hikmal.agfarm.data.model.Fruit

class FruitRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("fruits")

    suspend fun getAllFruits(): List<Fruit> {
        return try {
            val snapshot = dbRef.get().await()
            val fruitList = mutableListOf<Fruit>()
            for (childState in snapshot.children) {
                val fruit = childState.getValue(Fruit::class.java)
                if (fruit != null) {
                    fruitList.add(fruit)
                }
            }
            fruitList
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFruit(fruit: Fruit): Boolean {
        return try {
            val id = dbRef.push().key ?: return false
            val newFruit = fruit.copy(id = id) // Pastikan id unik Firebase masuk ke model
            dbRef.child(id).setValue(newFruit).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateFruit(fruit: Fruit): Boolean {
        return try {
            dbRef.child(fruit.id).setValue(fruit).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun deleteFruit(fruitId: String): Boolean {
        return try {
            dbRef.child(fruitId).removeValue().await()
            true
        } catch (e: Exception) { false }
    }
}