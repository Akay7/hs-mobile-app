package org.hyperskill.app.android.latex.view.model.block

class MetaBlock(
    baseUrl: String
) : ContentBlock {
    override val header: String = """
        <meta name="viewport" content="width=device-width, user-scalable=no, target-densitydpi=medium-dpi" />
        <base href="$baseUrl">
    """.trimIndent()
}