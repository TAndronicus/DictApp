package com.example.jbiedrzy.dictappkotlin

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

/**
 * Created by JB on 2018-01-03.
 */
@Dao
interface RepoDao {

    @get:Query("SELECT * FROM TransEntity")
    val allRepos: List<TransEntity>

    @Insert
    fun insert(entity: TransEntity)

    @Delete
    fun delete(entity: TransEntity)

}