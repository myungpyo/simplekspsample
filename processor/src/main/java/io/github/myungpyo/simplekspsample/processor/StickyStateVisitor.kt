package io.github.myungpyo.simplekspsample.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import io.github.myungpyo.simplekspsample.processor.StickyStateProcessor.Companion.STATE_HOLDER
import io.github.myungpyo.simplekspsample.processor.StickyStateProcessor.Companion.STATE_STORE

class StickyStateVisitor(
    private val saveCodeList: MutableList<String>,
    private val restoreCodeList: MutableList<String>,
    private val logger: KSPLogger,
): KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        super.visitFunctionDeclaration(function, data)
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        val propertyName = property.simpleName.asString()
        val resolvedType = property.type.resolve()
        val propertyType = resolvedType.declaration.simpleName.asString()
        logger.warn("1 : ${property.javaClass.kotlin.qualifiedName}")
        logger.warn("2 : ${resolvedType.declaration.javaClass.kotlin.qualifiedName}")

        // Generate put code
        var saveCode = "$STATE_STORE.put$propertyType(\"${makePropertyStoreKey(propertyName)}\", $STATE_HOLDER.$propertyName)"
        if (resolvedType.nullability == Nullability.NULLABLE) {
            saveCode = "$propertyName?.let{ $saveCode }"
        }
        saveCodeList.add(saveCode)

        // Generate restore code
        var restoreCode = "$STATE_HOLDER.$propertyName = $STATE_STORE.get$propertyType(\"${makePropertyStoreKey(propertyName)}\")"
        if (resolvedType.nullability != Nullability.NULLABLE && propertyType == "String") {
           restoreCode = "$restoreCode ?: $STATE_HOLDER.$propertyName"
        }
        restoreCodeList.add(restoreCode)
    }

    private fun makePropertyStoreKey(propertyName: String): String {
        return "StickyState_$propertyName"
    }
}