package com.example.logonrm.intentservice

import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import android.text.TextUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL



class DownloadService : IntentService(DownloadService::class.java.name) {
    companion object {
        val STATUS_RUNNING = 0
        val STATUS_FINISHED = 1
        val STATUS_ERROR = 2
    }

    override fun onHandleIntent(intent: Intent?) {
        val receiver = intent!!.getParcelableExtra<ResultReceiver>("receiver")
        val url = intent.getStringExtra("url")
        val bundle = Bundle()
        if(!TextUtils.isEmpty(url)) {
            receiver.send(STATUS_RUNNING, Bundle.EMPTY)
            try {
                val results = downloadData(url)
            }catch (e: Exception){
                bundle.putString(Intent.EXTRA_TEXT, e.toString())
                receiver.send(STATUS_ERROR, bundle)
            }
        }
        this.stopSelf()

    }
    @Throws(IOException::class, Exception::class)
    private fun downloadData(requestUrl : String): List<String?>{
        var inputStream: InputStream?
        var urlConnection: HttpURLConnection?
        var url = URL(requestUrl)

        urlConnection = url .openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Content-Type", "Application/json")
        urlConnection.setRequestProperty("Accept", "Application/json")
        urlConnection.requestMethod = "GET"

        val statusCode = urlConnection.responseCode

        if(statusCode == 200){
            inputStream = BufferedInputStream(urlConnection.inputStream)
            val response = convertInputStream(inputStream)
            val result = parseResult(response)
            return result.toList()
        }else{
            throw Exception("Falha nos dados")
        }
    }

    @Throws(IOException::class)
    private fun convertInputStream(inputStream: InputStream?): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream!!))
        var line = bufferedReader.readLine()
        var result = "";

        while(line != null){
            result += line
            line = bufferedReader.readLine()
        }
        inputStream?.close()

        return result

    }

    private fun parseResult(result: String): Array<String?> {
        var nomePokemons: Array<String?> = arrayOf()
        try{
            val response = JSONObject(result)
            val pokemons = response.optJSONArray("results")
            nomePokemons = arrayOfNulls(pokemons.length())
            for (i in 0 until pokemons.length()){
                val pokemon = pokemons.optJSONObject(i)
                val nomePokemon = pokemon.optString("name")
                nomePokemons[i] = nomePokemon
            }
        }catch (e: JSONException){
            e.printStackTrace()
        }
        return nomePokemons
    }


}