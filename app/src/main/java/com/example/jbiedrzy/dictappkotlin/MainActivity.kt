package com.example.jbiedrzy.dictappkotlin

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.ArrayAdapter
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.jbiedrzy.dictappkotlin.Constants.Companion.PonsUrl
import com.example.jbiedrzy.dictappkotlin.Constants.Companion.genus
import com.example.jbiedrzy.dictappkotlin.Constants.Companion.myCharset
import com.example.jbiedrzy.dictappkotlin.Constants.Companion.xSecret
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder.encode


class Dict : AppCompatActivity(), Constants {

    lateinit var repoDatabase: RepoDatabase
    internal var showOnceNotGiven: Boolean = false
    internal var showOnceNotFound: Boolean = false
    internal var showOnceWriteInDb: Boolean = false
    lateinit var translation1: TextView
    lateinit var translation2: TextView
    lateinit var translation3: TextView
    lateinit var translation4: TextView
    lateinit var source1: TextView
    lateinit var source2: TextView
    lateinit var source3: TextView
    lateinit var source4: TextView
    lateinit var language1: TextView
    lateinit var language2: TextView
    lateinit var language3: TextView
    lateinit var language4: TextView
    lateinit var word: EditText
    lateinit var spinner: Spinner
    lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repoDatabase = DictApplication.getDB(applicationContext)
        val search = findViewById<Button>(R.id.search)
        word = findViewById(R.id.word)
        translation1 = findViewById(R.id.translation1)
        translation2 = findViewById(R.id.translation2)
        translation3 = findViewById(R.id.translation3)
        translation4 = findViewById(R.id.translation4)
        val translations = arrayOf(translation1, translation2, translation3, translation4)
        source1 = findViewById(R.id.source1)
        source2 = findViewById(R.id.source2)
        source3 = findViewById(R.id.source3)
        source4 = findViewById(R.id.source4)
        val sources = arrayOf(source1, source2, source3, source4)
        language1 = findViewById(R.id.language1)
        language2 = findViewById(R.id.language2)
        language3 = findViewById(R.id.language3)
        language4 = findViewById(R.id.language4)
        val languages = arrayOf(language1, language2, language3, language4)
        val rememberWord = findViewById<Button>(R.id.rememberWord)
        val quiz = findViewById<Button>(R.id.quiz)
        spinner = findViewById(R.id.spinnerDict)
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        //Search
        search.setOnClickListener {
            if (word.text.toString().length == 0) {
                Toast.makeText(applicationContext, "Podaj słowo", Toast.LENGTH_SHORT).show()
            } else {
                retrieveAndSetUpTranslations(languages, translations, sources)
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            }
        }
        //Remember
        rememberWord.setOnClickListener {
            if (word.text.toString().length == 0) {
                Toast.makeText(applicationContext, "Podaj słowo", Toast.LENGTH_SHORT).show()
            } else {
                val entities = retrieveAndSetUpTranslations(languages, translations, sources)
                showOnceWriteInDb = true
                for (entity in entities) {
                    try {
                        writeEntitiesInDatabase(entity)
                    } catch (e: SQLiteConstraintException) {
                        Toast.makeText(applicationContext, "Już w bazie", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
        //Quiz
        quiz.setOnClickListener {
            val intent = Intent(applicationContext, QuizActivity::class.java)
            startActivity(intent)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                if (word.text.toString().isNotEmpty()) {
                    retrieveAndSetUpTranslations(languages, translations, sources)
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        translation1.text = savedInstanceState.getString("translation1")
        translation2.text = savedInstanceState.getString("translation2")
        translation3.text = savedInstanceState.getString("translation3")
        translation4.text = savedInstanceState.getString("translation4")
        language1.text = savedInstanceState.getString("language1")
        language2.text = savedInstanceState.getString("language2")
        language3.text = savedInstanceState.getString("language3")
        language4.text = savedInstanceState.getString("language4")
        source1.text = savedInstanceState.getString("source1")
        source2.text = savedInstanceState.getString("source2")
        source3.text = savedInstanceState.getString("source3")
        source4.text = savedInstanceState.getString("source4")
        word.setText(savedInstanceState.getString("word"))
        spinner.setSelection(savedInstanceState.getInt("spinnerChoice"))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putString("translation1", translation1.text.toString())
        outState.putString("translation2", translation2.text.toString())
        outState.putString("translation3", translation3.text.toString())
        outState.putString("translation4", translation4.text.toString())
        outState.putString("language1", language1.text.toString())
        outState.putString("language2", language2.text.toString())
        outState.putString("language3", language3.text.toString())
        outState.putString("language4", language4.text.toString())
        outState.putString("source1", source1.text.toString())
        outState.putString("source2", source2.text.toString())
        outState.putString("source3", source3.text.toString())
        outState.putString("source4", source4.text.toString())
        outState.putString("word", word.text.toString())
        outState.putInt("spinnerChoice", spinner.selectedItemPosition)
    }

    private fun writeEntitiesInDatabase(entity: TransEntity) {
        try {
            repoDatabase.repoDao.insert(entity)
        } catch (e: NullPointerException) {
            if (showOnceWriteInDb) {
                Toast.makeText(applicationContext, "Podaj słowo", Toast.LENGTH_SHORT).show()
            }
            showOnceWriteInDb = false
            e.printStackTrace()
        }

    }

    private fun retrieveAndSetUpTranslations(languages: Array<TextView>, translations: Array<TextView>, sources: Array<TextView>): List<TransEntity> {

        showOnceNotGiven = true
        showOnceNotFound = true
        val translateTo = setUpTranslationDirection(spinner)
        val entities = ArrayList<TransEntity>()
        for (i in translateTo.indices) {
            retrieveAndSetUpTranslations(word.text.toString(), translateTo[i], sources[i], translations[i])
            languages[i].text = translateTo[i].replace(spinner.selectedItem.toString().toLowerCase(), "").toUpperCase()
            if(translations[i].text.isEmpty() || word.text.isEmpty() || languages[i].text.isEmpty()) {
                continue
            } else {
                entities.add(TransEntity(translations[i].text.toString(), word.text.toString(), languages[i].text.toString()))
            }

        }
        if (spinner.selectedItem.toString().equals("fr", ignoreCase = true) || spinner.selectedItem.toString().equals("ru", ignoreCase = true)) {
            languages[3].text = ""
            sources[3].text = ""
            translations[3].text = ""
        }
        return entities
    }

    private fun setUpTranslationDirection(spinner: Spinner): List<String> {

        val direction = spinner.selectedItem.toString().toLowerCase()
        val infixes = object : ArrayList<String>() {
            init {
                add("pl")
                add("en")
                add("de")
                add("ru")
                add("fr")
            }
        }
        infixes.remove(direction)
        if (direction == "fr")
            infixes.remove("ru")
        else if (direction == "ru")
            infixes.remove("fr")
        val translateTo = ArrayList<String>(infixes.size)
        for (infix in infixes) {
            translateTo.add(addLanguage(infix, direction))
        }
        return translateTo

    }

    private fun addLanguage(infix: String, direction: String): String {

        return if (infix.compareTo(direction) < 0)
            infix + direction
        else
            direction + infix

    }

    private fun retrieveAndSetUpTranslations(word: String, direction: String, source: TextView, translation: TextView) {
        val queue = Volley.newRequestQueue(application)
        try {
            val query = String.format("q=%s&l=%s",
                    encode(word, myCharset),
                    encode(direction, myCharset))
            val url = PonsUrl + "?" + query
            val stringRequest = object : StringRequest(Request.Method.GET, url,
                    object : Response.Listener<String> {
                        override fun onResponse(response: String) {
                            val result: List<List<String>>
                            try {
                                result = retrieveTranslations(response)
                                setUpTranslation(source, translation, result)
                            } catch (e: Exception) {
                                if (showOnceNotFound) {
                                    Toast.makeText(applicationContext, direction + ": Nie znaleziono podanego słowa", Toast.LENGTH_SHORT).show()
                                }
                                showOnceNotFound = false
                                e.printStackTrace()
                            }

                        }
                    }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    if (showOnceNotGiven) {
                        Toast.makeText(applicationContext, "Nie podano słowa.", Toast.LENGTH_LONG).show()
                    }
                    showOnceNotGiven = false
                }
            }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("X-Secret", xSecret)

                    return params
                }
            }
            queue.add(stringRequest)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

    }

    @Throws(IndexOutOfBoundsException::class)
    private fun setUpTranslation(sourcePlace: TextView, translation: TextView, results: List<List<String>>) {

        val limit = 0
        val sources = results[0]
        val targets = results[1]
        var targetResult = ""
        var iter = 0
        for (target in targets) {
            if (iter > limit) {
                break
            }
            targetResult = targetResult + (if (target.length > 20) target.substring(0, 20) else target) + "\n"
            iter++
        }
        translation.text = targetResult
        iter = 0
        var sourceResult = ""
        for (source in sources) {
            if (iter > limit) {
                break
            }
            sourceResult = sourceResult + (if (source.length > 20) source.substring(0, 20) else source) + "\n"
            iter++
        }
        sourcePlace.text = sourceResult

    }

    @Throws(JSONException::class, StringIndexOutOfBoundsException::class)
    private fun retrieveTranslations(responseBody: String): List<List<String>> {

        var json = responseBody.replace("\r".toRegex(), "").replace("\n".toRegex(), "")
        json = json.substring(1, json.length - 1)
        val jsonObject = JSONObject(json)
        val jsonArray = jsonObject.getJSONArray("hits")

        val sources = ArrayList<String>()
        val targets = ArrayList<String>()
        val result = ArrayList<ArrayList<String>>()

        for (i in 0 until jsonArray.length()) {
            val roms = jsonArray.getJSONObject(i).getJSONArray("roms")
            for (j in 0 until roms.length()) {
                val arabs = roms.getJSONObject(j).getJSONArray("arabs")
                for (k in 0 until arabs.length()) {
                    val translations = arabs.getJSONObject(k).getJSONArray("translations")
                    for (l in 0 until translations.length()) {

                        var source = ""
                        val originalSource = translations.getJSONObject(l).getString("source")
                        originalSource.replace("\n".toRegex(), "")
                        var temp = originalSource.split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (m in 1 until temp.size) {
                            val frag = temp[m].split("<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            source = source + frag[0]
                        }
                        source = source.trim { it <= ' ' }
                        source = source.replace("  ".toRegex(), " ")

                        var target = ""
                        var originalTarget = translations.getJSONObject(l).getString("target")
                        originalTarget = originalTarget.replace("\n".toRegex(), "")
                        originalTarget = removeGenus(originalTarget)
                        temp = originalTarget.split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (m in temp.indices) {
                            val frag = temp[m].split("<".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            target = target + frag[0]
                        }
                        target = target.trim { it <= ' ' }
                        target = target.replace("  ".toRegex(), " ")
                        target = target.replace("&#39;".toRegex(), "'")

                        sources.add(source)
                        targets.add(target)
                    }
                }
            }
        }
        result.add(sources)
        result.add(targets)

        return result

    }

    private fun removeGenus(originalTarget: String): String {
        var originalTarget = originalTarget

        for (i in 0 until genus.size)
            originalTarget = originalTarget.replace(genus.get(i), "")

        return originalTarget
    }

    override fun onDestroy() {
        repoDatabase.close()
        super.onDestroy()
    }

}

