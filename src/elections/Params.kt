package elections

import java.util.prefs.Preferences

private fun prefNode(): Preferences = Preferences.userRoot().node("ContributorsUI")

data class Params(var getNumberOfNodes:Int,val variant: Variant)

fun loadStoredParams(): Params {
    return prefNode().run {
        Params(
            getInt("getNumberOfNodes",10),
            Variant.valueOf(get("variant", Variant.BLOCKING.name))
        )
    }
}
