package app.pg.patches.dialer

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.patch.ResourcePatchContext
import org.w3c.dom.Element
import java.io.FileNotFoundException
import java.nio.file.Files
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.relativeTo


@Suppress("unused")
val callRecordingVoiceRemoverPatch = resourcePatch (
    name = "Call recording announcements remover",
    description = "Remove the announcements when starting or stopping a call recording",
) {
    compatibleWith("com.google.android.dialer"("149.0.682953539-pixel2024"))

    val VOICE_STRINGS = arrayOf(
        "call_recording_starting_voice",
        "call_recording_ending_voice",
    )

    execute { context ->
        VOICE_STRINGS.forEach { s ->
            removeStringAllResources(context, s)
        }
    }

}

private fun removeStringResources(
    context: ResourcePatchContext,
    resource: String,
    stringName: String
) {
    try {
        context.document[resource].use { document ->
            val nodeList = document.getElementsByTagName("string")
            val stringNode: Element? = (0 until nodeList.length)
                .asSequence()
                .mapNotNull { nodeList.item(it) as? Element }
                .firstOrNull { it.getAttribute("name") == stringName }

            stringNode?.textContent = ""
        }
    } catch (_: FileNotFoundException) {
        // Ignoring missing file
    }
}

private fun removeStringAllResources(context: ResourcePatchContext, stringName: String) {
    // Remove from res/values/strings.xml
    removeStringResources(context, "res/values/strings.xml", stringName)

    // Process res/values-*/strings.xml directories
    val apkFiles = context["."].toPath()
    val valuesDirs = Files.walk(apkFiles.resolve("res"), 1)
        .filter { it.isDirectory() && it.name.startsWith("values-") }
        .filter { Files.exists(it.resolve("strings.xml")) }

    valuesDirs.forEach { dir ->
        val relativePath = dir.relativeTo(apkFiles).toString()
        removeStringResources(context, "$relativePath/strings.xml", stringName)
    }
}
