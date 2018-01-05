package com.example.jbiedrzy.dictappkotlin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*


/**
 * Created by JB on 2018-01-03.
 */
class QuizActivity : AppCompatActivity() {

    lateinit var repoDatabase: RepoDatabase
    internal var rightOne: Int? = null
    lateinit var typ1: Button
    lateinit var typ2: Button
    lateinit var typ3: Button
    lateinit var typ4: Button
    lateinit var wordToMatch: TextView
    lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        spinner = findViewById(R.id.spinnerQuiz)
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_quiz, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        var dict: Button = findViewById(R.id.dict)
        typ1 = findViewById(R.id.typ1)
        typ2 = findViewById(R.id.typ2)
        typ3 = findViewById(R.id.typ3)
        typ4 = findViewById(R.id.typ4)
        val buttons = arrayOf<Button>(typ1, typ2, typ3, typ4)
        wordToMatch = findViewById(R.id.wordToMatch)
        val drawNext: Button = findViewById(R.id.drawNext)

        setUpQuiz(spinner, buttons, wordToMatch)
        dict.setOnClickListener{
                val intent = Intent(applicationContext, Dict::class.java)
                startActivity(intent)
        }
        typ1.setOnClickListener{
                if (0 == rightOne) {
                    typ1.setTextColor(Color.GREEN)
                } else {
                    typ1.setTextColor(Color.RED)
                }
        }
        typ2.setOnClickListener{
                if (1 == rightOne) {
                    typ2.setTextColor(Color.GREEN)
                } else {
                    typ2.setTextColor(Color.RED)
                }
        }
        typ3.setOnClickListener{
                if (2 == rightOne) {
                    typ3.setTextColor(Color.GREEN)
                } else {
                    typ3.setTextColor(Color.RED)
                }
        }
        typ4.setOnClickListener{
                if (3 == rightOne) {
                    typ4.setTextColor(Color.GREEN)
                } else {
                    typ4.setTextColor(Color.RED)
                }
        }
        drawNext.setOnClickListener{
                setUpQuiz(spinner, buttons, wordToMatch)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                setUpQuiz(spinner, buttons, wordToMatch)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
    }

    private fun setUpQuiz(spinner: Spinner, buttons: Array<Button>, wordToMatch: TextView) {
        try {
            repoDatabase = DictApplication.getDB(applicationContext)
            val entities = getEntitiesWithGivenLang(spinner)
            val order = drawElements(entities!!.size)
            rightOne = (Math.random() * 4).toInt()
            for (i in 0..3) {
                buttons[i].setText(entities[order[i]].word)
                buttons[i].setTextColor(Color.BLACK)
            }
            wordToMatch.text = entities[order[rightOne!!]].translation
        } catch (e: NullPointerException) {
            Toast.makeText(applicationContext, "Za mało słów w bazie", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, Dict::class.java)
            startActivity(intent)
        }

    }

    private fun drawElements(size: Int): List<Int> {
        val result = ArrayList<Int>()
        result.add((Math.random() * size).toInt())
        var it = 3
        while (it > 0) {
            val next = (Math.random() * size).toInt()
            if (!result.contains(next)) {
                it--
                result.add(next)
            }
        }
        return result
    }

    private fun getEntitiesWithGivenLang(spinner: Spinner): List<TransEntity>? {
        val entities = repoDatabase.repoDao.allRepos
        var result: MutableList<TransEntity> = ArrayList()
        for (entity in entities) {
            if (entity.lang.equals(spinner.selectedItem.toString(), ignoreCase = true)) {
                result.add(entity)
            }
        }
        if (result.size < 4) {
            for (i in 0..4) {
                result = ArrayList()
                for (entity in entities) {
                    if (entity.lang.equals(spinner.getItemAtPosition(i).toString(), ignoreCase = true)) {
                        result.add(entity)
                    }
                }
                if (result.size > 3) {
                    spinner.setSelection(i)
                    return result
                }
            }
            Toast.makeText(applicationContext, "Zbyt mało słów w bazie", Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, Dict::class.java)
            startActivity(intent)
            return null
        } else {
            return result
        }
    }

    override fun onDestroy() {
        repoDatabase.close()
        super.onDestroy()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        wordToMatch.text = savedInstanceState.getString("wordToMatch")
        typ1.setText(savedInstanceState.getString("typ1"))
        typ2.setText(savedInstanceState.getString("typ2"))
        typ3.setText(savedInstanceState.getString("typ3"))
        typ4.setText(savedInstanceState.getString("typ4"))
        spinner.setSelection(savedInstanceState.getInt("spinnerSelection"))
        typ1.setTextColor(savedInstanceState.getInt("typ1Color"))
        typ2.setTextColor(savedInstanceState.getInt("typ2Color"))
        typ3.setTextColor(savedInstanceState.getInt("typ3Color"))
        typ4.setTextColor(savedInstanceState.getInt("typ4Color"))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString("wordToMatch", wordToMatch.text.toString())
        outState.putString("typ1", typ1.getText().toString())
        outState.putString("typ2", typ2.getText().toString())
        outState.putString("typ3", typ3.getText().toString())
        outState.putString("typ4", typ4.getText().toString())
        outState.putInt("spinnerSelection", spinner.selectedItemPosition)
        outState.putInt("typ1Color", typ1.getCurrentTextColor())
        outState.putInt("typ2Color", typ2.getCurrentTextColor())
        outState.putInt("typ3Color", typ3.getCurrentTextColor())
        outState.putInt("typ4Color", typ4.getCurrentTextColor())
    }
}