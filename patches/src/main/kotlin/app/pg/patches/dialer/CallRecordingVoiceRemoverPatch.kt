package app.pg.patches.dialer

import app.revanced.patcher.patch.ResourcePatchContext
import app.revanced.patcher.patch.resourcePatch
import org.w3c.dom.Element
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.logging.Logger
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.relativeTo


@Suppress("unused")
val callRecordingVoiceRemoverPatch = resourcePatch (
    name = "Call recording announcements remover",
    description = "Remove the announcements when starting or stopping a call recording",
) {
    compatibleWith("com.google.android.dialer")

    val VOICE_STRINGS = arrayOf(
        "call_recording_starting_voice",
        "call_recording_ending_voice",
    )

    execute {
        val context = this;
        VOICE_STRINGS.forEach { s ->
            editStringAllResources(context, s, " ")
        }
        editStringAllResources(context, "search_bar_hint", ".", Operation.APPEND)
        Logger.getLogger(this::class.java.name).info("Adding '.' to search hint to visually detect patched app")

        Thread.sleep(60000)
    }

}

private enum class Operation { SUBSTITUTE, APPEND }

private fun editStringResources(
    context: ResourcePatchContext,
    resource: String,
    stringName: String,
    stringValue: String,
    operation: Operation = Operation.SUBSTITUTE,
) {
    try {
        context.document(resource).use { document ->
            val nodeList = document.getElementsByTagName("string")
            val stringNode: Element? = (0 until nodeList.length)
                .asSequence()
                .mapNotNull { nodeList.item(it) as? Element }
                .firstOrNull { it.getAttribute("name") == stringName }

            when (operation) {
                Operation.SUBSTITUTE -> stringNode?.textContent = stringValue
                Operation.APPEND -> stringNode?.textContent = stringNode?.textContent.plus(stringValue)
            }
        }
    } catch (_: FileNotFoundException) {
        // Ignoring missing file
    }
}

private fun editStringAllResources(
    context: ResourcePatchContext,
    stringName: String,
    stringValue: String,
    operation: Operation = Operation.SUBSTITUTE
) {
    // Remove from res/values/strings.xml
    editStringResources(context, "res/values/strings.xml", stringName, stringValue, operation)

    // Process res/values-*/strings.xml directories
    val apkFiles = context["."].toPath()
    val valuesDirs = Files.walk(apkFiles.resolve("res"), 1)
        .filter { it.isDirectory() && it.name.startsWith("values-") }
        .filter { Files.exists(it.resolve("strings.xml")) }

    valuesDirs.forEach { dir ->
        val relativePath = dir.relativeTo(apkFiles).toString()
        editStringResources(context, "$relativePath/strings.xml", stringName, stringValue, operation)
    }
}
