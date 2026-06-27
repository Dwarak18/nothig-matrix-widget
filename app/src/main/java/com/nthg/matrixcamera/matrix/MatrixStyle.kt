package com.nthg.matrixcamera.matrix

/**
 * The six visual styles for the matrix renderer.
 * Each style transforms the processed grayscale bitmap
 * into a distinct aesthetic rendering.
 */
enum class MatrixStyle(val displayName: String, val shortName: String) {
    /** White circular dots on black — the signature Nothing Matrix look */
    NOTHING_MATRIX(displayName = "Nothing Matrix", shortName = "NTHG"),

    /** Circular LEDs with soft glow — warm LED panel aesthetic */
    LED_MATRIX(displayName = "LED Matrix", shortName = "LED"),

    /** ASCII characters mapped to brightness — retro terminal vibe */
    ASCII_TERMINAL(displayName = "ASCII Terminal", shortName = "ASCII"),

    /** Dot glyph pattern inspired by Nothing Phone glyph interface */
    DOT_GLYPH(displayName = "Dot Glyph", shortName = "GLPH"),

    /** Segmented LCD pixel aesthetic — vintage display feel */
    RETRO_LCD(displayName = "Retro LCD", shortName = "LCD"),

    /** 4-color Game Boy green palette pixel art */
    PIXEL_GAMEBOY(displayName = "Pixel Game Boy", shortName = "GBY");

    companion object {
        fun fromIndex(index: Int): MatrixStyle = entries[index.coerceIn(0, entries.size - 1)]
    }
}
