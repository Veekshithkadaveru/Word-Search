# Add project specific ProGuard rules here.

# ---------------------------------------------------------------------------
# Stack trace readability (crash reports will show line numbers)
# ---------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# Gson – keep all data classes used for JSON deserialization from puzzles.json
# ---------------------------------------------------------------------------
-keep class app.krafted.wordsearch.data.PuzzleCatalog { *; }
-keep class app.krafted.wordsearch.data.PuzzleCategory { *; }
-keep class app.krafted.wordsearch.data.PuzzleDefinition { *; }

# Gson uses reflection on field names; don't obfuscate them
-keepclassmembers class app.krafted.wordsearch.data.** {
    <fields>;
}

# ---------------------------------------------------------------------------
# Room – entities and DAOs are handled by KSP at compile time; no extra rules
# needed. Keep the entity class itself so its field names survive obfuscation.
# ---------------------------------------------------------------------------
-keep class app.krafted.wordsearch.data.db.PuzzleProgress { *; }