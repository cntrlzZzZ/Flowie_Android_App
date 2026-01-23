package fr.eurecom.flowie.ui.screens

data class FaqItem(
    val question: String,
    val answer: String
)

object SettingsContent {

    // --- ABOUT ---
    val aboutTitle = "About Flowie"
    val aboutBody = """
Flowie helps you find drinking water spots and useful amenities nearby.

We’re starting with Vienna, and expanding city by city.
The idea is simple: make it easy to stay hydrated while you’re out.

If you want to help, you can contribute new spots, update existing ones, and add photos when possible.
""".trimIndent()

    // --- FAQ ---
    val faqTitle = "FAQ"
    val faqItems: List<FaqItem> = listOf(
        FaqItem(
            "1) Why can’t I see any fountains on the map?",
            "Try tapping Refresh spots, zooming out slightly, and checking your filters (Dog bowl / Wheelchair can hide most pins). Also make sure you’re online."
        ),
        FaqItem(
            "2) My location is wrong — how do I fix it?",
            "Make sure Location is enabled and Flowie has permission. On an emulator, set a mock location (e.g., Vienna) in the emulator controls."
        ),
        FaqItem(
            "3) What do “Verified” and “Community” mean?",
            "Verified spots come from trusted sources or have been checked. Community spots are added by users."
        ),
        FaqItem(
            "4) What does “Active / Inactive” mean?",
            "Active means it’s expected to work recently. Inactive means it’s been reported as not reliable (broken, blocked, seasonal, etc.)."
        ),
        FaqItem(
            "5) How do I save a spot?",
            "Open a spot and tap the bookmark icon. Your saved spots show up in the Saved tab."
        ),
        FaqItem(
            "6) Are saved spots synced across devices?",
            "Not right now — saved spots are stored locally on your device."
        ),
        FaqItem(
            "7) How do I report a broken fountain or wrong info?",
            "Open the spot, then tap “Report an issue” in the spot details panel that slides up after tapping a pin."
        ),
        FaqItem(
            "8) Why don’t some spots have a photo?",
            "Some spots just don’t have a photo yet. We’re focusing on coverage first, and photos will be added gradually over time."
        ),
        FaqItem(
            "9) Can I use Flowie offline?",
            "The map and spot loading need internet. Your Saved list can still show, but you won’t fetch new spots offline."
        ),
        FaqItem(
            "10) Does Flowie work outside Vienna?",
            "Only where we have data. Vienna is the main focus for now, but more places will be added."
        )
    )

    // --- PRIVACY & SAFETY ---
    val privacyTitle = "Privacy & Safety"

    val privacyBody = """
Privacy
• Location (optional): used to centre the map and show nearby spots.
• Saved spots: stored locally on your device (spot IDs).
• Contributions (if you submit): the info you enter (tags, updates, optional photo).

What we don’t do
• We don’t sell your data.
• We don’t track you in the background.

Photos
• Only upload photos you’re comfortable sharing publicly.

Data storage
• Spots are fetched from our Supabase database.
• Saved spots are stored on your phone.
""".trimIndent()

    val safetyBody = """
Safety tips
• Water sources can change — if a spot looks dirty or unsafe, don’t use it.
• Respect private property and don’t trespass.

Reporting
• If something is wrong, please report it via the buttons on the spot.
  You’ll find “Report an issue” inside the spot details panel after tapping a pin.

Emergency note
• Flowie isn’t an emergency service. If you’re in danger, contact local emergency numbers.
""".trimIndent()
}
