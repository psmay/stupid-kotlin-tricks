package com.psmay.stupid.options

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class OptListViewTest {
    // Opt.ListView is almost exclusively backed by extension methods on Opt itself, so we won't test most of them here.

    private val optIntEmpty: Opt<Int> = emptyOpt()
    private val optStringEmpty: Opt<String> = emptyOpt()
    private val optIntFull: Opt<Int> = optOf(10)
    private val optStringFull: Opt<String> = optOf("Hello")

    @Test
    fun listViewAndListViewAsIterableAreSimilarObjects() {
        fun <T> test(opt: Opt<T>) {
            val listView: Opt.ListView<T> = opt.asList()
            val listViewFromIterable: Opt.ListView<T> = listView.asIterable()
            assertOptEquals(listView.asOpt(), listViewFromIterable.asOpt())
        }

        test(optIntEmpty)
        test(optStringEmpty)
        test(optIntFull)
        test(optStringFull)
    }

    @Test
    fun listViewAsSequenceAndOptAsSequenceAreSimilarObjects() {
        fun <T> test(opt: Opt<T>) {
            val listView = opt.asList()
            val listViewAsSequence: Opt.SequenceView<T> = listView.asSequence()
            val optAsSequence: Opt.SequenceView<T> = opt.asSequence()
            assertOptEquals(listViewAsSequence.asOpt(), optAsSequence.asOpt())
        }

        test(optIntEmpty)
        test(optStringEmpty)
        test(optIntFull)
        test(optStringFull)
    }

    @Test
    fun listContentSameAsOpt() {
        fun <T> test(opt: Opt<T>) {
            val expected = opt.testCopyToList()
            val actual = opt.asList()
            assertEquals(expected.size, actual.size)
            assertContentEquals(expected, actual)
        }

        test(optIntEmpty)
        test(optStringEmpty)
        test(optIntFull)
        test(optStringFull)
    }
}