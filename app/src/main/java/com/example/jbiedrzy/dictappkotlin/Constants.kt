package com.example.jbiedrzy.dictappkotlin

import java.util.*


/**
 * Created by JB on 2018-01-03.
 */
interface Constants {
    companion object {

        val PonsUrl = "https://api.pons.com/v1/dictionary"
        val myCharset = java.nio.charset.StandardCharsets.UTF_8.name().toString()
        val xSecret = "37a74c966dc0ab760d257f25cef507aa733d25a438a9a007e0f0cc72e3c5bb36"//"2f97dfe4e65874ed59f2fc4a335a6b9c97bd75406e89e9c3559d4becbeeed3d0";
        val genus = Arrays.asList(
                "<span class=\"genus\"><acronym title=\"masculine\">m</acronym></span>",
                "<span class=\"genus\"><acronym title=\"neuter\">nt</acronym></span>",
                "<span class=\"genus\"><acronym title=\"feminine\">f</acronym></span>",
                "<span class=\"genus\"><acronym title=\"masculine\">м</acronym></span>",
                "<span class=\"genus\"><acronym title=\"neuter\">ср</acronym></span>",
                "<span class=\"genus\"><acronym title=\"feminine\">ж</acronym></span>")
    }

}