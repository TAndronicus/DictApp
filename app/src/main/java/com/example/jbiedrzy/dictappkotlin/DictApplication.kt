package com.example.jbiedrzy.dictappkotlin

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context


/**
 * Created by JB on 2018-01-03.
 */
class DictApplication : Application() {
    companion object {

        private lateinit var repoDatabase: RepoDatabase
        private var wasDBInitialized = false

        fun getDB(context: Context): RepoDatabase {
            if (!wasDBInitialized) {
                repoDatabase = Room.databaseBuilder(context, RepoDatabase::class.java, "dictDB0").allowMainThreadQueries().build()
                wasDBInitialized = true
            }
            return repoDatabase
        }

        private fun doesDatabaseExist(context: Context, dbName: String): Boolean {
            val dbFile = context.getDatabasePath(dbName)
            return dbFile.exists()
        }
    }

}