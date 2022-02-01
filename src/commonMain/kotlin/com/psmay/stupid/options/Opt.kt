package com.psmay.stupid.options

import kotlin.jvm.JvmInline

/**
 * Type to unambiguously represent either
 *
 * - the value of an actual result, or
 * - the absence of an actual result.
 *
 * In ordinary applications, this distinction is implemented using `null`; this type works in situations where this
 * is not possible; in particular, it addresses the possibility that `null` is a valid value for a given purpose and
 * thus cannot represent the absence of a value.
 *
 * An [Opt] can be created using methods on the companion object, particularly [Opt.valueOf()]. More importantly,
 * methods are available for fetching single items (`firstOpt`, `lastOpt`, `elementAtOpt`, `singleOpt`) from
 * iterables and * sequences. Virtually anywhere a `*OrNull` method exists, there should be a `*Opt` method to report
 * the same result as an [Opt].
 */
@JvmInline
value class Opt<out T> private constructor(private val valueOrMarker: Any?) {
    private object EmptyValueMarker

    /**
     * Returns true if this option contains a value.
     */
    val hasValue get() = valueOrMarker !== EmptyValueMarker

    @Suppress("UNCHECKED_CAST")
    private inline val rawValue
        get() = valueOrMarker as T

    /**
     * Returns the value contained in this option, if any.
     *
     * @throws NoSuchElementException If this option is empty.
     */
    val value: T get() = if (valueOrMarker === EmptyValueMarker) throw NoSuchElementException() else rawValue

    /**
     * Transforms the contents of this option, creating a new option with transformed contents.
     */
    fun <R> map(transform: (T) -> R): Opt<R> = if (hasValue) Opt(transform(rawValue)) else emptyOpt()

    /**
     * Filters the option so that only contents that satisfy this predicate remain.
     */
    fun filter(predicate: (T) -> Boolean): Opt<T> = if (hasValue && !predicate(rawValue)) emptyOpt() else this

    /**
     * Calls an action on each contained element.
     */
    fun forEach(action: (T) -> Unit) {
        if (hasValue)
            action(rawValue)
    }

    override fun toString() = if (hasValue) "optOf($rawValue)" else "emptyOpt()"

    /**
     * A view of [Opt] that implements the [List] interface.
     */
    @JvmInline
    value class ListView<out T> internal constructor(private val opt: Opt<T>) : List<T> {
        /**
         * Returns an iterable view of the option that generated this list view (which is this list view itself).
         */
        fun asIterable() = this

        /**
         * Returns the [Opt] corresponding to this list view.
         */
        fun asOpt() = opt

        /**
         * Returns a sequence view of the option that generated this list view.
         */
        fun asSequence() = opt.asSequence()

        override fun contains(element: @UnsafeVariance T) = opt.contains(element)

        override fun containsAll(elements: Collection<@UnsafeVariance T>) = opt.containsAll(elements)

        override fun get(index: Int): T = opt[index]

        override fun indexOf(element: @UnsafeVariance T) = opt.indexOf(element)

        override fun isEmpty() = opt.isEmpty()

        override fun iterator() = opt.iterator()

        override fun lastIndexOf(element: @UnsafeVariance T) = opt.lastIndexOf(element)

        override fun listIterator() = opt.listIterator()

        override fun listIterator(index: Int) = opt.listIterator(index)

        override val size get() = opt.size

        override fun subList(fromIndex: Int, toIndex: Int): ListView<T> {
            return if (fromIndex == 0 && toIndex == size) {
                this
            } else if (fromIndex == toIndex) {
                emptyOpt<T>().asList()
            } else {
                throw IndexOutOfBoundsException()
            }
        }
    }

    @JvmInline
    value class SequenceView<out T> internal constructor(private val opt: Opt<T>) : Sequence<T> {
        /**
         * Returns an iterable view of the option that generated this list view (which is the same as [asList]).
         */
        fun asIterable() = asList()

        /**
         * Returns the [Opt] corresponding to this sequence view.
         */
        fun asOpt() = opt

        /**
         * Returns a list view of the option that generated this sequence view.
         */
        fun asList() = opt.asList()

        override fun iterator() = opt.iterator()
    }

    companion object {
        internal fun contentEquals(a: Opt<*>, b: Opt<*>): Boolean = a.valueOrMarker == b.valueOrMarker

        internal fun contentSame(a: Opt<*>, b: Opt<*>): Boolean = a.valueOrMarker === b.valueOrMarker

        internal fun <T> emptyOpt(): Opt<T> = Opt(EmptyValueMarker)

        internal inline fun <T> flatten(opt: Opt<Opt<T>>) = if (opt.hasValue) opt.rawValue else emptyOpt()

        internal inline fun <T> getOrElse(opt: Opt<T>, defaultValue: () -> T): T =
            if (opt.hasValue) opt.rawValue else defaultValue()

        internal inline fun <T, R> join(opt: Opt<T>, ifFull: (T) -> R, ifEmpty: () -> R): R =
            if (opt.hasValue) ifFull(opt.rawValue) else ifEmpty()

        internal fun <T> listViewOf(opt: Opt<T>) = ListView(opt)

        internal fun <T> optOf(value: T): Opt<T> = Opt(value)

        internal fun <T> sequenceViewOf(opt: Opt<T>) = SequenceView(opt)
    }
}

/**
 * Returns whether this option contains only values that satisfy a predicate.
 *
 * For an empty option, this always returns `true`.
 */
inline fun <T> Opt<T>.all(noinline predicate: (T) -> Boolean) = filter(predicate).hasValue == hasValue

/**
 * Returns whether this option contains a value.
 */
inline fun <T> Opt<T>.any() = hasValue

/**
 * Returns whether this option contains a value that satisfies a predicate.
 *
 * For an empty option, this always returns `false`.
 */
inline fun <T> Opt<T>.any(noinline predicate: (T) -> Boolean) = filter(predicate).hasValue

/**
 * Returns an iterable view of this option (which is the same as a list view).
 */
inline fun <T> Opt<T>.asIterable() = asList()

/**
 * Returns a list view of this option.
 */
fun <T> Opt<T>.asList(): Opt.ListView<T> = Opt.listViewOf(this)

/**
 * Returns a sequence view of this option.
 */
fun <T> Opt<T>.asSequence(): Opt.SequenceView<T> = Opt.sequenceViewOf(this)

/**
 * Returns the value contained in this option, if any.
 *
 * @throws NoSuchElementException If this option is empty.
 */
inline operator fun <T> Opt<T>.component1(): T = value

/**
 * Returns whether this option contains the specified element.
 */
inline fun <T> Opt<T>.contains(element: T) = any { it == element }

/**
 * Returns whether this option contains all specified elements.
 *
 * If this option is empty, this returns `true` if [elements] is also empty.
 *
 * If this option is full, this returns `true` if the contained value is equal to all values in [elements].
 */
inline fun <T> Opt<T>.containsAll(elements: Iterable<T>): Boolean {
    return map { value -> elements.all { value == it } }.getOrElse { elements.none() }
}

/**
 * Returns whether this option contains any specified elements.
 *
 * If this option is empty, this returns `false`.
 *
 * If this option is full, this returns `true` if the contained value is equal to any value in [elements].
 */
inline fun <T> Opt<T>.containsAny(elements: Iterable<T>): Boolean {
    return any { value -> elements.any { value == it } }
}

/**
 * Returns whether another option has equal contents to this option.
 *
 * This option `contentEquals` [other] only if:
 *
 * - This and [other] both contain values and the contained value of this is structurally equal (`==`) to the
 * contained value of [other], or
 * - this and [other] are both empty.
 */
infix fun Opt<*>.contentEquals(other: Opt<*>) = Opt.contentEquals(this, other)

/**
 * Returns whether another option has equal contents to this option.
 *
 * This option `contentSameAs` [other] only if:
 *
 * - This and [other] both contain values and the contained value of this is referentially equal (`===`) to the
 * contained value of [other], or
 * - this and [other] are both empty.
 */
infix fun Opt<*>.contentSameAs(other: Opt<*>) = Opt.contentSame(this, other)

/**
 * Returns the number of values contained by this option.
 */
inline fun <T> Opt<T>.count() = size

/**
 * Returns the number of values contained by this option that satisfy a predicate.
 */
inline fun <T> Opt<T>.count(noinline predicate: (T) -> Boolean) = if (any(predicate)) 1 else 0

/**
 * Produces an empty option.
 */
fun <T> emptyOpt() = Opt.emptyOpt<T>()

/**
 * Returns an option containing only values from this option that are instances of a specified type.
 */
inline fun <reified R> Opt<*>.filterIsInstance(): Opt<R> =
    flatMap { if (it is R) optOf(it) else emptyOpt() }

/**
 * Returns an option containing only values from this option that do not satisfy a predicate.
 */
inline fun <T> Opt<T>.filterNot(noinline predicate: (T) -> Boolean) = if (any(predicate)) emptyOpt() else this

/**
 * Returns an option containing only values from this option that are not `null`.
 */
inline fun <T> Opt<T?>.filterNotNull(): Opt<T> =
    flatMap { if (it != null) optOf(it) else emptyOpt() }

/**
 * Flattens the result of mapping the contents of this option to an option.
 *
 * If this option is empty, an empty option is returned. Otherwise, [transform] is called on the contents of this
 * option and the resulting option is returned.
 */
inline fun <T, R> Opt<T>.flatMap(noinline transform: (T) -> Opt<R>): Opt<R> = map(transform).flatten()

/**
 * Unwraps, by one layer, an option contained in another option.
 */
inline fun <T> Opt<Opt<T>>.flatten1(): Opt<T> = flatten()

/**
 * Unwraps, by 2 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<T>>>.flatten2(): Opt<T> = flatten1().flatten()

/**
 * Unwraps, by 3 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<Opt<T>>>>.flatten3(): Opt<T> = flatten2().flatten()

/**
 * Unwraps, by 4 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<Opt<Opt<T>>>>>.flatten4(): Opt<T> = flatten3().flatten()

/**
 * Unwraps, by 5 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<Opt<Opt<Opt<T>>>>>>.flatten5(): Opt<T> = flatten4().flatten()

/**
 * Unwraps, by 6 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<Opt<Opt<Opt<Opt<T>>>>>>>.flatten6(): Opt<T> = flatten5().flatten()

/**
 * Unwraps, by 7 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<Opt<Opt<Opt<Opt<Opt<T>>>>>>>>.flatten7(): Opt<T> = flatten6().flatten()

/**
 * Unwraps, by 8 layers, an option contained in other options.
 */
inline fun <T> Opt<Opt<Opt<Opt<Opt<Opt<Opt<Opt<Opt<T>>>>>>>>>.flatten8(): Opt<T> = flatten7().flatten()

/**
 * Returns an iterator whose elements are the contents of an iterator of options.
 */
fun <T> Iterator<Opt<T>>.flatten(): Iterator<T> {
    return object : Iterator<T> {
        val source = this@flatten
        var nextOpt: Opt<T> = emptyOpt()

        private fun advance(): Opt<T> {
            while (source.hasNext()) {
                val foundOpt = source.next()
                if (foundOpt.hasValue) {
                    return foundOpt
                }
            }
            return emptyOpt()
        }

        override fun hasNext(): Boolean {
            if (!nextOpt.hasValue) {
                nextOpt = advance()
            }
            return nextOpt.hasValue
        }

        override fun next(): T {
            if (!hasNext()) {
                throw NoSuchElementException()
            }

            val opt = nextOpt
            nextOpt = emptyOpt()
            return opt.value
        }
    }
}

/**
 * Returns a list whose elements are the contents of an iterable of options.
 */
inline fun <T> Iterable<Opt<T>>.flatten(): List<T> {
    val iterable = object : Iterable<T> {
        override fun iterator(): Iterator<T> = this@flatten.iterator().flatten()
    }
    return iterable.toList()
}

/**
 * Unwraps, by one layer, an option contained in another option.
 */
fun <T> Opt<Opt<T>>.flatten(): Opt<T> = Opt.flatten(this)

/**
 * Returns a sequence whose elements are the contents of a sequence of options.
 */
inline fun <T> Sequence<Opt<T>>.flatten(): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> = this@flatten.iterator().flatten()
    }
}

/**
 * Returns the result of one operation for a full option or a different operation for an empty option.
 */
fun <T, R> Opt<T>.fold(ifFull: (T) -> R, ifEmpty: () -> R) = Opt.join(this, ifFull, ifEmpty)

/**
 * Returns the value contained in this option, if any.
 *
 * @throws NoSuchElementException If this option is empty.
 */
inline fun <T> Opt<T>.get() = value

/**
 * Returns the contained value, if it is present and the specified index is 0.
 *
 * @throws IndexOutOfBoundsException If [index] is out of bounds.
 */
inline operator fun <T> Opt<T>.get(index: Int): T =
    if (index == 0 && hasValue) value else throw IndexOutOfBoundsException()

/**
 * Returns the contained value, if it is present, or a default value if this option is empty.
 */
fun <R, T : R> Opt<T>.getOrDefault(defaultValue: R): R = if (hasValue) value else defaultValue

/**
 * Returns the contained value, if it is present, or a default value if this option is empty.
 */
fun <T> Opt<T>.getOrElse(defaultValue: () -> T): T = Opt.getOrElse(this, defaultValue)

/**
 * Returns the value contained in this option, or `null` if this option is empty.
 *
 * Note that if [T] is itself a nullable type, `null` may be returned as an actual value even if this option is not
 * empty.
 */
inline fun <T> Opt<T>.getOrNull() = if (hasValue) value else null

/**
 * Returns this option, or an option containing the specified element if this option is empty.
 */
inline fun <T> Opt<T>.ifEmpty(element: T): Opt<T> = if (this.hasValue) this else optOf(element)

/**
 * Returns this option, or an option containing the generated element if this option is empty.
 */
inline fun <T> Opt<T>.ifEmpty(defaultValue: () -> T): Opt<T> = if (this.hasValue) this else optOf(defaultValue())

/**
 * Returns the index of the specified value in this option, or -1 if there is no matching value.
 */
inline fun <T> Opt<T>.indexOf(element: T) = if (contains(element)) 0 else -1

/**
 * Returns whether this option is empty.
 */
inline fun <T> Opt<T>.isEmpty() = !hasValue

/**
 * Returns an iterator over the contents of this option.
 */
inline fun <T> Opt<T>.iterator(): Iterator<T> = listIterator()

/**
 * Returns the index of the specified value in this option, or -1 if there is no matching value.
 */
inline fun <T> Opt<T>.lastIndexOf(element: T) = if (contains(element)) 0 else -1

/**
 * Returns a list iterator over the contents of this option.
 */
fun <T> Opt<T>.listIterator(): ListIterator<T> =
    if (hasValue) SingleListIterator(value, true) else EmptyListIterator

/**
 * Returns a list iterator over the contents of this option, positioned at the specified index.
 *
 * @throws IndexOutOfBoundsException If [index] is out of bounds.
 */
fun <T> Opt<T>.listIterator(index: Int): ListIterator<T> {
    val size = this.size

    return if (index < 0 || index > size) {
        throw IndexOutOfBoundsException()
    } else if (size == 1) {
        SingleListIterator(value, index == 0)
    } else {
        EmptyListIterator
    }
}

/**
 * Maps the contained values from this option, discarding the results that are `null`.
 */
inline fun <T, R : Any> Opt<T>.mapNotNull(noinline transform: (T) -> R?): Opt<R> =
    flatMap {
        val result = transform(it)
        if (result == null) emptyOpt() else optOf(result)
    }

/**
 * Returns an option that contains the same values as this option, except for any that are equal to the specified
 * element.
 */
inline operator fun <T> Opt<T>.minus(element: T) = filterNot { it == element }

/**
 * Returns an option that contains the same values as this option, except for any that are equal to the contained
 * value of the specified element.
 */
inline operator fun <T> Opt<T>.minus(other: Opt<T>) = filterNot { other.contains(it) }

/**
 * Returns an option that contains the same values as this option, except for any that are equal to the specified
 * elements.
 */
inline operator fun <T> Opt<T>.minus(elements: Iterable<T>) = if (this.containsAny(elements)) emptyOpt() else this

/**
 * Returns an option that contains the same values as this option, except for any that are equal to the specified
 * elements.
 */
inline operator fun <T> Opt<T>.minus(elements: Sequence<T>) = minus(elements.asIterable())

/**
 * Returns whether this option contains no values. (This is the opposite of [any].)
 */
inline fun <T> Opt<T>.none() = !any()

/**
 * Returns whether this option contains no values that satisfy a predicate. (This is the opposite of [any].)
 */
inline fun <T> Opt<T>.none(noinline predicate: (T) -> Boolean) = !any(predicate)

/**
 * Produces an option containing the specified value.
 */
fun <T> optOf(value: T) = Opt.optOf(value)

/**
 * Calls an action for each value contained in this option, then returns this option.
 */
inline fun <T> Opt<T>.onEach(noinline action: (T) -> Unit): Opt<T> {
    forEach(action)
    return this
}

/**
 * Returns this option, or the specified option if this option is empty.
 */
inline fun <T> Opt<T>.replaceIfEmpty(other: Opt<T>) = if (this.hasValue) this else other

/**
 * Returns this option, or the generated option if this option is empty.
 */
inline fun <T> Opt<T>.replaceIfEmpty(defaultValue: () -> Opt<T>): Opt<T> = if (this.hasValue) this else defaultValue()

/**
 * Returns the number of values contained in this option.
 */
inline val <T> Opt<T>.size get() = if (hasValue) 1 else 0

/**
 * Merges the contained values of two full options into a new full option containing a pair of values, or returns an
 * empty option if either source option is empty.
 */
inline infix fun <T, R> Opt<T>.zip(other: Opt<R>): Opt<Pair<T, R>> {
    return flatMap { a -> other.map { b -> Pair(a, b) } }
}

/**
 * Merges the contained values of two full options into a new full option containing the merge result, or returns an
 * empty option if either source option is empty.
 */
inline fun <T, R, V> Opt<T>.zip(other: Opt<R>, noinline transform: (a: T, b: R) -> V): Opt<V> {
    return flatMap { a -> other.map { b -> transform(a, b) } }
}