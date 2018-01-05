package com.example.jbiedrzy.dictappkotlin

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull


/**
 * Created by JB on 2018-01-03.
 */
@Entity
class TransEntity(@field:PrimaryKey @NonNull
                  var word: String, var translation: String, var lang: String){

    constructor(): this("", "", "")
}
