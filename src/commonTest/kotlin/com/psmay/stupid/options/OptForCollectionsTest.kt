package com.psmay.stupid.options

import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
internal class OptForCollectionsTest {
    private val listIntEmpty = emptyList<Int>()
    private val listIntFull = listOf(11, 22, 33)
    //private val listIntSingle = listOf(11)
    private val iterableIntEmpty = emptySequence<Int>().asIterable()
    private val iterableIntFull = sequenceOf(11, 22, 33).asIterable()
    //private val iterableIntSingle = sequenceOf(11).asIterable()
    //private val sequenceIntEmpty = emptySequence<Int>()
    //private val sequenceIntFull = sequenceOf(11, 22, 33)
    //private val sequenceIntSingle = sequenceOf(11)

    private val mapStringNullStringEmpty = emptyList<Pair<String, String?>>().toMap()

    private val mapStringNullStringFull = listOf(
        "alpha" to "a",
        "bravo" to "b",
        "_kot" to null,
        "_lin" to null,
    ).toMap()

    private val listNullStringEmpty = emptyList<String?>()
    private val listNullStringFull = listOf("a", null, "ka", null, "sa", null)
    private val listNullStringSingleNull = listOf<String?>(null)
    private val listNullStringSingleNotNull = listOf("a")

    @Test
    fun iterableElementAtOptTest() {
        assertOptEmpty(listIntEmpty.elementAtOpt(-1))
        assertOptEmpty(listIntEmpty.elementAtOpt(0))
        assertOptEmpty(listIntEmpty.elementAtOpt(1))

        assertOptEmpty(iterableIntEmpty.elementAtOpt(-1))
        assertOptEmpty(iterableIntEmpty.elementAtOpt(0))
        assertOptEmpty(iterableIntEmpty.elementAtOpt(1))

        assertOptEmpty(listIntFull.elementAtOpt(-1))
        assertOptEquals(optOf(11), listIntFull.elementAtOpt(0))
        assertOptEquals(optOf(22), listIntFull.elementAtOpt(1))
        assertOptEquals(optOf(33), listIntFull.elementAtOpt(2))
        assertOptEmpty(listIntFull.elementAtOpt(3))

        assertOptEmpty(iterableIntFull.elementAtOpt(-1))
        assertOptEquals(optOf(11), iterableIntFull.elementAtOpt(0))
        assertOptEquals(optOf(22), iterableIntFull.elementAtOpt(1))
        assertOptEquals(optOf(33), iterableIntFull.elementAtOpt(2))
        assertOptEmpty(iterableIntFull.elementAtOpt(3))
    }

    @Test
    fun iterableFirstOptTest() {
        assertOptEmpty(listIntEmpty.firstOpt())
        assertOptEquals(optOf(11), listIntFull.firstOpt())
        assertOptEmpty(iterableIntEmpty.firstOpt())
        assertOptEquals(optOf(11), iterableIntFull.firstOpt())

        assertOptEmpty(listIntEmpty.firstOpt { it > 15 })
        assertOptEquals(optOf(22), listIntFull.firstOpt { it > 15 })
        assertOptEmpty(iterableIntEmpty.firstOpt { it > 15 })
        assertOptEquals(optOf(22), iterableIntFull.firstOpt { it > 15 })

        assertOptEmpty(listIntEmpty.firstOpt { it > 50 })
        assertOptEmpty(listIntFull.firstOpt { it > 50 })
        assertOptEmpty(iterableIntEmpty.firstOpt { it > 50 })
        assertOptEmpty(iterableIntFull.firstOpt { it > 50 })
    }

    @Test
    fun listGetOptTest() {
        assertOptEmpty(listIntEmpty.getOpt(-1))
        assertOptEmpty(listIntEmpty.getOpt(0))
        assertOptEmpty(listIntEmpty.getOpt(1))

        assertOptEmpty(listIntFull.getOpt(-1))
        assertOptEquals(optOf(11), listIntFull.getOpt(0))
        assertOptEquals(optOf(22), listIntFull.getOpt(1))
        assertOptEquals(optOf(33), listIntFull.getOpt(2))
        assertOptEmpty(listIntFull.getOpt(3))
    }

    @Test
    fun mapGetOptTest() {
        assertOptEmpty(mapStringNullStringEmpty.getOpt("alpha"))
        assertOptEmpty(mapStringNullStringEmpty.getOpt("bravo"))
        assertOptEmpty(mapStringNullStringEmpty.getOpt("_kot"))
        assertOptEmpty(mapStringNullStringEmpty.getOpt("_lin"))
        assertOptEmpty(mapStringNullStringEmpty.getOpt("not a key"))

        assertOptEquals(optOf("a"), mapStringNullStringFull.getOpt("alpha"))
        assertOptEquals(optOf("b"), mapStringNullStringFull.getOpt("bravo"))
        assertOptEquals(optOf(null), mapStringNullStringFull.getOpt("_kot"))
        assertOptEquals(optOf(null), mapStringNullStringFull.getOpt("_lin"))
        assertOptEmpty(mapStringNullStringFull.getOpt("not a key"))
    }

    @Test
    fun iterableLastOptTest() {
        assertOptEmpty(listIntEmpty.lastOpt())
        assertOptEquals(optOf(33), listIntFull.lastOpt())
        assertOptEmpty(iterableIntEmpty.lastOpt())
        assertOptEquals(optOf(33), iterableIntFull.lastOpt())

        assertOptEmpty(listIntEmpty.lastOpt { it > 15 })
        assertOptEquals(optOf(33), listIntFull.lastOpt { it > 15 })
        assertOptEmpty(iterableIntEmpty.lastOpt { it > 15 })
        assertOptEquals(optOf(33), iterableIntFull.lastOpt { it > 15 })

        assertOptEmpty(listIntEmpty.lastOpt { it > 50 })
        assertOptEmpty(listIntFull.lastOpt { it > 50 })
        assertOptEmpty(iterableIntEmpty.lastOpt { it > 50 })
        assertOptEmpty(iterableIntFull.lastOpt { it > 50 })
    }

    @Test
    fun iteratorLastOptTest() {
        assertOptEmpty(listIntEmpty.iterator().lastOpt())
        assertOptEquals(optOf(33), listIntFull.iterator().lastOpt())
        assertOptEmpty(iterableIntEmpty.iterator().lastOpt())
        assertOptEquals(optOf(33), iterableIntFull.iterator().lastOpt())

        assertOptEmpty(listIntEmpty.iterator().lastOpt { it > 15 })
        assertOptEquals(optOf(33), listIntFull.iterator().lastOpt { it > 15 })
        assertOptEmpty(iterableIntEmpty.iterator().lastOpt { it > 15 })
        assertOptEquals(optOf(33), iterableIntFull.iterator().lastOpt { it > 15 })

        assertOptEmpty(listIntEmpty.iterator().lastOpt { it > 50 })
        assertOptEmpty(listIntFull.iterator().lastOpt { it > 50 })
        assertOptEmpty(iterableIntEmpty.iterator().lastOpt { it > 50 })
        assertOptEmpty(iterableIntFull.iterator().lastOpt { it > 50 })
    }

    @Test
    fun iteratorNextOptTest() {
        assertOptEmpty(listIntEmpty.iterator().nextOpt())
        assertOptEquals(optOf(11), listIntFull.iterator().nextOpt())
        assertOptEmpty(iterableIntEmpty.iterator().nextOpt())
        assertOptEquals(optOf(11), iterableIntFull.iterator().nextOpt())

        assertOptEmpty(listIntEmpty.iterator().nextOpt { it > 15 })
        assertOptEquals(optOf(22), listIntFull.iterator().nextOpt { it > 15 })
        assertOptEmpty(iterableIntEmpty.iterator().nextOpt { it > 15 })
        assertOptEquals(optOf(22), iterableIntFull.iterator().nextOpt { it > 15 })

        assertOptEmpty(listIntEmpty.iterator().nextOpt { it > 50 })
        assertOptEmpty(listIntFull.iterator().nextOpt { it > 50 })
        assertOptEmpty(iterableIntEmpty.iterator().nextOpt { it > 50 })
        assertOptEmpty(iterableIntFull.iterator().nextOpt { it > 50 })
    }

    @Test
    fun mutableListRemoveFirstOptTest() {
        run {
            val m = listNullStringEmpty.toMutableList()
            assertOptEmpty(m.removeFirstOpt())
            assertOptEmpty(m.removeFirstOpt())
        }

        run {
            val m = listNullStringFull.toMutableList()
            assertOptEquals(optOf("a"), m.removeFirstOpt())
            assertOptEquals(optOf(null), m.removeFirstOpt())
            assertOptEquals(optOf("ka"), m.removeFirstOpt())
            assertOptEquals(optOf(null), m.removeFirstOpt())
            assertOptEquals(optOf("sa"), m.removeFirstOpt())
            assertOptEquals(optOf(null), m.removeFirstOpt())
            assertOptEmpty(m.removeFirstOpt())
            assertOptEmpty(m.removeFirstOpt())
        }
    }

    @Test
    fun mutableListRemoveLastOptTest() {
        run {
            val m = listNullStringEmpty.toMutableList()
            assertOptEmpty(m.removeLastOpt())
            assertOptEmpty(m.removeLastOpt())
        }

        run {
            val m = listNullStringFull.toMutableList()
            assertOptEquals(optOf(null), m.removeLastOpt())
            assertOptEquals(optOf("sa"), m.removeLastOpt())
            assertOptEquals(optOf(null), m.removeLastOpt())
            assertOptEquals(optOf("ka"), m.removeLastOpt())
            assertOptEquals(optOf(null), m.removeLastOpt())
            assertOptEquals(optOf("a"), m.removeLastOpt())
            assertOptEmpty(m.removeLastOpt())
            assertOptEmpty(m.removeLastOpt())
        }
    }

    @Test
    fun singleOptTest() {
        assertOptEmpty(listNullStringEmpty.singleOpt()) // 0
        assertOptEmpty(listNullStringEmpty.singleOptOrNull()) // 0
        assertOptEmpty(listNullStringEmpty.singleOpt { true }) // 0
        assertOptEmpty(listNullStringEmpty.singleOptOrNull { true }) // 0

        assertOptEquals(optOf("a"), listNullStringSingleNotNull.singleOpt()) // 1
        assertOptEquals(optOf("a"), listNullStringSingleNotNull.singleOptOrNull()) // 1
        assertOptEquals(optOf("a"), listNullStringSingleNotNull.singleOpt { true }) // 1
        assertOptEquals(optOf("a"), listNullStringSingleNotNull.singleOptOrNull { true }) // 1
        assertOptEmpty(listNullStringSingleNotNull.singleOpt { false }) // 0
        assertOptEmpty(listNullStringSingleNotNull.singleOptOrNull { false }) // 0

        assertOptEquals(optOf(null), listNullStringSingleNull.singleOpt()) // 1
        assertOptEquals(optOf(null), listNullStringSingleNull.singleOptOrNull()) // 1
        assertOptEquals(optOf(null), listNullStringSingleNull.singleOpt { true }) // 1
        assertOptEquals(optOf(null), listNullStringSingleNull.singleOptOrNull { true }) // 1
        assertOptEmpty(listNullStringSingleNull.singleOpt { false }) // 0
        assertOptEmpty(listNullStringSingleNull.singleOptOrNull { false }) // 0

        assertOptEmpty(listNullStringFull.singleOpt()) // multiple
        assertNull(listNullStringFull.singleOptOrNull()) // multiple
        assertOptEquals(optOf("ka"), listNullStringFull.singleOpt { it == "ka" }) // 1
        assertOptEquals(optOf("ka"), listNullStringFull.singleOptOrNull { it == "ka" }) // 1
        assertOptEmpty(listNullStringFull.singleOpt { it?.endsWith("a") ?: false }) // multiple
        assertNull(listNullStringFull.singleOptOrNull { it?.endsWith("a") ?: false }) // multiple
        assertOptEmpty(listNullStringFull.singleOpt { false }) // 0
        assertOptEmpty(listNullStringFull.singleOptOrNull { false }) // 0
    }

    @Test
    fun skipThenNextOptTest() {
        assertOptEmpty(listNullStringEmpty.iterator().skipThenNextOpt(-1))
        assertOptEmpty(listNullStringEmpty.iterator().skipThenNextOpt(0))
        assertOptEmpty(listNullStringEmpty.iterator().skipThenNextOpt(1))

        assertOptEmpty(listNullStringFull.iterator().skipThenNextOpt(-1))
        assertOptEquals(optOf("a"), listNullStringFull.iterator().skipThenNextOpt(0))
        assertOptEquals(optOf(null), listNullStringFull.iterator().skipThenNextOpt(1))
        assertOptEquals(optOf("ka"), listNullStringFull.iterator().skipThenNextOpt(2))
        assertOptEquals(optOf(null), listNullStringFull.iterator().skipThenNextOpt(3))
        assertOptEquals(optOf("sa"), listNullStringFull.iterator().skipThenNextOpt(4))
        assertOptEquals(optOf(null), listNullStringFull.iterator().skipThenNextOpt(5))
        assertOptEmpty(listNullStringFull.iterator().skipThenNextOpt(6))
    }
}