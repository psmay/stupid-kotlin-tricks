package com.psmay.stupid.options

/**
 * Retrieves, as an option, the element at the specified [index], if present.
 *
 * @param index The requested index of an element to retrieve.
 * @return An option containing the element of this iterable [index] elements from the start, or an empty option if
 * [index] is negative or after the end of this iterable.
 */
inline fun <T> Iterable<T>.elementAtOpt(index: Int): Opt<T> {
    return if (this is List) {
        this.getOpt(index)
    } else if (index < 0) {
        emptyOpt()
    } else {
        iterator().skipThenNextOpt(index)
    }
}

/**
 * Retrieves, as an option, the first element, if available.
 *
 * @return An option containing the first element of this iterable, or an empty option if this iterable is empty.
 */
inline fun <T> Iterable<T>.firstOpt(): Opt<T> = when (this) {
    is List -> if (isEmpty()) emptyOpt() else optOf(this[0])
    else -> iterator().nextOpt()
}

/**
 * Retrieves, as an option, the first element that satisfies a predicate, if any.
 *
 * @return An option containing the first element of this iterable that satisfies a predicate, or an empty option if
 * this iterable contains no matching elements.
 */
inline fun <T> Iterable<T>.firstOpt(predicate: (T) -> Boolean): Opt<T> = iterator().nextOpt(predicate)

/** Retrieves, as an option, the element at the specified [index], if present. */
inline fun <T> List<T>.getOpt(index: Int): Opt<T> =
    @Suppress("ConvertTwoComparisonsToRangeCheck")
    if (index >= 0 && index <= lastIndex) optOf(get(index)) else emptyOpt()

/**
 * Returns an option containing the value for the given key, or an empty option if there was no entry for the given key.
 */
inline fun <K, reified V> Map<K, V>.getOpt(key: K): Opt<V> {
    val valueOrNull = get(key)
    return if (valueOrNull != null) {
        optOf<V>(valueOrNull)
    } else if (null is V && containsKey(key)) {
        optOf(null as V)
    } else {
        emptyOpt()
    }
}

/**
 * Retrieves, as an option, the last element, if available.
 *
 * @return An option containing the last element of this iterable, or an empty option if this iterable is empty.
 */
inline fun <T> Iterable<T>.lastOpt(): Opt<T> = when (this) {
    is List -> if (isEmpty()) emptyOpt() else optOf(this[size - 1])
    else -> iterator().lastOpt()
}

/**
 * Gets the last element contained in an option, if present; otherwise, gets an empty option.
 */
inline fun <T> Iterator<T>.lastOpt(): Opt<T> {
    return if (hasNext()) {
        var last = next()
        while (hasNext()) {
            last = next()
        }
        optOf(last)
    } else {
        emptyOpt()
    }
}

/**
 * Retrieves, as an option, the last element that satisfies a predicate, if any.
 *
 * @return An option containing the last element of this iterable that satisfies a predicate, or an empty option if
 * this iterable contains no matching elements.
 */
inline fun <T> Iterable<T>.lastOpt(predicate: (T) -> Boolean): Opt<T> = iterator().lastOpt(predicate)

/**
 * Gets the last element that satisfies a predicate in an option, if present.
 */
inline fun <T> Iterator<T>.lastOpt(predicate: (T) -> Boolean): Opt<T> {
    var valueOpt: Opt<T> = emptyOpt()

    for (element in this) {
        if (predicate(element)) {
            valueOpt = optOf(element)
        }
    }

    return valueOpt
}

/**
 * Gets the next element contained in an option, if present; otherwise, gets an empty option.
 */
inline fun <T> Iterator<T>.nextOpt(): Opt<T> = if (hasNext()) optOf(next()) else emptyOpt()

/**
 * Gets the next element that satisfies a predicate in an option, if present.
 */
inline fun <T> Iterator<T>.nextOpt(predicate: (T) -> Boolean): Opt<T> {
    for (element in this) {
        if (predicate(element)) return optOf(element)
    }
    return emptyOpt()
}

/**
 * Removes and returns, as an option, the first element of this list, or an empty option if this list is empty.
 */
inline fun <T> MutableList<T>.removeFirstOpt(): Opt<T> =
    if (isEmpty()) emptyOpt() else optOf(removeAt(0))

/**
 * Removes and returns, as an option, the last element of this list, or an empty option if this list is empty.
 */
inline fun <T> MutableList<T>.removeLastOpt(): Opt<T> =
    if (isEmpty()) emptyOpt() else optOf(removeAt(lastIndex))

inline fun <T> Iterable<T>.singleOpt(): Opt<T> {
    return when (this) {
        is List ->
            if (size == 1) optOf(this[0]) else emptyOpt()
        else -> {
            val iterator = iterator()
            val nextOpt = iterator.nextOpt()
            if (nextOpt.hasValue && iterator.hasNext()) emptyOpt() else nextOpt
        }
    }
}

inline fun <T> Iterable<T>.singleOpt(predicate: (T) -> Boolean): Opt<T> {
    var matchOpt = emptyOpt<T>()
    for (element in this) {
        if (predicate(element)) {
            if (matchOpt.hasValue) {
                return emptyOpt()
            }
            matchOpt = optOf(element)
        }
    }
    return matchOpt
}

inline fun <T> Iterable<T>.singleOptOrNull(): Opt<T>? {
    return when (this) {
        is List ->
            when (size) {
                0 -> emptyOpt()
                1 -> optOf(this[0])
                else -> null
            }
        else -> {
            val iterator = iterator()
            val nextOpt = iterator.nextOpt()
            if (nextOpt.hasValue && iterator.hasNext()) null else nextOpt
        }
    }
}

inline fun <T> Iterable<T>.singleOptOrNull(predicate: (T) -> Boolean): Opt<T>? {
    var matchOpt = emptyOpt<T>()
    for (element in this) {
        if (predicate(element)) {
            if (matchOpt.hasValue) {
                return null
            }
            matchOpt = optOf(element)
        }
    }
    return matchOpt
}

/**
 * Skips elements from this iterator, then gets the next element contained in an option, if present.
 *
 * May skip fewer elements if the requested number of elements is not available or if [count] is less than 0.
 *
 * @param count The requested number of elements to skip.
 * @return An option containing the first element after the skip, or an empty option if the iterator is exhausted
 * before skipping [count] elements, or an empty option if [count] is less than 0.
 */
fun <T> Iterator<T>.skipThenNextOpt(count: Int): Opt<T> = if (count == skip(count)) nextOpt() else emptyOpt()

/**
 * Takes and discards the next [count] items from this iterator.
 *
 * May skip fewer elements if the requested number of elements is not available or if [count] is less than 0.
 *
 * @param count The requested number of elements to skip.
 * @return The number of elements actually skipped.
 */
internal inline fun <T> Iterator<T>.skip(count: Int): Int {
    var n = 0
    for (i in 0 until count) {
        if (hasNext()) {
            next()
            n++
        } else {
            break
        }
    }
    return n
}
