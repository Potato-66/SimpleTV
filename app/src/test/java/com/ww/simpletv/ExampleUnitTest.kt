package com.ww.simpletv

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

import org.junit.Assert.*
import java.io.File
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val f = File("F:\\PythonProject\\itvlist.m3u")
        val lines = f.readLines()
        val set = linkedSetOf<String>()
        lines.forEach {
            if (it.startsWith("#EXTINF")) {
                val tv = it.substring(it.lastIndexOf(",") + 1)
                set.add(tv)
            }
        }
//        val input = "#EXTINF:-1 tvg-id=\"A01\" tvg-name=\"CCTV1\" tvg-logo=\"https://cdn.jsdelivr.net/gh/wanglindl/TVlogo@main/img/CCTV1.png\" group-title=\"央视高清\",CCTV-1 综合"
//        val regex = Regex("tvg-logo=\"(.*?)\"")
//        val result = regex.find(input)?.groups?.get(1)?.value
        println("set size：${set.size}")
        val client = OkHttpClient.Builder().build()
        client.newCall(Request.Builder().url("http://ipv6.vm3.test-ipv6.com/ip/?callback=?&testdomain=test-ipv6.com&testname=test_aaaa").build()).execute().use {
            println("response code:${it.code}")
        }
    }
}