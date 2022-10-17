package com.github.andrasferenczi.swingrendering.preview

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class StuffPreviewFileEditorProvider : WeighedFileEditorProvider() {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return true
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return StuffPreviewFileEditor(project, file)
    }

    override fun getEditorTypeId(): String {
        return "stuff-preview-editor"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
    }
}