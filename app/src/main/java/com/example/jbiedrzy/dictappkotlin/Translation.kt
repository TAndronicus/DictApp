package com.example.jbiedrzy.dictappkotlin

/**
 * Created by JB on 2018-01-03.
 */
class Translation {

    var lang: String? = null
    var sources: List<String>? = null
    var targets: List<String>? = null

    constructor(lang: String, sources: List<String>, targets: List<String>) {
        this.lang = lang
        this.sources = sources
        this.targets = targets
    }

    constructor() {}

}
