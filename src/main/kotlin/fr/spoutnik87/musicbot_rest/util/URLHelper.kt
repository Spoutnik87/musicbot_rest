package fr.spoutnik87.musicbot_rest.util

import java.net.URL

class URLHelper {

    companion object {
        fun getQueryParameters(link: String) = getQueryParameters(URL(link))

        fun getQueryParameters(link: URL): List<Pair<String, String>> {
            return link.query.split("&").mapNotNull {
                val index = it.indexOf("=")
                if (index == -1) {
                    null
                } else {
                    Pair(it.substring(0, index), it.substring(index + 1))
                }
            }
        }
    }
}