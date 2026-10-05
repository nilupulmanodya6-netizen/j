package com.helakuru.promax

/**
 * ⌨️ Keyboard Layouts - Helakuru, Wijesekara, QWERTY, Symbols
 * Each row is a List<Key>
 */
object KeyboardLayout {

    data class Key(
        val label: String,
        val code: Int = 0, // 0 = normal char, negative = special
        val isSpecial: Boolean = false,
        val longPressOptions: List<String> = emptyList()
    )

    // === HELAKURU LAYOUT - Main Sinhala Layout ===
    // Based on real Helakuru arrangement
    val HELAKURU_LAYOUT: List<List<Key>> = listOf(
        // Row 1: Numbers + special
        listOf(
            Key("1", isSpecial = true, longPressOptions = listOf("!", "¹")),
            Key("2", isSpecial = true, longPressOptions = listOf("@", "²")),
            Key("3", isSpecial = true, longPressOptions = listOf("#", "³")),
            Key("4", isSpecial = true),
            Key("5", isSpecial = true),
            Key("6", isSpecial = true),
            Key("7", isSpecial = true),
            Key("8", isSpecial = true),
            Key("9", isSpecial = true),
            Key("0", isSpecial = true)
        ),
        // Row 2
        listOf(
            Key("අ"), Key("ආ"), Key("ඇ"), Key("ඈ"), Key("ඉ"), Key("ඊ"),
            Key("උ"), Key("ඌ"), Key("ඍ"), Key("එ")
        ),
        // Row 3
        listOf(
            Key("ඒ"), Key("ඓ"), Key("ඔ"), Key("ඕ"), Key("ඖ"),
            Key("ක"), Key("ඛ"), Key("ග"), Key("ඝ"), Key("ච")
        ),
        // Row 4
        listOf(
            Key("ඡ"), Key("ජ"), Key("ඣ"), Key("ඤ"), Key("ඦ"),
            Key("ට"), Key("ඨ"), Key("ඩ"), Key("ඪ"), Key("ණ")
        ),
        // Row 5
        listOf(
            Key("ත"), Key("ථ"), Key("ද"), Key("ධ"), Key("න"),
            Key("ප"), Key("ඵ"), Key("බ"), Key("භ"), Key("ම")
        ),
        // Row 6
        listOf(
            Key("ඹ"), Key("ය"), Key("ර"), Key("ල"), Key("ව"),
            Key("ශ"), Key("ෂ"), Key("ස"), Key("හ"), Key("ළ")
        ),
        // Row 7 - Special chars
        listOf(
            Key("ෆ"), Key("ං"), Key("ඃ"), Key("්"), Key("ා"),
            Key("ැ"), Key("ෑ"), Key("ි"), Key("ී"), Key("ු")
        ),
        // Row 8 - More diacritics
        listOf(
            Key("ූ"), Key("ෙ"), Key("ේ"), Key("ො"), Key("ෝ"),
            Key("ෞ"), Key("ෟ"), Key("ෘ"), Key("ෲ"), Key("ෳ")
        )
    )

    // === WIJESEKARA LAYOUT ===
    val WIJESEKARA_LAYOUT: List<List<Key>> = listOf(
        listOf(
            Key("1"), Key("2"), Key("3"), Key("4"), Key("5"),
            Key("6"), Key("7"), Key("8"), Key("9"), Key("0")
        ),
        listOf(
            Key("ු"), Key("ූ"), Key("අ"), Key("ආ"), Key("ඇ"),
            Key("ඈ"), Key("ඉ"), Key("ඊ"), Key("උ"), Key("ඌ")
        ),
        listOf(
            Key("එ"), Key("ඒ"), Key("ඓ"), Key("ඔ"), Key("ඕ"),
            Key("ක"), Key("ග"), Key("ච"), Key("ජ"), Key("ට")
        ),
        listOf(
            Key("ඩ"), Key("ණ"), Key("ත"), Key("ද"), Key("න"),
            Key("ප"), Key("බ"), Key("ම"), Key("ය"), Key("ර")
        ),
        listOf(
            Key("ල"), Key("ව"), Key("ශ"), Key("ෂ"), Key("ස"),
            Key("හ"), Key("ළ"), Key("ෆ"), Key("ං"), Key("ඃ")
        )
    )

    // === ENGLISH QWERTY ===
    val ENGLISH_QWERTY: List<List<Key>> = listOf(
        listOf(
            Key("q"), Key("w"), Key("e"), Key("r"), Key("t"),
            Key("y"), Key("u"), Key("i"), Key("o"), Key("p")
        ),
        listOf(
            Key("a"), Key("s"), Key("d"), Key("f"), Key("g"),
            Key("h"), Key("j"), Key("k"), Key("l")
        ),
        listOf(
            Key("z"), Key("x"), Key("c"), Key("v"), Key("b"),
            Key("n"), Key("m")
        )
    )

    // === SYMBOLS ===
    val SYMBOLS_LAYOUT: List<List<Key>> = listOf(
        listOf(
            Key("!"), Key("@"), Key("#"), Key("$"), Key("%"),
            Key("^"), Key("&"), Key("*"), Key("("), Key(")")
        ),
        listOf(
            Key("-"), Key("_"), Key("="), Key("+"), Key("["),
            Key("]"), Key("{"), Key("}"), Key(";"), Key(":")
        ),
        listOf(
            Key("'"), Key("\\""), Key(","), Key("."), Key("<"),
            Key(">"), Key("/"), Key("?"), Key("!"), Key("~")
        ),
        listOf(
            Key("©"), Key("®"), Key("™"), Key("₹"), Key("$"),
            Key("€"), Key("£"), Key("¥"), Key("¢"), Key("•")
        )
    )

    // === EMOJI (simplified) ===
    val EMOJI_LAYOUT: List<List<Key>> = listOf(
        listOf(
            Key("\uD83D\uDE00"), Key("\uD83D\uDE02"), Key("❤️"), Key("\uD83D\uDD25"), Key("\uD83D\uDC4D"),
            Key("\uD83D\uDE4F"), Key("\uD83D\uDE0D"), Key("\uD83E\uDD70"), Key("\uD83D\uDE0E"), Key("\uD83E\uDD23")
        ),
        listOf(
            Key("\uD83D\uDE0A"), Key("\uD83D\uDE42"), Key("\uD83D\uDE05"), Key("\uD83D\uDE07"), Key("\uD83E\uDD14"),
            Key("\uD83D\uDE2D"), Key("\uD83D\uDE22"), Key("\uD83E\uDD7A"), Key("\uD83D\uDE34"), Key("\uD83E\uDD29")
        ),
        listOf(
            Key("\uD83C\uDDF1\uD83C\uDDF0"), Key("\uD83D\uDE4F"), Key("\uD83D\uDCAA"), Key("❤️"), Key("✨"),
            Key("\uD83C\uDF89"), Key("\uD83D\uDCAF"), Key("\uD83D\uDC4F"), Key("\uD83E\uDD1D"), Key("\uD83D\uDE4C")
        )
    )

    fun getLongPressOptions(label: String): List<String> {
        return when (label) {
            "ක" -> listOf("ඛ", "ග", "ඝ")
            "ත" -> listOf("ථ", "ද", "ධ")
            "ප" -> listOf("ඵ", "බ", "භ")
            "අ" -> listOf("ආ", "ඇ", "ඈ")
            "1" -> listOf("!", "@", "#")
            else -> emptyList()
        }
    }
}
