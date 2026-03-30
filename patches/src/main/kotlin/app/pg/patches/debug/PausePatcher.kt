package app.pg.patches.dialer

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.longOption

@Suppress("unused")
val StartPausePatcher = bytecodePatch (
    name = "Start Pause Patcher",
    description = "Notify the start of the pause.",
    use = false,
) {}


@Suppress("unused")
val PausePatcher = bytecodePatch (
    name = "Pause Patcher",
    description = "Pause the patcher Xs bat the end of the process",
    use = false,
) {
    val pauseDurationOption by longOption(
        name = "Pause duration",
        description = "The number of seconds tha patcher will wait.",
        default = 120,
        validator = { it == null || it > 0 },
    )

    dependsOn(StartPausePatcher)

    afterDependents {
        Thread.sleep(pauseDurationOption?.times(1000)!!)
    }
}
