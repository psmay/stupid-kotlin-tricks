package com.psmay.stupid.options

// List iterator for an immutable, single-item list.
internal class SingleListIterator<T>(private val element: T, private var isAtStart: Boolean) : ListIterator<T> {
    override fun hasNext() = isAtStart

    override fun hasPrevious() = !isAtStart

    override fun next(): T {
        if (isAtStart) {
            isAtStart = false
            return element
        } else {
            throw NoSuchElementException()
        }
    }

    override fun nextIndex() = if (isAtStart) 0 else 1

    override fun previous(): T {
        if (isAtStart) {
            throw NoSuchElementException()
        } else {
            isAtStart = true
            return element
        }
    }

    override fun previousIndex() = if (isAtStart) -1 else 0
}
