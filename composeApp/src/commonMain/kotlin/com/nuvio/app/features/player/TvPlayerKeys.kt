package com.nuvio.app.features.player

/**
 * TV support (v1): remote-control keys the player understands.
 *
 * The platform activity forwards D-pad / media keys here while the player
 * screen is composed (see MainActivity.dispatchKeyEvent on Android). The
 * player runtime registers a handler and only consumes a key when it acted
 * on it (for example, controls hidden); otherwise the key falls through to
 * normal Compose focus navigation and button activation.
 */
enum class TvPlayerKey {
    TogglePlayback,
    SeekBack,
    SeekForward,
    ShowControls,
}

object TvPlayerKeyBridge {
    var handler: ((TvPlayerKey) -> Boolean)? = null
}
