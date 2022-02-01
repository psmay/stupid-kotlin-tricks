package com.psmay.stupid.options

// List iterator for an immutably empty list.
internal object EmptyListIterator : ListIterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previous(): Nothing = throw NoSuchElementException()
    override fun previousIndex(): Int = -1
}