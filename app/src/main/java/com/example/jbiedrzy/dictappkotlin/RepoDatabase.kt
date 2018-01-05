package com.example.jbiedrzy.dictappkotlin

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * Created by JB on 2018-01-03.
 */
@Database(entities = arrayOf(TransEntity::class), version = 1, exportSchema = false)
abstract class RepoDatabase : RoomDatabase() {

    abstract val repoDao: RepoDao

    companion object {

        private val DB_NAME = "repoDatabase.db"
        @Volatile private var instance: RepoDatabase? = null

        @Synchronized
        fun getInstance(context: Context): RepoDatabase? {
            if (instance == null) {
                instance = create(context)
            }
            return instance
        }

        private fun create(context: Context): RepoDatabase {
            return Room.databaseBuilder(
                    context,
                    RepoDatabase::class.java,
                    DB_NAME).build()
        }
    }
}