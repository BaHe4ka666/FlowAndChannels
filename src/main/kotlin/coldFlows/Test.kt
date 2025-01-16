package coldFlows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
private val scope = CoroutineScope(dispatcher)


fun main() {

    val flow = Repository.timer
    scope.launch {
        flow.collect {
            println(it)
        }
    }
    scope.launch {
        delay(5000)
        flow.collect {
            println(it)
        }
    }
}

/*
(конспекты стр. 123)
Особенности холодного потока данных:

1. Flow осуществляют emit данных только при наличии подписчиков
2. Холодные flow завершают работу, если подписчику данные не нунжы
3. Flow завершат работу, если все данные прошли заэмичены
4. При каждоый подписке создается новый поток данных

 */
