package io.github.myungpyo.simplekspsample.support

import java.io.OutputStream

internal class CodeLoom(private val outputStream: OutputStream) {
    private var indentLevel: Int = 0

    fun write(code: String): CodeLoom {
        outputStream += "\t".repeat(indentLevel)
        outputStream += code
        return this
    }

    fun writeWithOpenBracket(code: String): CodeLoom {
        write(code)
        outputStream += " {"
        indentLevel++
        return this
    }

    fun lineWrap(repeat: Int = 1): CodeLoom {
        outputStream += "\n".repeat(repeat)
        return this
    }

    fun closeBracket(): CodeLoom {
        indentLevel--
        outputStream += "\t".repeat(indentLevel)
        outputStream += "}"
        return this
    }

    private operator fun OutputStream.plusAssign(str: String) {
        this.write(str.toByteArray())
    }
}