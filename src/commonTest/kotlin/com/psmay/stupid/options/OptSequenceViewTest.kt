package com.psmay.stupid.options

import kotlin.test.Test
import kotlin.test.assertContentEquals

internal class OptSequenceViewTest {
    // Opt.SequenceView is backed by extension methods on Opt that are tested separately, so we don't do much here.

    private val optIntEmpty: Opt<Int> = emptyOpt()
    private val optStringEmpty: Opt<String> = emptyOpt()
    private val optIntFull: Opt<Int> = optOf(10)
    private val optStringFull: Opt<String> = optOf("Hello")

    @Test
    fun sequenceAsListAndSequenceAsIterableAreSimilarObjects() {
        fun <T> test(opt: Opt<T>) {
            val sequenceView = opt.asSequence()

            val asList: Opt.ListView<T> = sequenceView.asList()
            val asIterable: Opt.ListView<T> = sequenceView.asIterable()
            assertOptEquals(asList.asOpt(), asIterable.asOpt())
        }

        test(optIntEmpty)
        test(optStringEmpty)
        test(optIntFull)
        test(optStringFull)
    }

    @Test
    fun sequenceContentSameAsOpt() {
        fun <T> test(opt: Opt<T>) {
            val expected = opt.testCopyToList().asSequence()
            val actual = opt.asSequence()
            assertContentEquals(expected, actual)
        }

        test(optIntEmpty)
        test(optStringEmpty)
        test(optIntFull)
        test(optStringFull)
    }
}