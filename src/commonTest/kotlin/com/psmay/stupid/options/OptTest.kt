package com.psmay.stupid.options

import kotlin.test.*

// This is our own way to copy an opt to its equivalent list, to avoid code being subject to testing by itself.
internal fun <T> Opt<T>.testCopyToList() = if (hasValue) listOf(value) else emptyList()

// This is our own way to copy a zero- or single-element list to its equivalent opt.
internal fun <T> List<T>.testCopyToOpt(): Opt<T> {
    return when (this.size) {
        0 -> emptyOpt()
        1 -> optOf(this[0])
        else -> throw Exception()
    }
}

internal fun <T> assertOptEquals(expected: Opt<T>, actual: Opt<T>?) {
    if (expected.hasValue) {
        assertNotNull(actual, "Expected full option; actual is null")
        if (actual.hasValue) {
            assertEquals(expected.value, actual.value, "Expected full option contents to be equal")
        } else {
            fail("Expected option containing <${expected.value}>, got empty option")
        }
    } else {
        assertNotNull(actual, "Expected empty option; actual is null")
        if (actual.hasValue) {
            fail("Expected empty option, got option containing <${actual.value}>")
        }
    }
}

internal fun <T> assertOptEmpty(actual: Opt<T>?) {
    assertNotNull(actual, "Expected empty option; actual is null")
    assertFalse(actual.hasValue, "Expected empty option")
}

@Suppress("unused")
internal fun <T> assertOptFull(actual: Opt<T>?) {
    assertNotNull(actual, "Expected full option; actual is null")
    assertTrue(actual.hasValue, "Expected full option")
}

@Suppress("unused")
internal fun <T> assertOptSame(expected: Opt<T>, actual: Opt<T>) {
    if (expected.hasValue) {
        if (actual.hasValue) {
            assertSame(expected.value, actual.value, "Expected full option contents to be the same")
        } else {
            fail("Expected option containing same <${expected.value}>, got empty option")
        }
    } else {
        if (actual.hasValue) {
            fail("Expected empty option, got option containing <${actual.value}>")
        }
    }
}

// This is an implementation of the Maybe/Option concept for testing purposes. It is intended to be obvious in
// function and thus doesn't use any of the tricks employed by Opt to reduce overhead.
internal sealed class TestMaybe<T> {
    abstract val hasValue: Boolean
    abstract fun get(): T

    data class Some<T>(val value: T) : TestMaybe<T>() {
        override val hasValue get() = true
        override fun get() = value
    }

    object None : TestMaybe<Nothing>() {
        override val hasValue get() = false
        override fun get(): Nothing {
            throw NoSuchElementException()
        }
    }
}

internal class OptTest {
    private data class ExampleData(val intValue: Int, val stringValue: String)

    private val optIntEmpty: Opt<Int> = emptyOpt()
    private val optStringEmpty: Opt<String> = emptyOpt()
    private val optNullableExampleDataEmpty: Opt<ExampleData?> = emptyOpt()

    private val optIntFull: Opt<Int> = optOf(10)
    private val optStringFull: Opt<String> = optOf("Hello")
    private val optNullableExampleDataFull: Opt<ExampleData?> = optOf(ExampleData(72, "Beep"))
    private val optNullableExampleDataNull: Opt<ExampleData?> = optOf(null)

    //region Members of the object itself

    @Test
    fun hasValueTests() {
        // Meaning: hasValue is true when there is a value and false otherwise.
        assertFalse(optIntEmpty.hasValue)
        assertFalse(optStringEmpty.hasValue)
        assertFalse(optNullableExampleDataEmpty.hasValue)

        assertTrue(optIntFull.hasValue)
        assertTrue(optStringFull.hasValue)
        assertTrue(optNullableExampleDataFull.hasValue)
        assertTrue(optNullableExampleDataNull.hasValue) // Full of null is still full.
    }

    @Test
    fun valueTests() {
        // Meaning: value produces a value for full but throws for empty.
        assertFailsWith<NoSuchElementException> { optIntEmpty.value }
        assertFailsWith<NoSuchElementException> { optStringEmpty.value }
        assertFailsWith<NoSuchElementException> { optNullableExampleDataEmpty.value }

        assertEquals(10, optIntFull.value)
        assertEquals("Hello", optStringFull.value)
        assertEquals(ExampleData(72, "Beep"), optNullableExampleDataFull.value)
        assertNull(optNullableExampleDataNull.value) // Full, but the value itself is null.
    }

    @Test
    fun mapTests() {
        fun testInt(value: Int) = value.toLong() * 2
        fun testString(value: String) = "$value$value"
        fun testData(value: ExampleData?): Pair<Int, String> {
            return if (value == null) {
                -1 to "nothing"
            } else {
                value.intValue to value.stringValue
            }
        }

        assertEquals(listOf(), optIntEmpty.map { testInt(it) }.testCopyToList())
        assertEquals(listOf(20L), optIntFull.map { testInt(it) }.testCopyToList())
        assertEquals(listOf(), optStringEmpty.map { testString(it) }.testCopyToList())
        assertEquals(listOf("HelloHello"), optStringFull.map { testString(it) }.testCopyToList())
        assertEquals(listOf(), optNullableExampleDataEmpty.map { testData(it) }.testCopyToList())
        assertEquals(listOf(72 to "Beep"), optNullableExampleDataFull.map { testData(it) }.testCopyToList())
        assertEquals(listOf(-1 to "nothing"), optNullableExampleDataNull.map { testData(it) }.testCopyToList())
    }

    @Test
    fun filterTests() {
        assertOptEmpty(optIntEmpty.filter { it > 5 })
        assertOptEquals(optIntFull, optIntFull.filter { it > 5 })
        assertOptEmpty(optIntEmpty.filter { it < 5 })
        assertOptEmpty(optIntFull.filter { it < 5 })

        assertOptEmpty(optStringEmpty.filter { it.length > 3 })
        assertOptEquals(optStringFull, optStringFull.filter { it.length > 3 })
        assertOptEmpty(optStringEmpty.filter { it.length < 3 })
        assertOptEmpty(optStringFull.filter { it.length < 3 })

        assertOptEmpty(optNullableExampleDataEmpty.filter { it != null })
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull.filter { it != null })
        assertOptEmpty(optNullableExampleDataNull.filter { it != null })
        assertOptEmpty(optNullableExampleDataEmpty.filter { it == null })
        assertOptEmpty(optNullableExampleDataFull.filter { it == null })
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull.filter { it == null })
    }

    @Test
    fun forEachTests() {
        fun <T> test(opt: Opt<T>) {
            val expectedList = opt.testCopyToList()
            val actualList = mutableListOf<T>()
            opt.forEach { actualList.add(it) }
            assertContentEquals(expectedList, actualList)
        }

        test(optIntEmpty)
        test(optIntFull)
        test(optStringEmpty)
        test(optStringFull)
        test(optNullableExampleDataEmpty)
        test(optNullableExampleDataFull)
        test(optNullableExampleDataNull)
    }

    //endregion

    //region Members of the companion

    // The internal members flatten, getOrElse, join, listViewOf (asList), sequenceViewOf (asSequence) are tested via
    // their public extension methods.

    //endregion

    //region Extension members

    // emptyOpt() and optOf() aren't tested specifically; rest assured that at least some tests that use the above
    // values opt*Empty, opt*Full, opt*Null will not work unless these methods behave as specified.

    @Test
    fun allTests() {
        // Test that Opt.all() behaves the same as List.all()

        fun <T> test(opt: Opt<T>, predicate: (T) -> Boolean) {
            val expected = opt.testCopyToList().all(predicate)
            val actual = opt.all(predicate)
            assertEquals(expected, actual, "Expected all() evaluations to match")
        }

        test(optIntEmpty) { it > 0 }
        test(optIntEmpty) { it > 50 }
        test(optStringEmpty) { it.length > 3 }
        test(optStringEmpty) { it.length > 13 }
        test(optNullableExampleDataEmpty) { (it != null) && it.intValue > 50 }
        test(optNullableExampleDataEmpty) { (it != null) && it.intValue > 150 }
        test(optIntFull) { it > 0 }
        test(optIntFull) { it > 50 }
        test(optStringFull) { it.length > 3 }
        test(optStringFull) { it.length > 13 }
        test(optNullableExampleDataFull) { (it != null) && it.intValue > 50 }
        test(optNullableExampleDataFull) { (it != null) && it.intValue > 150 }
        test(optNullableExampleDataNull) { (it != null) && it.intValue > 50 }
        test(optNullableExampleDataNull) { (it != null) && it.intValue > 150 }
    }

    @Test
    fun anyTests() {
        // Test that Opt.any() behaves the same as List.any()

        fun <T> test(opt: Opt<T>, predicate: (T) -> Boolean) {
            val expected = opt.testCopyToList().any(predicate)
            val actual = opt.any(predicate)
            assertEquals(expected, actual, "Expected all() evaluations to match")
        }

        test(optIntEmpty) { it > 0 }
        test(optIntEmpty) { it > 50 }
        test(optStringEmpty) { it.length > 3 }
        test(optStringEmpty) { it.length > 13 }
        test(optNullableExampleDataEmpty) { (it != null) && it.intValue > 50 }
        test(optNullableExampleDataEmpty) { (it != null) && it.intValue > 150 }
        test(optIntFull) { it > 0 }
        test(optIntFull) { it > 50 }
        test(optStringFull) { it.length > 3 }
        test(optStringFull) { it.length > 13 }
        test(optNullableExampleDataFull) { (it != null) && it.intValue > 50 }
        test(optNullableExampleDataFull) { (it != null) && it.intValue > 150 }
        test(optNullableExampleDataNull) { (it != null) && it.intValue > 50 }
        test(optNullableExampleDataNull) { (it != null) && it.intValue > 150 }
    }

    @Test
    fun componentDestructuringTests() { // component1
        assertFailsWith<NoSuchElementException> {
            val (x) = optIntEmpty
            println(x)
        }
        assertFailsWith<NoSuchElementException> {
            val (x) = optStringEmpty
            println(x)
        }
        assertFailsWith<NoSuchElementException> {
            val (x) = optNullableExampleDataEmpty
            println(x)
        }

        run {
            val (x) = optIntFull
            assertEquals(10, x)
        }
        run {
            val (x) = optStringFull
            assertEquals("Hello", x)
        }
        run {
            val (x) = optNullableExampleDataFull
            assertEquals(ExampleData(72, "Beep"), x)
        }
        run {
            val (x) = optNullableExampleDataNull
            assertNull(x) // Full, but the value itself is null.
        }
    }

    @Test
    fun containsTests() {
        assertFalse(optIntEmpty.contains(10))
        assertFalse(optStringEmpty.contains("Hello"))
        assertFalse(optNullableExampleDataEmpty.contains(ExampleData(72, "Beep")))

        assertTrue(optIntFull.contains(10))
        assertFalse(optIntFull.contains(20))

        assertTrue(optStringFull.contains("Hello"))
        assertFalse(optStringFull.contains("Not hello"))

        assertTrue(optNullableExampleDataFull.contains(ExampleData(72, "Beep")))
        assertFalse(optNullableExampleDataFull.contains(ExampleData(12, "Wrong")))
        assertFalse(optNullableExampleDataFull.contains(null))

        assertFalse(optNullableExampleDataNull.contains(ExampleData(72, "Beep")))
        assertFalse(optNullableExampleDataNull.contains(ExampleData(12, "Wrong")))
        assertTrue(optNullableExampleDataNull.contains(null))
    }

    @Test
    fun containsAllTests() {
        val exampleData = ExampleData(72, "Beep")
        val wrongExampleData = ExampleData(12, "Wrong")

        // containsAll always returns true when it doesn't ask for anything
        assertTrue(optIntEmpty.containsAll(emptyList()))
        assertTrue(optStringEmpty.containsAll(emptyList()))
        assertTrue(optNullableExampleDataEmpty.containsAll(emptyList()))
        assertTrue(optIntFull.containsAll(emptyList()))
        assertTrue(optStringFull.containsAll(emptyList()))
        assertTrue(optNullableExampleDataFull.containsAll(emptyList()))
        assertTrue(optNullableExampleDataNull.containsAll(emptyList()))

        // Where there is a single element, it behaves as contains
        assertFalse(optIntEmpty.containsAll(listOf(10)))
        assertFalse(optStringEmpty.containsAll(listOf("Hello")))

        assertFalse(optNullableExampleDataEmpty.containsAll(listOf(exampleData)))

        assertTrue(optIntFull.containsAll(listOf(10)))
        assertFalse(optIntFull.containsAll(listOf(20)))

        assertTrue(optStringFull.containsAll(listOf("Hello")))
        assertFalse(optStringFull.containsAll(listOf("Not hello")))

        assertTrue(optNullableExampleDataFull.containsAll(listOf(exampleData)))

        assertFalse(optNullableExampleDataFull.containsAll(listOf(wrongExampleData)))
        assertFalse(optNullableExampleDataFull.containsAll(listOf(null)))

        assertFalse(optNullableExampleDataNull.containsAll(listOf(exampleData)))
        assertFalse(optNullableExampleDataNull.containsAll(listOf(wrongExampleData)))
        assertTrue(optNullableExampleDataNull.containsAll(listOf(null)))

        // In fact, if there is only a single repeated element, it still behaves as contains
        assertFalse(optIntEmpty.containsAll(listOf(10, 10)))
        assertFalse(optStringEmpty.containsAll(listOf("Hello", "Hello")))
        assertFalse(optNullableExampleDataEmpty.containsAll(listOf(exampleData, exampleData)))

        assertTrue(optIntFull.containsAll(listOf(10, 10)))
        assertFalse(optIntFull.containsAll(listOf(20, 20)))

        assertTrue(optStringFull.containsAll(listOf("Hello", "Hello")))
        assertFalse(optStringFull.containsAll(listOf("Not hello", "Not hello")))

        assertTrue(optNullableExampleDataFull.containsAll(listOf(exampleData, exampleData)))
        assertFalse(optNullableExampleDataFull.containsAll(listOf(wrongExampleData, wrongExampleData)))
        assertFalse(optNullableExampleDataFull.containsAll(listOf(null, null)))

        assertFalse(optNullableExampleDataNull.containsAll(listOf(exampleData, exampleData)))
        assertFalse(optNullableExampleDataNull.containsAll(listOf(wrongExampleData, wrongExampleData)))
        assertTrue(optNullableExampleDataNull.containsAll(listOf(null, null)))

        // But if there are distinct elements, no more than one can appear so the result is false
        assertFalse(optIntEmpty.containsAll(listOf(10, 20)))
        assertFalse(optStringEmpty.containsAll(listOf("Hello", "Not hello")))
        assertFalse(optNullableExampleDataEmpty.containsAll(listOf(exampleData, wrongExampleData)))

        assertFalse(optIntFull.containsAll(listOf(10, 20)))
        assertFalse(optIntFull.containsAll(listOf(20, 30)))

        assertFalse(optStringFull.containsAll(listOf("Hello", "Not hello")))
        assertFalse(optStringFull.containsAll(listOf("Not hello", "More not hello")))

        assertFalse(optNullableExampleDataFull.containsAll(listOf(exampleData, wrongExampleData)))
        assertFalse(optNullableExampleDataFull.containsAll(listOf(null, exampleData)))

        assertFalse(optNullableExampleDataNull.containsAll(listOf(exampleData, wrongExampleData)))
        assertFalse(optNullableExampleDataNull.containsAll(listOf(null, exampleData)))
    }

    @Test
    fun containsAnyTests() {
        val exampleData = ExampleData(72, "Beep")
        val wrongExampleData = ExampleData(12, "Wrong")

        // containsAny always returns false if there is nothing to look for
        assertFalse(optIntEmpty.containsAny(emptyList()))
        assertFalse(optStringEmpty.containsAny(emptyList()))
        assertFalse(optNullableExampleDataEmpty.containsAny(emptyList()))
        assertFalse(optIntFull.containsAny(emptyList()))
        assertFalse(optStringFull.containsAny(emptyList()))
        assertFalse(optNullableExampleDataFull.containsAny(emptyList()))
        assertFalse(optNullableExampleDataNull.containsAny(emptyList()))

        // Where there is a single element, it behaves as contains
        assertFalse(optIntEmpty.containsAny(listOf(10)))
        assertFalse(optStringEmpty.containsAny(listOf("Hello")))

        assertFalse(optNullableExampleDataEmpty.containsAny(listOf(exampleData)))

        assertTrue(optIntFull.containsAny(listOf(10)))
        assertFalse(optIntFull.containsAny(listOf(20)))

        assertTrue(optStringFull.containsAny(listOf("Hello")))
        assertFalse(optStringFull.containsAny(listOf("Not hello")))

        assertTrue(optNullableExampleDataFull.containsAny(listOf(exampleData)))

        assertFalse(optNullableExampleDataFull.containsAny(listOf(wrongExampleData)))
        assertFalse(optNullableExampleDataFull.containsAny(listOf(null)))

        assertFalse(optNullableExampleDataNull.containsAny(listOf(exampleData)))
        assertFalse(optNullableExampleDataNull.containsAny(listOf(wrongExampleData)))
        assertTrue(optNullableExampleDataNull.containsAny(listOf(null)))

        // In fact, if there is only a single repeated element, it still behaves as contains
        assertFalse(optIntEmpty.containsAny(listOf(10, 10)))
        assertFalse(optStringEmpty.containsAny(listOf("Hello", "Hello")))
        assertFalse(optNullableExampleDataEmpty.containsAny(listOf(exampleData, exampleData)))

        assertTrue(optIntFull.containsAny(listOf(10, 10)))
        assertFalse(optIntFull.containsAny(listOf(20, 20)))

        assertTrue(optStringFull.containsAny(listOf("Hello", "Hello")))
        assertFalse(optStringFull.containsAny(listOf("Not hello", "Not hello")))

        assertTrue(optNullableExampleDataFull.containsAny(listOf(exampleData, exampleData)))
        assertFalse(optNullableExampleDataFull.containsAny(listOf(wrongExampleData, wrongExampleData)))
        assertFalse(optNullableExampleDataFull.containsAny(listOf(null, null)))

        assertFalse(optNullableExampleDataNull.containsAny(listOf(exampleData, exampleData)))
        assertFalse(optNullableExampleDataNull.containsAny(listOf(wrongExampleData, wrongExampleData)))
        assertTrue(optNullableExampleDataNull.containsAny(listOf(null, null)))

        // But if there are distinct elements, the result is true if any apply
        assertFalse(optIntEmpty.containsAny(listOf(10, 20)))
        assertFalse(optStringEmpty.containsAny(listOf("Hello", "Not hello")))
        assertFalse(optNullableExampleDataEmpty.containsAny(listOf(exampleData, wrongExampleData)))

        assertTrue(optIntFull.containsAny(listOf(10, 20)))
        assertFalse(optIntFull.containsAny(listOf(20, 30)))

        assertTrue(optStringFull.containsAny(listOf("Hello", "Not hello")))
        assertFalse(optStringFull.containsAny(listOf("Not hello", "More not hello")))

        assertTrue(optNullableExampleDataFull.containsAny(listOf(exampleData, wrongExampleData)))
        assertFalse(optNullableExampleDataFull.containsAny(listOf(null, wrongExampleData)))

        assertFalse(optNullableExampleDataNull.containsAny(listOf(exampleData, wrongExampleData)))
        assertTrue(optNullableExampleDataNull.containsAny(listOf(null, wrongExampleData)))
    }

    @Test
    fun contentEqualsTests() {
        assertTrue(optIntEmpty contentEquals emptyOpt<Int>())
        assertTrue(optStringEmpty contentEquals emptyOpt<String>())
        assertTrue(optNullableExampleDataEmpty contentEquals emptyOpt<String?>())

        assertTrue(optIntFull contentEquals optOf(10))
        assertTrue(optStringFull contentEquals optOf("Hello"))
        assertTrue(optNullableExampleDataFull contentEquals optOf(ExampleData(72, "Beep")))
        assertTrue(optNullableExampleDataNull contentEquals optOf<ExampleData?>(null))

        // Some examples of where there should be no match
        assertFalse(optIntEmpty contentEquals (optIntFull)) // One is empty, one not
        assertFalse(optStringFull contentEquals (optOf("Not hello"))) // Same type, not same contents

        // contentEquals basically ignores type whenever it can:

        // Empty means empty across types
        assertTrue(optIntEmpty contentEquals (optStringEmpty))
        assertTrue(optNullableExampleDataEmpty contentEquals (optStringEmpty))

        // Null means null across types
        assertTrue(optNullableExampleDataNull contentEquals optOf<String?>(null))

        // And if an object happens to belong to two not-always-compatible types, we're covered there, also
        assertTrue(optOf<CharSequence>("Magic") contentEquals optOf<Comparable<String>>("Magic"))
    }

    @Test
    fun contentReferentiallyEqualsTests() {
        assertTrue(optIntEmpty contentSameAs emptyOpt<Int>())
        assertTrue(optStringEmpty contentSameAs emptyOpt<String>())
        assertTrue(optNullableExampleDataEmpty contentSameAs emptyOpt<String?>())

        assertTrue(optIntFull contentSameAs optOf(10))
        assertTrue(optStringFull contentSameAs optOf("Hello"))
        assertTrue(optNullableExampleDataNull contentSameAs optOf<ExampleData?>(null))

        // Some examples of where there should be no match
        assertFalse(optIntEmpty contentSameAs (optIntFull)) // One is empty, one not
        assertFalse(optStringFull contentSameAs (optOf("Not hello"))) // Same type, not same contents

        // contentEquals basically ignores type whenever it can:

        // Empty means empty across types
        assertTrue(optIntEmpty contentSameAs (optStringEmpty))
        assertTrue(optNullableExampleDataEmpty contentSameAs (optStringEmpty))

        // Null means null across types
        assertTrue(optNullableExampleDataNull contentSameAs optOf<String?>(null))

        // And if an object happens to belong to two not-always-compatible types, we're covered there, also
        assertTrue(optOf<CharSequence>("Magic") contentSameAs optOf<Comparable<String>>("Magic"))

        // For reference values, things work a tiny bit differently than contentEquals.

        // This object has the same value, but isn't the same reference:
        assertFalse(optNullableExampleDataFull contentSameAs optOf(ExampleData(72, "Beep")))

        // But it does work if we use the original reference:
        val originalReference = optNullableExampleDataFull.value
        assertTrue(optNullableExampleDataFull contentSameAs optOf(originalReference))
    }

    @Suppress("RemoveExplicitTypeArguments")
    @Test
    fun filterIsInstanceTests() {
        assertOptEmpty(optIntEmpty.filterIsInstance<Int>())
        assertOptEmpty(optIntEmpty.filterIsInstance<String>())

        assertOptEmpty(optStringEmpty.filterIsInstance<String>())
        assertOptEmpty(optStringEmpty.filterIsInstance<Int>())

        assertOptEquals(optIntFull, optIntFull.filterIsInstance<Int>())
        assertOptEmpty(optIntFull.filterIsInstance<String>())

        assertOptEquals(optStringFull, optStringFull.filterIsInstance<String>())
        assertOptEmpty(optStringFull.filterIsInstance<Int>())

        val optIntAsAnyQ = optOf<Any?>(10)
        val optStringAsAnyQ = optOf<Any?>("Hello")
        val optDataAsAnyQ = optOf<Any?>(ExampleData(72, "Beep"))
        val optNullAsAnyQ = optOf<Any?>(null)

        assertOptEquals(optIntFull, optIntAsAnyQ.filterIsInstance<Int>())
        assertOptEmpty(optIntAsAnyQ.filterIsInstance<String>())
        assertOptEmpty(optIntAsAnyQ.filterIsInstance<String?>())
        assertOptEmpty(optIntAsAnyQ.filterIsInstance<ExampleData?>())

        assertOptEquals(optStringFull, optStringAsAnyQ.filterIsInstance<String>())
        assertOptEquals(optStringFull, optStringAsAnyQ.filterIsInstance<String?>())
        assertOptEmpty(optStringFull.filterIsInstance<Int>())
        assertOptEmpty(optStringFull.filterIsInstance<ExampleData?>())

        assertOptEquals(optNullableExampleDataFull, optDataAsAnyQ.filterIsInstance<ExampleData>())
        assertOptEquals(optNullableExampleDataFull, optDataAsAnyQ.filterIsInstance<ExampleData?>())
        assertOptEmpty(optDataAsAnyQ.filterIsInstance<Int>())
        assertOptEmpty(optDataAsAnyQ.filterIsInstance<String>())
        assertOptEmpty(optDataAsAnyQ.filterIsInstance<String?>())

        assertOptEmpty(optNullAsAnyQ.filterIsInstance<ExampleData>())
        assertOptEquals(optNullableExampleDataNull, optNullAsAnyQ.filterIsInstance<ExampleData?>())
        assertOptEmpty(optNullAsAnyQ.filterIsInstance<Int>())
        assertOptEmpty(optNullAsAnyQ.filterIsInstance<String>())
        val stringNull: String? = null
        assertOptEquals(optOf(stringNull), optNullAsAnyQ.filterIsInstance<String?>())
    }

    @Test
    fun filterNotTests() {
        assertOptEmpty(optIntEmpty.filterNot { it <= 5 })
        assertOptEquals(optIntFull, optIntFull.filterNot { it <= 5 })
        assertOptEmpty(optIntEmpty.filterNot { it >= 5 })
        assertOptEmpty(optIntFull.filterNot { it >= 5 })

        assertOptEmpty(optStringEmpty.filterNot { it.length <= 3 })
        assertOptEquals(optStringFull, optStringFull.filterNot { it.length <= 3 })
        assertOptEmpty(optStringEmpty.filterNot { it.length >= 3 })
        assertOptEmpty(optStringFull.filterNot { it.length >= 3 })

        assertOptEmpty(optNullableExampleDataEmpty.filterNot { it == null })
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull.filterNot { it == null })
        assertOptEmpty(optNullableExampleDataNull.filterNot { it == null })
        assertOptEmpty(optNullableExampleDataEmpty.filterNot { it != null })
        assertOptEmpty(optNullableExampleDataFull.filterNot { it != null })
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull.filterNot { it != null })
    }

    @Test
    fun filterNotNullTests() {
        assertOptEmpty(optIntEmpty.filterNotNull())
        assertOptEmpty(optStringEmpty.filterNotNull())
        assertOptEmpty(optNullableExampleDataEmpty.filterNotNull())

        assertEquals(optIntFull, optIntFull.filterNotNull())
        assertEquals(optStringFull, optStringFull.filterNotNull())
        assertEquals(optNullableExampleDataFull, optNullableExampleDataFull.filterNotNull())
        assertOptEmpty(optNullableExampleDataNull.filterNotNull())
    }

    @Test
    fun flatMapTests() {
        // On empty opt

        run {
            var ran = false
            assertOptEmpty(optIntEmpty.flatMap { ran = true; optOf(it * 2) })
            assertFalse(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optIntEmpty.flatMap { ran = true; emptyOpt<String>() }) // arbitrary type
            assertFalse(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optStringEmpty.flatMap { ran = true; optOf(it + it) })
            assertFalse(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optStringEmpty.flatMap { ran = true; emptyOpt<Int>() }) // arbitrary type
            assertFalse(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optNullableExampleDataEmpty.flatMap { ran = true; optOf(it?.intValue) })
            assertFalse(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optNullableExampleDataEmpty.flatMap { ran = true; emptyOpt<String>() }) // arbitrary type
            assertFalse(ran)
        }

        // On full opt

        run {
            var ran = false
            assertOptEquals(optOf(20), optIntFull.flatMap { ran = true; optOf(it * 2) })
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optIntFull.flatMap { ran = true; emptyOpt<String>() }) // arbitrary type
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEquals(optOf("HelloHello"), optStringFull.flatMap { ran = true; optOf(it + it) })
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optStringFull.flatMap { ran = true; emptyOpt<Int>() }) // arbitrary type
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEquals(optOf(72), optNullableExampleDataFull.flatMap { ran = true; optOf(it?.intValue) })
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optNullableExampleDataFull.flatMap { ran = true; emptyOpt<String>() }) // arbitrary type
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEquals(optOf(null), optNullableExampleDataNull.flatMap { ran = true; optOf(it?.intValue) })
            assertTrue(ran)
        }

        run {
            var ran = false
            assertOptEmpty(optNullableExampleDataNull.flatMap { ran = true; emptyOpt<String>() }) // arbitrary type
            assertTrue(ran)
        }
    }

    @Test
    fun flattenTests() {
        assertEquals(optIntEmpty, optOf(optIntEmpty).flatten())
        assertEquals(optStringEmpty, optOf(optStringEmpty).flatten())
        assertEquals(optNullableExampleDataEmpty, optOf(optNullableExampleDataEmpty).flatten())
        assertEquals(optIntFull, optOf(optIntFull).flatten())
        assertEquals(optStringFull, optOf(optStringFull).flatten())
        assertEquals(optNullableExampleDataFull, optOf(optNullableExampleDataFull).flatten())
        assertEquals(optNullableExampleDataNull, optOf(optNullableExampleDataNull).flatten())
    }

    @Test
    fun flattenIteratorBasedTests() {
        // Test for flatten() on iterator, iterable, sequence.
        val optList = listOf(
            optIntEmpty,
            optIntFull,
            optStringEmpty,
            optStringFull,
            optNullableExampleDataEmpty,
            optNullableExampleDataFull,
            optNullableExampleDataNull
        )

        val expectedValueList = listOf(
            10,
            "Hello",
            ExampleData(72, "Beep"),
            null
        )

        val iteratorToTest: Iterator<Any?> = optList.iterator().flatten()
        val iterableToTest: Iterable<Any?> = optList.asIterable().flatten()
        val sequenceToTest: Sequence<Any?> = optList.asSequence().flatten()

        assertContentEquals(expectedValueList.asSequence(), iteratorToTest.asSequence())
        assertContentEquals(expectedValueList, iterableToTest)
        assertContentEquals(expectedValueList.asSequence(), sequenceToTest)
    }

    @Test
    fun foldTests() {
        run {
            var a = false
            var b = false
            assertNull(
                optIntEmpty.fold(
                    { a = true; it },
                    { b = true; null }))
            assertFalse(a)
            assertTrue(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(TestMaybe.None,
                optIntEmpty.fold(
                    { a = true; TestMaybe.Some(it) },
                    { b = true; TestMaybe.None }))
            assertFalse(a)
            assertTrue(b)
        }

        run {
            var a = false
            var b = false
            assertNull(
                optStringEmpty.fold(
                    { a = true; it },
                    { b = true; null }))
            assertFalse(a)
            assertTrue(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(TestMaybe.None,
                optStringEmpty.fold(
                    { a = true; TestMaybe.Some(it) },
                    { b = true; TestMaybe.None }))
            assertFalse(a)
            assertTrue(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(TestMaybe.None,
                optNullableExampleDataEmpty.fold(
                    { a = true; TestMaybe.Some(it) },
                    { b = true; TestMaybe.None }))
            assertFalse(a)
            assertTrue(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(10,
                optIntFull.fold(
                    { a = true; it },
                    { b = true; null }))
            assertTrue(a)
            assertFalse(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(TestMaybe.Some(10),
                optIntFull.fold(
                    { a = true; TestMaybe.Some(it) },
                    { b = true; TestMaybe.None }))
            assertTrue(a)
            assertFalse(b)
        }

        run {
            var a = false
            var b = false
            assertEquals("Hello",
                optStringFull.fold(
                    { a = true; it },
                    { b = true; null }))
            assertTrue(a)
            assertFalse(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(TestMaybe.Some("Hello"),
                optStringFull.fold(
                    { a = true; TestMaybe.Some(it) },
                    { b = true; TestMaybe.None }))
            assertTrue(a)
            assertFalse(b)
        }

        run {
            var a = false
            var b = false
            assertEquals(TestMaybe.Some(ExampleData(72, "Beep")),
                optNullableExampleDataFull.fold(
                    { a = true; TestMaybe.Some(it) },
                    { b = true; TestMaybe.None }))
            assertTrue(a)
            assertFalse(b)
        }
    }

    @Test
    fun getByIndexTests() {
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty[-1] }
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty[0] }
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty[1] }
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty[2] }

        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty[-1] }
        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty[0] }
        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty[1] }
        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty[2] }

        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty[-1] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty[0] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty[1] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty[2] }

        assertFailsWith<IndexOutOfBoundsException> { optIntFull[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optIntFull[-1] }
        assertEquals(10, optIntFull[0])
        assertFailsWith<IndexOutOfBoundsException> { optIntFull[1] }
        assertFailsWith<IndexOutOfBoundsException> { optIntFull[2] }

        assertFailsWith<IndexOutOfBoundsException> { optStringFull[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optStringFull[-1] }
        assertEquals("Hello", optStringFull[0])
        assertFailsWith<IndexOutOfBoundsException> { optStringFull[1] }
        assertFailsWith<IndexOutOfBoundsException> { optStringFull[2] }

        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataFull[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataFull[-1] }
        assertEquals(ExampleData(72, "Beep"), optNullableExampleDataFull[0])
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataFull[1] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataFull[2] }

        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataNull[-2] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataNull[-1] }
        assertNull(optNullableExampleDataNull[0])
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataNull[1] }
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataNull[2] }
    }

    @Test
    fun getOrDefaultTests() {
        val ourDefaultInt = 20
        val ourDefaultString = "Not hello"
        val ourDefaultData = ExampleData(13, "Bad luck")

        assertEquals(ourDefaultInt, optIntEmpty.getOrDefault(ourDefaultInt))
        assertEquals(ourDefaultString, optStringEmpty.getOrDefault(ourDefaultString))
        assertEquals(ourDefaultData, optNullableExampleDataEmpty.getOrDefault(ourDefaultData))

        assertEquals(10, optIntFull.getOrDefault(ourDefaultInt))
        assertEquals("Hello", optStringFull.getOrDefault(ourDefaultString))
        assertEquals(ExampleData(72, "Beep"), optNullableExampleDataFull.getOrDefault(ourDefaultData))
        assertNull(optNullableExampleDataNull.getOrDefault(ourDefaultData))
    }

    @Test
    fun getOrElseTests() {
        val ourDefaultInt = 20
        val ourDefaultString = "Not hello"
        val ourDefaultData = ExampleData(13, "Bad luck")

        run {
            var defaultCalled = false
            assertEquals(ourDefaultInt, optIntEmpty.getOrElse { defaultCalled = true; ourDefaultInt })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertEquals(ourDefaultString, optStringEmpty.getOrElse { defaultCalled = true; ourDefaultString })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertEquals(
                ourDefaultData,
                optNullableExampleDataEmpty.getOrElse { defaultCalled = true; ourDefaultData })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertEquals(10, optIntFull.getOrElse { defaultCalled = true; ourDefaultInt })
            assertFalse(defaultCalled)
        }


        run {
            var defaultCalled = false
            assertEquals("Hello", optStringFull.getOrElse { defaultCalled = true; ourDefaultString })
            assertFalse(defaultCalled)
        }


        run {
            var defaultCalled = false
            assertEquals(
                ExampleData(72, "Beep"),
                optNullableExampleDataFull.getOrElse { defaultCalled = true; ourDefaultData })
            assertFalse(defaultCalled)
        }


        run {
            var defaultCalled = false
            assertNull(optNullableExampleDataNull.getOrElse { defaultCalled = true; ourDefaultData })
            assertFalse(defaultCalled)
        }
    }

    @Test
    fun getOrNullTests() {
        assertNull(optIntEmpty.getOrNull())
        assertNull(optStringEmpty.getOrNull())
        assertNull(optNullableExampleDataEmpty.getOrNull())

        assertEquals(10, optIntFull.getOrNull())
        assertEquals("Hello", optStringFull.getOrNull())
        assertEquals(ExampleData(72, "Beep"), optNullableExampleDataFull.getOrNull())

        assertNull(optNullableExampleDataNull.getOrNull())
    }

    @Test
    fun ifEmptyWithValueTests() {
        val ourDefaultInt = 20
        val ourDefaultString = "Not hello"
        val ourDefaultData = ExampleData(13, "Bad luck")

        assertOptEquals(optOf(ourDefaultInt), optIntEmpty.ifEmpty(ourDefaultInt))
        assertOptEquals(optOf(ourDefaultString), optStringEmpty.ifEmpty(ourDefaultString))
        assertOptEquals(optOf(ourDefaultData), optNullableExampleDataEmpty.ifEmpty(ourDefaultData))


        assertEquals(optIntFull, optIntFull.ifEmpty(ourDefaultInt))
        assertEquals(optStringFull, optStringFull.ifEmpty(ourDefaultString))
        assertEquals(optNullableExampleDataFull, optNullableExampleDataFull.ifEmpty(ourDefaultData))
        assertEquals(optNullableExampleDataNull, optNullableExampleDataNull.ifEmpty(ourDefaultData))
    }

    @Test
    fun ifEmptyWithCallbackTests() {
        val ourDefaultInt = 20
        val ourDefaultString = "Not hello"
        val ourDefaultData = ExampleData(13, "Bad luck")

        run {
            var defaultCalled = false
            assertEquals(optOf(ourDefaultInt), optIntEmpty.ifEmpty { defaultCalled = true; ourDefaultInt })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertEquals(optOf(ourDefaultString), optStringEmpty.ifEmpty { defaultCalled = true; ourDefaultString })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertEquals(
                optOf(ourDefaultData),
                optNullableExampleDataEmpty.ifEmpty { defaultCalled = true; ourDefaultData })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertEquals(optIntFull, optIntFull.ifEmpty { defaultCalled = true; ourDefaultInt })
            assertFalse(defaultCalled)
        }


        run {
            var defaultCalled = false
            assertEquals(optStringFull, optStringFull.ifEmpty { defaultCalled = true; ourDefaultString })
            assertFalse(defaultCalled)
        }


        run {
            var defaultCalled = false
            assertEquals(
                optNullableExampleDataFull,
                optNullableExampleDataFull.ifEmpty { defaultCalled = true; ourDefaultData })
            assertFalse(defaultCalled)
        }


        run {
            var defaultCalled = false
            assertEquals(optNullableExampleDataNull,
                optNullableExampleDataNull.ifEmpty { defaultCalled = true; ourDefaultData })
            assertFalse(defaultCalled)
        }
    }

    @Test
    fun listIteratorWorks() {
        fun <T> assertAtIndex(listIterator: ListIterator<T>, nextIndex: Int) {
            assertEquals(nextIndex, listIterator.nextIndex())
            assertEquals(nextIndex - 1, listIterator.previousIndex())
        }

        fun <T> assertAtEnd(listIterator: ListIterator<T>) {
            assertFalse(listIterator.hasNext())
            assertFailsWith<NoSuchElementException> { listIterator.next() }
        }

        fun <T> assertAtStart(listIterator: ListIterator<T>) {
            assertFalse(listIterator.hasPrevious())
            assertFailsWith<NoSuchElementException> { listIterator.previous() }
        }

        fun <T> assertEmptyIterator(listIterator: ListIterator<T>) {
            assertAtIndex(listIterator, 0)
            assertAtStart(listIterator)
            assertAtEnd(listIterator)
        }

        fun <T> assertSingletonIteratorAtStart(listIterator: ListIterator<T>, contents: T) {
            assertAtIndex(listIterator, 0)
            assertAtStart(listIterator)
            assertEquals(contents, listIterator.next())
            assertAtIndex(listIterator, 1)
            assertAtEnd(listIterator)
            assertEquals(contents, listIterator.previous())
            assertAtIndex(listIterator, 0)
            assertAtStart(listIterator)
        }

        fun <T> assertSingletonIteratorAtEnd(listIterator: ListIterator<T>, contents: T) {
            assertAtIndex(listIterator, 1)
            assertAtEnd(listIterator)
            assertEquals(contents, listIterator.previous())
            assertAtIndex(listIterator, 0)
            assertAtStart(listIterator)
            assertEquals(contents, listIterator.next())
            assertAtIndex(listIterator, 1)
            assertAtEnd(listIterator)
        }

        // As a control
        assertEmptyIterator(emptyList<Int>().listIterator())
        assertSingletonIteratorAtStart(listOf(123).listIterator(), 123)
        assertSingletonIteratorAtStart(listOf(123).listIterator(0), 123)
        assertSingletonIteratorAtEnd(listOf(123).listIterator(1), 123)

        assertEmptyIterator(optIntEmpty.listIterator())
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty.listIterator(-1) }
        assertEmptyIterator(optIntEmpty.listIterator(0))
        assertFailsWith<IndexOutOfBoundsException> { optIntEmpty.listIterator(1) }

        assertEmptyIterator(optStringEmpty.listIterator())
        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty.listIterator(-1) }
        assertEmptyIterator(optStringEmpty.listIterator(0))
        assertFailsWith<IndexOutOfBoundsException> { optStringEmpty.listIterator(1) }

        assertEmptyIterator(optNullableExampleDataEmpty.listIterator())
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty.listIterator(-1) }
        assertEmptyIterator(optNullableExampleDataEmpty.listIterator(0))
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataEmpty.listIterator(1) }

        assertSingletonIteratorAtStart(optIntFull.listIterator(), 10)
        assertFailsWith<IndexOutOfBoundsException> { optIntFull.listIterator(-1) }
        assertSingletonIteratorAtStart(optIntFull.listIterator(0), 10)
        assertSingletonIteratorAtEnd(optIntFull.listIterator(1), 10)
        assertFailsWith<IndexOutOfBoundsException> { optIntFull.listIterator(2) }

        assertSingletonIteratorAtStart(optStringFull.listIterator(), "Hello")
        assertFailsWith<IndexOutOfBoundsException> { optStringFull.listIterator(-1) }
        assertSingletonIteratorAtStart(optStringFull.listIterator(0), "Hello")
        assertSingletonIteratorAtEnd(optStringFull.listIterator(1), "Hello")
        assertFailsWith<IndexOutOfBoundsException> { optStringFull.listIterator(2) }

        assertSingletonIteratorAtStart(optNullableExampleDataFull.listIterator(), ExampleData(72, "Beep"))
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataFull.listIterator(-1) }
        assertSingletonIteratorAtStart(optNullableExampleDataFull.listIterator(0), ExampleData(72, "Beep"))
        assertSingletonIteratorAtEnd(optNullableExampleDataFull.listIterator(1), ExampleData(72, "Beep"))
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataFull.listIterator(2) }

        assertSingletonIteratorAtStart(optNullableExampleDataNull.listIterator(), null)
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataNull.listIterator(-1) }
        assertSingletonIteratorAtStart(optNullableExampleDataNull.listIterator(0), null)
        assertSingletonIteratorAtEnd(optNullableExampleDataNull.listIterator(1), null)
        assertFailsWith<IndexOutOfBoundsException> { optNullableExampleDataNull.listIterator(2) }
    }

    @Test
    fun mapNotNullTests() {
        fun <T> test(opt: Opt<T>) {
            val listCopy = opt.testCopyToList()

            val transform1: (T) -> Pair<T, Boolean>? = { it to true }
            val transform2: (T) -> Pair<T, Boolean>? = { it to false }
            val transform3: (T) -> Pair<T, Boolean>? = { null }

            for (transform in listOf(transform1, transform2, transform3)) {
                assertOptEquals(opt.map { transform(it) }.filterNotNull(), opt.mapNotNull { transform(it) })
                assertOptEquals(listCopy.mapNotNull { transform(it) }.testCopyToOpt(), opt.mapNotNull { transform(it) })
            }
        }

        test(optIntEmpty)
        test(optIntFull)
        test(optStringEmpty)
        test(optStringFull)
        test(optNullableExampleDataEmpty)
        test(optNullableExampleDataFull)
        test(optNullableExampleDataNull)
    }

    @Test
    fun minusElementTests() {
        // Controls
        assertContentEquals(emptyList(), emptyList<Int>() - 10)
        assertContentEquals(emptyList(), listOf(10) - 10)
        assertContentEquals(listOf(10), listOf(10) - 12)

        // Tests
        assertOptEquals(optIntEmpty, optIntFull - 10)
        assertOptEquals(optStringEmpty, optStringEmpty - "Hello")
        assertOptEquals(optNullableExampleDataEmpty, optNullableExampleDataEmpty - ExampleData(72, "Beep"))

        assertOptEmpty(optIntFull - 10)
        assertOptEmpty(optStringEmpty - "Hello")
        assertOptEmpty(optNullableExampleDataFull - ExampleData(72, "Beep"))
        assertOptEmpty(optNullableExampleDataNull - null)

        assertOptEquals(optIntFull, optIntFull - 12)
        assertOptEquals(optStringFull, optStringFull - "Not Hello")
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - ExampleData(13, "Bad luck"))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - ExampleData(13, "Bad luck"))
    }

    @Test
    fun minusOptTests() {
        // Controls
        assertContentEquals(emptyList(), emptyList<Int>() - emptyList())
        assertContentEquals(emptyList(), emptyList<Int>() - listOf(10))
        assertContentEquals(emptyList(), listOf(10) - listOf(10))
        assertContentEquals(listOf(10), listOf(10) - listOf(12))
        assertContentEquals(listOf(10), listOf(10) - emptyList())

        // Tests
        assertOptEquals(optIntEmpty, optIntFull - optOf(10))
        assertOptEquals(optStringEmpty, optStringEmpty - optOf("Hello"))
        assertOptEquals(optNullableExampleDataEmpty, optNullableExampleDataEmpty - optOf(ExampleData(72, "Beep")))

        assertOptEmpty(optIntFull - optOf(10))
        assertOptEmpty(optStringEmpty - optOf("Hello"))
        assertOptEmpty(optNullableExampleDataFull - optOf(ExampleData(72, "Beep")))
        assertOptEmpty(optNullableExampleDataNull - optOf(null))

        assertOptEquals(optIntFull, optIntFull - optOf(12))
        assertOptEquals(optStringFull, optStringFull - optOf("Not Hello"))
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - optOf(ExampleData(13, "Bad luck")))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - optOf(ExampleData(13, "Bad luck")))

        assertOptEquals(optIntEmpty, optIntEmpty - emptyOpt())
        assertOptEquals(optStringEmpty, optStringEmpty - emptyOpt())
        assertOptEquals(optNullableExampleDataEmpty, optNullableExampleDataEmpty - emptyOpt())
        assertOptEquals(optIntFull, optIntFull - emptyOpt())
        assertOptEquals(optStringFull, optStringFull - emptyOpt())
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - emptyOpt())
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - emptyOpt())
    }

    @Test
    fun minusIterableTests() {
        // Controls
        assertContentEquals(emptyList(), emptyList<Int>() - emptyList())
        assertContentEquals(emptyList(), emptyList<Int>() - listOf(10))
        assertContentEquals(emptyList(), emptyList<Int>() - listOf(10, 12))
        assertContentEquals(emptyList(), listOf(10) - listOf(10))
        assertContentEquals(emptyList(), listOf(10) - listOf(10, 12))
        assertContentEquals(listOf(10), listOf(10) - listOf(12))
        assertContentEquals(listOf(10), listOf(10) - listOf(12, 14))
        assertContentEquals(listOf(10), listOf(10) - emptyList())

        assertOptEmpty(optIntEmpty - emptyList())
        assertOptEmpty(optIntEmpty - listOf(10))
        assertOptEmpty(optIntEmpty - listOf(10, 12))
        assertOptEmpty(optIntFull - listOf(10))
        assertOptEmpty(optIntFull - listOf(10, 12))
        assertOptEquals(optIntFull, optIntFull - listOf(12))
        assertOptEquals(optIntFull, optIntFull - listOf(12, 14))
        assertOptEquals(optIntFull, optIntFull - emptyList())

        assertOptEmpty(optStringEmpty - emptyList())
        assertOptEmpty(optStringEmpty - listOf("Hello"))
        assertOptEmpty(optStringEmpty - listOf("Hello", "Not hello"))
        assertOptEmpty(optStringFull - listOf("Hello"))
        assertOptEmpty(optStringFull - listOf("Hello", "Not hello"))
        assertOptEquals(optStringFull, optStringFull - listOf("Not hello"))
        assertOptEquals(optStringFull, optStringFull - listOf("Not hello", "Still not hello"))
        assertOptEquals(optStringFull, optStringFull - emptyList())

        val dataValue = ExampleData(72, "Beep")
        val notDataValue1 = ExampleData(13, "Bad luck")
        val notDataValue2 = ExampleData(17, "Really")
        assertOptEmpty(optNullableExampleDataEmpty - emptyList())
        assertOptEmpty(optNullableExampleDataEmpty - listOf(dataValue))
        assertOptEmpty(optNullableExampleDataEmpty - listOf(dataValue, notDataValue1))
        assertOptEmpty(optNullableExampleDataFull - listOf(dataValue))
        assertOptEmpty(optNullableExampleDataFull - listOf(dataValue, notDataValue1))
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - listOf(notDataValue1))
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - listOf(notDataValue1, notDataValue2))
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - emptyList())
        assertOptEmpty(optNullableExampleDataNull - listOf(null))
        assertOptEmpty(optNullableExampleDataNull - listOf(null, notDataValue1))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - listOf(notDataValue1))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - listOf(notDataValue1, notDataValue2))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - emptyList())
    }

    @Test
    fun minusSequenceTests() {
        // Controls
        assertContentEquals(emptySequence(), emptySequence<Int>() - emptySequence())
        assertContentEquals(emptySequence(), emptySequence<Int>() - sequenceOf(10))
        assertContentEquals(emptySequence(), emptySequence<Int>() - sequenceOf(10, 12))
        assertContentEquals(emptySequence(), sequenceOf(10) - sequenceOf(10))
        assertContentEquals(emptySequence(), sequenceOf(10) - sequenceOf(10, 12))
        assertContentEquals(sequenceOf(10), sequenceOf(10) - sequenceOf(12))
        assertContentEquals(sequenceOf(10), sequenceOf(10) - sequenceOf(12, 14))
        assertContentEquals(sequenceOf(10), sequenceOf(10) - emptySequence())

        assertOptEmpty(optIntEmpty - emptySequence())
        assertOptEmpty(optIntEmpty - sequenceOf(10))
        assertOptEmpty(optIntEmpty - sequenceOf(10, 12))
        assertOptEmpty(optIntFull - sequenceOf(10))
        assertOptEmpty(optIntFull - sequenceOf(10, 12))
        assertOptEquals(optIntFull, optIntFull - sequenceOf(12))
        assertOptEquals(optIntFull, optIntFull - sequenceOf(12, 14))
        assertOptEquals(optIntFull, optIntFull - emptySequence())

        assertOptEmpty(optStringEmpty - emptySequence())
        assertOptEmpty(optStringEmpty - sequenceOf("Hello"))
        assertOptEmpty(optStringEmpty - sequenceOf("Hello", "Not hello"))
        assertOptEmpty(optStringFull - sequenceOf("Hello"))
        assertOptEmpty(optStringFull - sequenceOf("Hello", "Not hello"))
        assertOptEquals(optStringFull, optStringFull - sequenceOf("Not hello"))
        assertOptEquals(optStringFull, optStringFull - sequenceOf("Not hello", "Still not hello"))
        assertOptEquals(optStringFull, optStringFull - emptySequence())

        val dataValue = ExampleData(72, "Beep")
        val notDataValue1 = ExampleData(13, "Bad luck")
        val notDataValue2 = ExampleData(17, "Really")
        assertOptEmpty(optNullableExampleDataEmpty - emptySequence())
        assertOptEmpty(optNullableExampleDataEmpty - sequenceOf(dataValue))
        assertOptEmpty(optNullableExampleDataEmpty - sequenceOf(dataValue, notDataValue1))
        assertOptEmpty(optNullableExampleDataFull - sequenceOf(dataValue))
        assertOptEmpty(optNullableExampleDataFull - sequenceOf(dataValue, notDataValue1))
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - sequenceOf(notDataValue1))
        assertOptEquals(optNullableExampleDataFull,
            optNullableExampleDataFull - sequenceOf(notDataValue1, notDataValue2))
        assertOptEquals(optNullableExampleDataFull, optNullableExampleDataFull - emptySequence())
        assertOptEmpty(optNullableExampleDataNull - sequenceOf(null))
        assertOptEmpty(optNullableExampleDataNull - sequenceOf(null, notDataValue1))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - sequenceOf(notDataValue1))
        assertOptEquals(optNullableExampleDataNull,
            optNullableExampleDataNull - sequenceOf(notDataValue1, notDataValue2))
        assertOptEquals(optNullableExampleDataNull, optNullableExampleDataNull - emptySequence())
    }

    @Test
    fun replaceIfEmptyWithOptTests() {
        val ourDefaultInt = 20
        val ourDefaultString = "Not hello"
        val ourDefaultData = ExampleData(13, "Bad luck")

        // Supplying full
        assertOptEquals(optOf(ourDefaultInt), optIntEmpty.replaceIfEmpty(optOf(ourDefaultInt)))
        assertOptEquals(optOf(ourDefaultString), optStringEmpty.replaceIfEmpty(optOf(ourDefaultString)))
        assertOptEquals(optOf(ourDefaultData), optNullableExampleDataEmpty.replaceIfEmpty(optOf(ourDefaultData)))

        assertEquals(optIntFull, optIntFull.replaceIfEmpty(optOf(ourDefaultInt)))
        assertEquals(optStringFull, optStringFull.replaceIfEmpty(optOf(ourDefaultString)))
        assertEquals(optNullableExampleDataFull, optNullableExampleDataFull.replaceIfEmpty(optOf(ourDefaultData)))
        assertEquals(optNullableExampleDataNull, optNullableExampleDataNull.replaceIfEmpty(optOf(ourDefaultData)))

        // Supplying empty
        assertOptEmpty(optIntEmpty.replaceIfEmpty(emptyOpt()))
        assertOptEmpty(optStringEmpty.replaceIfEmpty(emptyOpt()))
        assertOptEmpty(optNullableExampleDataEmpty.replaceIfEmpty(emptyOpt()))

        assertEquals(optIntFull, optIntFull.replaceIfEmpty(emptyOpt()))
        assertEquals(optStringFull, optStringFull.replaceIfEmpty(emptyOpt()))
        assertEquals(optNullableExampleDataFull, optNullableExampleDataFull.replaceIfEmpty(emptyOpt()))
        assertEquals(optNullableExampleDataNull, optNullableExampleDataNull.replaceIfEmpty(emptyOpt()))
    }

    @Test
    fun replaceIfEmptyWithCallbackTests() {
        val ourDefaultInt = 20
        val ourDefaultString = "Not hello"
        val ourDefaultData = ExampleData(13, "Bad luck")

        // Supplying full

        run {
            var defaultCalled = false
            assertOptEquals(optOf(ourDefaultInt),
                optIntEmpty.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultInt) })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optOf(ourDefaultString),
                optStringEmpty.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultString) })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(
                optOf(ourDefaultData),
                optNullableExampleDataEmpty.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultData) })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optIntFull, optIntFull.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultInt) })
            assertFalse(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optStringFull,
                optStringFull.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultString) })
            assertFalse(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(
                optNullableExampleDataFull,
                optNullableExampleDataFull.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultData) })
            assertFalse(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optNullableExampleDataNull,
                optNullableExampleDataNull.replaceIfEmpty { defaultCalled = true; optOf(ourDefaultData) })
            assertFalse(defaultCalled)
        }

        // Supplying empty

        run {
            var defaultCalled = false
            assertOptEmpty(optIntEmpty.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEmpty(optStringEmpty.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEmpty(optNullableExampleDataEmpty.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertTrue(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optIntFull, optIntFull.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertFalse(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optStringFull, optStringFull.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertFalse(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(
                optNullableExampleDataFull,
                optNullableExampleDataFull.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertFalse(defaultCalled)
        }

        run {
            var defaultCalled = false
            assertOptEquals(optNullableExampleDataNull,
                optNullableExampleDataNull.replaceIfEmpty { defaultCalled = true; emptyOpt() })
            assertFalse(defaultCalled)
        }
    }

    @Test
    fun shallowMembersTest() {

        // For the sake of clearing a few "unused" warnings the right way, here we test members that don't merit their
        // own full tests because they are based on other methods in a direct or nearly direct fashion.
        fun <T> test(opt: Opt<T>, valueForType: T) {
            val listCopy = opt.testCopyToList()
            val valuesToTry = listOf(valueForType) + listCopy
            val predicate: (T) -> Boolean = { valuesToTry.contains(it) }

            assertEquals(opt.hasValue, opt.any()) // alias
            assertEquals(opt.asList(), opt.asIterable()) // alias
            assertContentEquals(listCopy, opt.asList()) // tested in depth at OptListViewTest

            assertEquals(listCopy.count(), opt.count()) // see any()
            assertEquals(listCopy.count { predicate(it) }, opt.count { predicate(it) }) // see any(predicate)

            // see flatten()
            assertOptEquals(opt, optOf(opt).flatten1())
            assertOptEquals(opt, optOf(optOf(opt)).flatten2())
            assertOptEquals(opt, optOf(optOf(optOf(opt))).flatten3())
            assertOptEquals(opt, optOf(optOf(optOf(optOf(opt)))).flatten4())
            assertOptEquals(opt, optOf(optOf(optOf(optOf(optOf(opt))))).flatten5())
            assertOptEquals(opt, optOf(optOf(optOf(optOf(optOf(optOf(opt)))))).flatten6())
            assertOptEquals(opt, optOf(optOf(optOf(optOf(optOf(optOf(optOf(opt))))))).flatten7())
            assertOptEquals(opt, optOf(optOf(optOf(optOf(optOf(optOf(optOf(optOf(opt)))))))).flatten8())

            // see value
            if (opt.hasValue) {
                assertEquals(opt.value, opt.get())
            } else {
                assertFailsWith<NoSuchElementException> { opt.get() }
            }

            // see contains()
            for (it in valuesToTry) {
                val copy = opt.testCopyToList()
                assertEquals(copy.indexOf(it), opt.indexOf(it))
                assertEquals(copy.lastIndexOf(it), opt.lastIndexOf(it))
            }

            assertEquals(!opt.hasValue, opt.isEmpty()) // alias

            // see listIterator()
            assertContentEquals(listCopy.asSequence(), opt.iterator().asSequence())

            assertEquals(listCopy.none(), opt.none()) // see any()
            assertEquals(listCopy.none { predicate(it) }, opt.none { predicate(it) }) // see any(predicate)

            run {
                val buffer = mutableListOf<T>()
                val onEachResult = opt.onEach { buffer.add(it) } // see forEach(action)
                assertEquals(opt, onEachResult)
                assertContentEquals(listCopy, buffer)
            }

            assertEquals(listCopy.size, opt.size) // see hasValue
        }

        test(optIntEmpty, 25)
        test(optIntFull, 25)
        test(optStringEmpty, "Not hello")
        test(optStringFull, "Not hello")
        test(optNullableExampleDataEmpty, ExampleData(13, "Bad luck"))
        test(optNullableExampleDataFull, ExampleData(13, "Bad luck"))
        test(optNullableExampleDataNull, ExampleData(13, "Bad luck"))
    }

    @Test
    fun zipTests() {
        fun <A, B> testInfix(left: Opt<A>, right: Opt<B>) {
            val leftHasValue = left.hasValue
            val rightHasValue = right.hasValue

            val result = left zip right

            if (leftHasValue && rightHasValue) {
                assertOptEquals(optOf(left.value to right.value), result)
            } else {
                assertOptEmpty(result)
            }
        }

        fun <A, B> testWithTransform(left: Opt<A>, right: Opt<B>) {
            val leftHasValue = left.hasValue
            val rightHasValue = right.hasValue

            // Intentionally slightly different to the infix result
            val result = left.zip(right) { a, b -> Pair(b, a) }

            if (leftHasValue && rightHasValue) {
                assertOptEquals(optOf(right.value to left.value), result)
            } else {
                assertOptEmpty(result)
            }
        }

        fun <A, B> test(left: Opt<A>, right: Opt<B>) {
            testInfix(left, right)
            testWithTransform(left, right)
        }

        test(optIntEmpty, optIntEmpty)
        test(optIntEmpty, optStringEmpty)
        test(optIntEmpty, optNullableExampleDataEmpty)
        test(optIntEmpty, optIntFull)
        test(optIntEmpty, optStringFull)
        test(optIntEmpty, optNullableExampleDataFull)
        test(optIntEmpty, optNullableExampleDataNull)
        test(optStringEmpty, optIntEmpty)
        test(optStringEmpty, optStringEmpty)
        test(optStringEmpty, optNullableExampleDataEmpty)
        test(optStringEmpty, optIntFull)
        test(optStringEmpty, optStringFull)
        test(optStringEmpty, optNullableExampleDataFull)
        test(optStringEmpty, optNullableExampleDataNull)
        test(optNullableExampleDataEmpty, optIntEmpty)
        test(optNullableExampleDataEmpty, optStringEmpty)
        test(optNullableExampleDataEmpty, optNullableExampleDataEmpty)
        test(optNullableExampleDataEmpty, optIntFull)
        test(optNullableExampleDataEmpty, optStringFull)
        test(optNullableExampleDataEmpty, optNullableExampleDataFull)
        test(optNullableExampleDataEmpty, optNullableExampleDataNull)
        test(optIntFull, optIntEmpty)
        test(optIntFull, optStringEmpty)
        test(optIntFull, optNullableExampleDataEmpty)
        test(optIntFull, optIntFull)
        test(optIntFull, optStringFull)
        test(optIntFull, optNullableExampleDataFull)
        test(optIntFull, optNullableExampleDataNull)
        test(optStringFull, optIntEmpty)
        test(optStringFull, optStringEmpty)
        test(optStringFull, optNullableExampleDataEmpty)
        test(optStringFull, optIntFull)
        test(optStringFull, optStringFull)
        test(optStringFull, optNullableExampleDataFull)
        test(optStringFull, optNullableExampleDataNull)
        test(optNullableExampleDataFull, optIntEmpty)
        test(optNullableExampleDataFull, optStringEmpty)
        test(optNullableExampleDataFull, optNullableExampleDataEmpty)
        test(optNullableExampleDataFull, optIntFull)
        test(optNullableExampleDataFull, optStringFull)
        test(optNullableExampleDataFull, optNullableExampleDataFull)
        test(optNullableExampleDataFull, optNullableExampleDataNull)
        test(optNullableExampleDataNull, optIntEmpty)
        test(optNullableExampleDataNull, optStringEmpty)
        test(optNullableExampleDataNull, optNullableExampleDataEmpty)
        test(optNullableExampleDataNull, optIntFull)
        test(optNullableExampleDataNull, optStringFull)
        test(optNullableExampleDataNull, optNullableExampleDataFull)
        test(optNullableExampleDataNull, optNullableExampleDataNull)
    }

    //endregion

    //region Any-like overrides

    // Explicit definitions for equals() and hashCode() are unavailable for the current version of Kotlin as of this
    // writing; we're simply testing to see whether they behave in a reasonable fashion.

    // The preferred way to test value equality (==) is using `contentEquals`. The preferred way to test referential
    // equality is using `contentSameAs`.

    @Test
    @Suppress("ReplaceAssertBooleanWithAssertEquality")
    fun equalsTests() {
        val intAsInt = 123
        @Suppress("RedundantNullableReturnType") val intAsNullableAny: Any? = 123

        assertTrue(emptyOpt<Int>() == emptyOpt<Int>())
        assertTrue(emptyOpt<Int>() == emptyOpt<Any>())

        // Presumably legal because either can be null
        assertTrue(emptyOpt<Int?>() == emptyOpt<String?>())

        // not allowed, but emptyOpt<Int> contentEquals emptyOpt<String?> is legal
        // assertTrue(emptyOpt<Int>() == emptyOpt<String?>())

        assertTrue(optOf(intAsInt) == optOf(intAsInt))
        assertTrue(optOf(intAsInt) == optOf(intAsNullableAny))
        assertTrue(optOf<Int?>(null) == optOf<String?>(null))

        val data1 = ExampleData(72, "Beep")
        val data2 = ExampleData(72, "Beep")
        assertEquals(data1, data2)
        assertNotSame(data1, data2)
        assertTrue(optOf(data1) == optOf(data2))
    }

    @Test
    fun hashCodeTests() {
        val intAsInt = 123
        var otherIntAsInt = 456

        for (n in 1..100000) {
            if (intAsInt.hashCode() != otherIntAsInt.hashCode()) {
                otherIntAsInt++
            }
        }

        @Suppress("RedundantNullableReturnType") val intAsNullableAny: Any? = intAsInt
        @Suppress("RedundantNullableReturnType") val otherIntAsNullableAny: Any? = otherIntAsInt

        if (intAsInt.hashCode() == otherIntAsInt.hashCode()) {
            throw Exception("Something weird is going on with Int hashCode implementation.")
        }

        assertEquals(intAsInt.hashCode(), intAsNullableAny.hashCode())

        if (intAsInt.hashCode() != intAsNullableAny.hashCode() ||
            otherIntAsInt.hashCode() != otherIntAsNullableAny.hashCode()
        ) {
            throw Exception("Int hashCode changes when wrapped in Any?.")
        }

        assertEquals(emptyOpt<Int>().hashCode(), emptyOpt<Int>().hashCode())
        assertEquals(emptyOpt<Int>().hashCode(), emptyOpt<Int?>().hashCode())
        assertEquals(emptyOpt<Int>().hashCode(), emptyOpt<String?>().hashCode())

        assertEquals(optOf(intAsInt).hashCode(), optOf(intAsNullableAny).hashCode())
        assertEquals(optOf(otherIntAsInt).hashCode(), optOf(otherIntAsNullableAny).hashCode())
        assertNotEquals(optOf(intAsInt).hashCode(), optOf(otherIntAsInt).hashCode())
        assertNotEquals(optOf(intAsNullableAny).hashCode(), optOf(otherIntAsNullableAny).hashCode())

        val data1 = ExampleData(72, "Beep")
        val data2 = ExampleData(72, "Beep")
        assertEquals(optOf(data1).hashCode(), optOf(data2).hashCode())
    }

    @Test
    fun toStringTests() {
        fun <T> test(opt: Opt<T>) {
            val expectedString = if (opt.hasValue) "optOf(${opt.value})" else "emptyOpt()"
            assertEquals(expectedString, "$opt")
        }

        test(optIntEmpty)
        test(optStringEmpty)
        test(optNullableExampleDataEmpty)
        test(optIntFull)
        test(optStringFull)
        test(optNullableExampleDataFull)
        test(optNullableExampleDataNull)
    }

    //endregion
}