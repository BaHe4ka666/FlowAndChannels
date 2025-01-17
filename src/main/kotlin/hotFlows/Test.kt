package hotFlows

import hotFlows.Repository.timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
private val scope = CoroutineScope(dispatcher)


fun main() {
    val flow = RepositoryColdFlows.timer
    scope.launch {
        delay(2000)
        flow.collect {
            println("Coroutine 1: $it")
        }
    }
    scope.launch {
        delay(5000)
        flow.collect {
            println("Coroutine 2: $it")
        }
        println("Finished")
    }
}

/*
(Конспекты стр.123)
Особенности горячих потоков:

1. Flow осуществляет emit данных независимо от наличия подписчиков
2. Все подписчики получают одни и те же элементы
3. Если подписчикам не нужны данные, то flow не завершает свою работу
4. Не завершается никогда
*/

