package dictionary

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URI
import java.util.concurrent.Executors

object Repository {

    private const val HEADER_KEY = "X-Api-Key"
    private const val API_KEY = "uqmWHHXR2lNf4LOTEaeV0g==xvrLTy4q8yyxXNFQ"
    private const val BASE_URL = "https://api.api-ninjas.com/v1/dictionary?word="

    // Необходимо указать явно, что неиспользуемые ключи объекта JSON мы игнорируем
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadDefinition(word: String): String {
        return withContext(Dispatchers.IO) {
            val connection: HttpURLConnection? = null
            try {
                val urlString = BASE_URL + word // Есть адрес в интернете по которому мы хотим получить данные

                val url = URI.create(urlString).toURL() /* Этот адрес мы преобразуем в объект URL,
                чтобы можно было перейти по этому адресу, открыв соединение */

                val connection = url.openConnection() as HttpURLConnection /* Переходим по адресу, открывая
                соединение. Но в объекте, который мы получаем, под капотом происходит upcast к родительскому типу и нужных
                методов для чтения ответа у него нет, для того, чтобы эти методы появились, мы этот объект приводим к дочернему
                типу HttpURLConnection */

                // Передаем заголовок (можно сделать через функцию apply{})
                connection.addRequestProperty(HEADER_KEY, API_KEY)

                // Чтение потока от сервера в виде строки формата JSON
                val response = connection.inputStream.bufferedReader().readText()

                json.decodeFromString<Definition>(response).definition

            } catch (e: Exception) {
                println(e)
                ""
            } finally { // Код в этом блоке выполнится вне зависимости от результата выполнения блока try/catch
                connection?.disconnect() // Закрытие соединения
            }
        }
    }
}

private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
private val scope = CoroutineScope(dispatcher)

fun main() {
    // Всю работу с интернетом необходимо выносить в фоновый поток
    scope.launch {
        while (true) {
            println("Enter word: ")
            val word = readln()
            val definition = Repository.loadDefinition(word)
            println(definition)
        }
    }
}