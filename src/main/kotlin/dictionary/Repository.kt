package dictionary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URI

object Repository {

    private const val HEADER_KEY = "X-Api-Key"
    private const val API_KEY = "uqmWHHXR2lNf4LOTEaeV0g==xvrLTy4q8yyxXNFQ"
    private const val BASE_URL = "https://api.api-ninjas.com/v1/dictionary?word="

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadDefinition(word: String): List<String> {
        return withContext(Dispatchers.IO) {
            val connection: HttpURLConnection? = null
            try {
                val urlString = BASE_URL + word
                val url = URI.create(urlString).toURL()
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    addRequestProperty(HEADER_KEY, API_KEY)
                    readTimeout = 5000
                }
                val response = connection.inputStream.bufferedReader().readText()
                json.decodeFromString<Definition>(response).mapDefinitionToString()
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun Definition.mapDefinitionToString(): List<String> {
        return this.definition.split(Regex("\\d. ")).map { it.trim() }.filter { it.isNotEmpty() }
    }
}


