plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter parameters {
    replacements.string(current.version >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
    }
}

stonecutter active "1.21.10"