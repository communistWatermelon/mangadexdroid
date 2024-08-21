package com.melonhead.lib_networking.ratelimit.impl

internal class LongContinuousBuffer(private val capacity: Int) {
    private val backend = LongArray(capacity)
    private var head = 0
    var size = 0
        private set

    fun isEmpty() = size == 0

    fun add(element: Long) {
        if (size == capacity) {
            shiftHead()
            backend[getInternalIndex(size - 1)] = element
        } else {
            backend[getInternalIndex(size)] = element
            size += 1
        }
    }

    fun get(index: Int): Long {
        return backend[getInternalIndex(index)]
    }

    private fun shiftHead() {
        if (head == size - 1) {
            head = 0
        } else {
            head += 1
        }
    }

    private fun getInternalIndex(index: Int): Int {
        require(index < capacity)

        var internal = head + index
        if (internal >= capacity) {
            internal -= capacity
        }

        return internal
    }
}

internal fun LongContinuousBuffer.oldest(): Long {
    if (isEmpty())
        throw NoSuchElementException("LongContinuousBuffer is empty.")

    return get(0)
}

internal fun LongContinuousBuffer.newest(): Long {
    if (isEmpty())
        throw NoSuchElementException("LongContinuousBuffer is empty.")

    return get(size - 1)
}
