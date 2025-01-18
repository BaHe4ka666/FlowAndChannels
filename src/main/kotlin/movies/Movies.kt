package movies

import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.coroutineContext
import kotlin.time.measureTime

private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
private val scope = CoroutineScope(dispatcher)


fun main() {
    val job = scope.launch {
        val time = measureTime {
            loadMovies()
        }
        println(time)
    }
    scope.launch {
        delay(4000)
        job.cancel()
    }
}

private suspend fun loadMoviesIds(): List<Int> {
    delay(3000)
    return (0..100).toList()
}

private suspend fun loadMovieById(id: Int): String {
    delay(5000)
    return "Movie $id"
}

private suspend fun loadMovies(): List<String> {
    return coroutineScope {
        loadMoviesIds().map {
            async {
                loadMovieById(it).also {
                    println(it)
                }
            }
        }.awaitAll()
    }

}


