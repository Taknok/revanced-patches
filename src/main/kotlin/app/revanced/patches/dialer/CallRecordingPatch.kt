package app.revanced.patches.dialer.app.revanced.patches.dialer.callrecording

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.playservice.VersionCheckPatch
import org.w3c.dom.Element

@Patch(
    name = "Remove voice in call recording",
    description = "Remove the announcement when activating th call recording",
    compatiblePackages = [
        compatiblePackages(
            "com.google.android.dialer",
            [
                "149.0.682953539",
            ]
        )
    ],
)
internal object CallRecordingPatch : ResourcePatch() {
    private const val VOICE_STRINGS = [
        "call_recording_starting_voice",
        "call_recording_ending_voice",
    ]

    override fun execute(context: ResourceContext) {
        VOICE_STRINGS.forEach { s ->
            editStringResource(context, s, "")
        }
    }
}

private fun editStringResource(
    context: ResourceContext,
    stringName: String,
    stringNewValue: String,
) {

    context.xmlEditor[resourceFile].use { editor ->
        val document = editor.file
        
        val callRecordingStartingVoice = document.getElementsByName("stringName").items(0) as Element
        
        callRecordingStartingVoice.textContent = stringNewValue
    }
}