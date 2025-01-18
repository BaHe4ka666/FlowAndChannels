package flowOn

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors

private val dispatcher1 = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
private val dispatcher2 = Executors.newCachedThreadPool().asCoroutineDispatcher()
private val dispatcher3 = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
private val scope = CoroutineScope(dispatcher1)

fun main() {
    scope.launch {
        getFlow().onStart { println("onStart: ${getCurrentThread()}") }
            .onEach { println("onEach 1: ${getCurrentThread()}") }

            .flowOn(dispatcher1) // Выше будет созданный диспатчер

            .map {
                println("Map: ${getCurrentThread()}")
                it
            }

            .flowOn(Dispatchers.Default) // Выше будет диспачер дефолт

            .onEach { println("onEach 2: ${getCurrentThread()}") }
            .collect {
                println("Collected: $it on ${getCurrentThread()}")
            }
    }

}

fun getFlow() = flow {
    var seconds = 0
    while (true) {
        println("Emitted: $seconds in ${getCurrentThread()}")
        emit(seconds++)
        delay(1000)
    }
}

fun getCurrentThread(): String = Thread.currentThread().name // Возвращает имя потока