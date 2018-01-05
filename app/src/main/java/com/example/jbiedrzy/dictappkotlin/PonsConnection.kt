package com.example.jbiedrzy.dictappkotlin

import org.json.JSONObject
import org.json.JSONException
import android.os.AsyncTask
import com.example.jbiedrzy.dictappkotlin.Constants.Companion.myCharset
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.util.*


/**
 * Created by JB on 2018-01-03.
 */
class PonsConnection : AsyncTask<Array<String>, Void, List<Translation>>(), Constants {

    override fun doInBackground(vararg strings: Array<String>): List<Translation> {
        return getPONSTranslation(strings[0][0], strings[0][1])
    }

    companion object {

        private var response: InputStream? = null
        private var responseBody = ""
        private var scanner: Scanner? = null

        fun getPONSTranslation(direction: String, word: String): List<Translation> {

            val translateTo = setUpTranslationDirection(direction)
            val translations = ArrayList<Translation>()

            for (i in translateTo.indices)
                try {
                    translations.add(getTranslationFromServer(word, translateTo[i]))
                } catch (e: NoSuchElementException) {
                    translations.add(Translation())
                    e.printStackTrace()
                } catch (e: FileNotFoundException) {
                    translations.add(Translation())
                    e.printStackTrace()
                } catch (e: JSONException) {
                    translations.add(Translation())
                    e.printStackTrace()
                }

            return translations
        }

        @Throws(FileNotFoundException::class, JSONException::class)
        private fun getTranslationFromServer(word: String, direction: String): Translation {

            var query = ""
            try {
                query = String.format("q=%s&l=%s",
                        URLEncoder.encode(word, myCharset),
                        URLEncoder.encode(direction, myCharset))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            var connection: URLConnection? = null
            try {
                connection = URL(Constants.PonsUrl + "?" + query).openConnection()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            connection!!.setDoOutput(true)
            connection!!.setRequestProperty("Accept-Charset", myCharset)
            connection!!.setRequestProperty("X-Secret", Constants.xSecret)

            try {
                response = connection!!.getInputStream()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            scanner = Scanner(response)
            responseBody = scanner!!.useDelimiter("\\A").next()

            var json = responseBody.replace("\r".toRegex(), "").replace("\n".toRegex(), "")
            json = json.substring(1, json.length - 1)
            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("hits")

            val sources = ArrayList<String>()
            val targets = ArrayList<String>()
            val lang = direction.replace(jsonObject.getString("lang").toRegex(), "")

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
                                source += frag[0]
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
                                target += frag[0]
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

            return Translation(lang, sources, targets)
        }

        private fun removeGenus(originalTarget: String): String {
            var originalTarget = originalTarget

            for (i in 0 until Constants.genus.size)
                originalTarget = originalTarget.replace(Constants.genus.get(i), "")

            return originalTarget
        }

        private fun setUpTranslationDirection(direction: String): List<String> {
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
    }
}