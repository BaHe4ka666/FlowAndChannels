package lesson1

import kotlin.random.Random

fun main() {
    var filterCount = 0
    var mapCount = 0
    val numbers = mutableListOf<Int>().apply {
        repeat(1000) {
            add(Random.nextInt(1000))
        }
    }.asSequence()

    numbers.filter {
        println("Filter")
        filterCount++
        it % 2 == 0
    }.map {
        println("Map")
        mapCount++
        it * 10
    }.take(10)
        .forEach(::println)

    println("Filtered: $filterCount")
    println("Mapped: $mapCount")
}