package io.github.myungpyo.simplekspsample.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import io.github.myungpyo.simplekspsample.StickyState
import io.github.myungpyo.simplekspsample.support.CodeLoom

/**
 * Symbol processor to generate sticky state bindings.
 */
@KspExperimental
class StickyStateProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    companion object {
        private val STICKY_ANNOTATION_NAME = StickyState::class.java.name
        const val STATE_HOLDER = "stateHolder"
        const val STATE_STORE = "stateStore"
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbolMap = resolver
            .getSymbolsWithAnnotation(STICKY_ANNOTATION_NAME)
            .filterIsInstance<KSPropertyDeclaration>()
            .groupBy {
                resolver.getOwnerJvmClassName(it) ?: run {
                    logger.warn("Failed to find class name of property $it")
                    return emptyList()
                }
            }
        if (symbolMap.isEmpty()) return emptyList()

        return symbolMap.flatMap { (ownerClassName, propertyDeclarations) ->
            val sourceFiles = symbolMap[ownerClassName]?.getOrNull(0)?.containingFile?.toArray() ?: run {
                logger.warn("Failed to find owner file of $ownerClassName. This class will be dependant to all files.")
                resolver.getAllFiles().toList().toTypedArray()
            }
            generateBinding(ownerClassName, sourceFiles, propertyDeclarations)
        }
    }

    private fun generateBinding(
        ownerClassName: String,
        sourceFiles: Array<KSFile>,
        propertyDeclarations: List<KSPropertyDeclaration>
    ): List<KSPropertyDeclaration> {
        val ownerPackageName = ownerClassName.substringBeforeLast(".")
        val ownerClassSimpleName = ownerClassName.substringAfterLast(".")
        val bindingClassName = "${ownerClassSimpleName}StateBinding"

        val saveCodeList = mutableListOf<String>()
        val restoreCodeList = mutableListOf<String>()
        propertyDeclarations.forEach { propertyDeclaration ->
            propertyDeclaration.accept(StickyStateVisitor(saveCodeList, restoreCodeList, logger), Unit)
        }

        val outputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                sources = sourceFiles
            ),
            packageName = ownerPackageName,
            fileName = bindingClassName,
        )

        outputStream.use {
            with(CodeLoom(it)) {
                write("package $ownerPackageName").lineWrap(repeat = 2)
                write("import android.os.Bundle").lineWrap(repeat = 2)
                writeWithOpenBracket("class $bindingClassName").lineWrap()

                lineWrap()

                // Save function
                writeWithOpenBracket("fun save($STATE_HOLDER: $ownerClassSimpleName, $STATE_STORE: Bundle)").lineWrap()
                writeWithOpenBracket("with($STATE_HOLDER)").lineWrap()
                saveCodeList.forEach { saveCode ->
                    write(saveCode).lineWrap()
                }
                closeBracket().lineWrap()
                closeBracket().lineWrap(repeat = 2)

                // Restore function
                writeWithOpenBracket("fun restore($STATE_HOLDER: $ownerClassSimpleName, $STATE_STORE: Bundle?)").lineWrap()
                write("$STATE_STORE ?: return").lineWrap()
                restoreCodeList.forEach { restoreCode ->
                    write(restoreCode).lineWrap()
                }
                closeBracket().lineWrap()

                lineWrap()
                closeBracket().lineWrap()
            }
        }

        return propertyDeclarations.filterNot { it.validate() }.toList()
    }

    private inline fun <reified T: Any> T?.toArray(): Array<T>? {
        this ?: return null
        return arrayOf(this)
    }
}