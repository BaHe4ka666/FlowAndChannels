package exceptions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

private val dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()
private val scope = CoroutineScope(dispatcher)

fun main() {
    val flow = getFlow()
    scope.launch {
        flow.retry(5) { true }
            .catch { println("Exception caught") }
            .collect {
                println(it)
            }
    }
}
    
private fun getFlow(): Flow<Int> {
    return flow {
        repeat(5) {
            if (it == 3) {
                error("Exception in flow")
            }
            emit(it)
        }
    }
}