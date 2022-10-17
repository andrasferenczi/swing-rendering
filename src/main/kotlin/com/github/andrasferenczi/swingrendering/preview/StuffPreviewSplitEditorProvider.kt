package com.github.andrasferenczi.swingrendering.preview

import com.github.andrasferenczi.swingrendering.preview.split.SplitTextEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import com.intellij.openapi.util.Key

class StuffPreviewSplitEditorProvider : SplitTextEditorProvider(
    PsiAwareTextEditorProvider(),
    StuffPreviewFileEditorProvider()
) {

    override fun createSplitEditor(firstEditor: FileEditor, secondEditor: FileEditor): FileEditor {
        require(firstEditor is TextEditor) { "Main editor should be TextEditor" }
        require(secondEditor is StuffPreviewFileEditor) { "Secondary editor should be StuffPreviewFileEditor" }

        return TheEditor(firstEditor, secondEditor)
    }

    private class TheEditor(
        editor: TextEditor,
        preview: StuffPreviewFileEditor,
    ) : TextEditorWithPreview(
        editor,
        preview,
        "Stuff Preview",
        Layout.SHOW_EDITOR_AND_PREVIEW,
    ) {
        init {
            editor.putUserData(PARENT_SPLIT_EDITOR_KEY, this)
            preview.putUserData(PARENT_SPLIT_EDITOR_KEY, this)
        }

        companion object {

            val PARENT_SPLIT_EDITOR_KEY = Key.create<TheEditor>("parentSplit")

        }

    }

}