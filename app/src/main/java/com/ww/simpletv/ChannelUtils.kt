package com.ww.simpletv

import android.content.Context
import android.util.Log
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/26
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
object ChannelUtils {
    val channelSet = linkedSetOf<TV>()

    suspend fun updateChannel(context: Context): Boolean {
        channelSet.clear()
        parseChannel(context)
        return channelSet.isNotEmpty()
    }

    suspend fun parseChannel(context: Context) {
        return withContext(Dispatchers.IO) {
            val file = getChannelFile(context)
            if (file == null) {
                Log.e("ww", "parseChannel: iptv file not exist")
                return@withContext
            }
            val lines = file.readLines()
            if (!lines[0].startsWith("#EXTM3U")) {
                Log.e("ww", "parseChannel: Non standard m3u8 file, parsing error")
                return@withContext
            }
            var id = ""
            var name = ""
            var group = ""
            var logo = ""
            lines.forEach { line ->
                if (line.startsWith("#EXTINF")) {
                    line.split(" ")
                    Regex("tvg-id=\"([^\"]+)\"").find(line)?.groups?.get(1)?.let {
                        id = it.value
                    }
                    Regex("tvg-logo=\"(.*?)\"").find(line)?.groups?.get(1)?.let {
                        logo = it.value
                    }
                    Regex("group-title=\"([^\"]*)\"").find(line)?.groups?.get(1)?.let {
                        group = it.value
                    }
                    name = line.substring(line.indexOf(",") + 1)
                } else if (line.startsWith("http")) {
                    channelSet.add(TV(id, name, group, logo, url = line))
                }
            }
        }
    }

    private fun getChannelFile(context: Context): File? {
        val file = File(context.filesDir, Constant.FILE_NAME)
        return if (!file.exists()) {
            if (downloadIPTVFile(file)) file else null
        } else {
            if (MMKV.defaultMMKV().decodeBool(Constant.KEY_AUTO_UPDATE, true)) {
                val lastModified = file.lastModified()
                if (System.currentTimeMillis() - lastModified > 24 * 60 * 60 * 1000) {
                    try {
                        if (downloadIPTVFile(file)) {
                            Log.e("ww", "getChannelFile update iptv file success")
                        } else {
                            Log.e("ww", "getChannelFile update iptv file fail")
                        }
                    } catch (e:Exception) {
                        e.printStackTrace()
                        Log.e("ww", "auto update iptv file fail")
                    }
                }
            }
            file
        }
    }

    fun downloadIPTVFile(file: File): Boolean {
        val client = OkHttpClient.Builder().build()
        val url = API.URL_M3U
        return client.newCall(Request.Builder().url(url).build()).execute().use {
            if (it.isSuccessful) {
                file.createNewFile()
                it.body?.let { body ->
                    file.writeText(body.string())
                    true
                } ?: false
            } else {
                Log.e("ww", "download iptv file fail${it.message}}")
                false
            }
        }
    }
}