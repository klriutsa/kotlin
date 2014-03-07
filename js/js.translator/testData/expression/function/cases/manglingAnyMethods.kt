package foo

public fun equals(a: Any?): Boolean = true
public fun hashCode(): Int = 0
public fun toString(): String = ""

public class PublicClass {
    override fun equals(a: Any?): Boolean = this.identityEquals(a)
    override fun hashCode(): Int = 0
    override fun toString(): String = "PublicClass"
}

internal class InternalClass {
    override fun equals(a: Any?): Boolean = this.identityEquals(a)
    override fun hashCode(): Int = 1
    override fun toString(): String = "InternalClass"

    // overloads
    public fun equals(a: Any?, b: Any?): Boolean = a == b
    public fun hashCode(i: Int): Int = i
    public fun toString(s: String): String = s
}

private class PrivateClass {
    override fun equals(a: Any?): Boolean = this.identityEquals(a)
    override fun hashCode(): Int = 2
    override fun toString(): String = "InternalClass"

    // overloads
    public fun equals(a: Any?, b: Any?): Boolean = a == b
    public fun hashCode(i: Int): Int = i
    public fun toString(s: String): String = s
}

// Helpers

native
fun String.search(regexp: RegExp): Int = noImpl

native
class RegExp(regexp: String, flags: String = "") {
    fun exec(s: String): Array<String>? = noImpl
}

val CALEE_NAME = RegExp("""((?:equals|hashCode|toString)[^(]*)""")

fun <T> Function0<T>.extractNames(): Array<String> {
    val names = CALEE_NAME.exec(this.toString(): String)

    if (names == null || names.size != 2) {
        throw Exception("Cannot extract function name, $names for actual = \"$this\"")
    }

    return names
}

// Testing

var testGroup = ""

fun test(expected: String, f: () -> Unit) {
    val actual = f.extractNames()

    if (expected != actual[1]) {
        throw Exception("Failed on '$testGroup' group: expected = \"$expected\", actual[1] = \"${actual[1]}\"\n actual = $actual")
    }
}

val SIMPLE_EQUALS = "equals"
val SIMPLE_HASH_CODE_0 = "hashCode_0"
val SIMPLE_TO_STRING_0 = "toString_0"
val STABLE_EQUALS = { equals(0) }.extractNames()[1]
val STABLE_HASH_CODE = { hashCode() }.extractNames()[1]
val STABLE_TO_STRING = { toString() }.extractNames()[1]

fun box(): String {
    testGroup = "Public Class"
    test(STABLE_EQUALS) { PublicClass().equals(0) }
    test(STABLE_HASH_CODE) { PublicClass().hashCode() }
    test(STABLE_TO_STRING) { PublicClass().toString() }

    testGroup = "Internal Class"
    test(STABLE_EQUALS) { InternalClass().equals(0) }
    test(STABLE_HASH_CODE) { InternalClass().hashCode() }
    test(STABLE_TO_STRING) { InternalClass().toString() }
    test(SIMPLE_EQUALS) { InternalClass().equals(0, 1) }
    test(SIMPLE_HASH_CODE_0) { InternalClass().hashCode(2) }
    test(SIMPLE_TO_STRING_0) { InternalClass().toString("3") }

    testGroup = "Private Class"
    test(STABLE_EQUALS) { PrivateClass().equals(0) }
    test(STABLE_HASH_CODE) { PrivateClass().hashCode() }
    test(STABLE_TO_STRING) { PrivateClass().toString() }
    test(SIMPLE_EQUALS) { PrivateClass().equals(0, 1) }
    test(SIMPLE_HASH_CODE_0) { PrivateClass().hashCode(2) }
    test(SIMPLE_TO_STRING_0) { PrivateClass().toString("3") }

    return "OK"
}
