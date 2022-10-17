package com.github.andrasferenczi.swingrendering.preview.split

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

abstract class SplitTextEditorProvider(
    private val firstProvider: FileEditorProvider,
    private val secondProvider: FileEditorProvider,
) : AsyncFileEditorProvider, DumbAware {

    private val editorTypeId = "copied-split-provider[${firstProvider.editorTypeId};${secondProvider.editorTypeId}]"


    override fun accept(project: Project, file: VirtualFile): Boolean {
        return firstProvider.accept(project, file) && secondProvider.accept(project, file)
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return createEditorAsync(project, file).build()
    }

    override fun getEditorTypeId(): String {
        return this.editorTypeId
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }

    override fun createEditorAsync(project: Project, file: VirtualFile): AsyncFileEditorProvider.Builder {
        val firstBuilder = firstProvider.getBuilder(project, file)
        val secondBuilder = secondProvider.getBuilder(project, file)

        return builder {
            createSplitEditor(firstBuilder.build(), secondBuilder.build())
        }
    }

    protected abstract fun createSplitEditor(
        firstEditor: FileEditor,
        secondEditor: FileEditor,
    ): FileEditor

    companion object {

        private fun builder(action: () -> FileEditor): AsyncFileEditorProvider.Builder {
            return object : AsyncFileEditorProvider.Builder() {
                override fun build(): FileEditor {
                    return action()
                }
            }
        }

        private fun FileEditorProvider.getBuilder(
            project: Project,
            file: VirtualFile,
        ): AsyncFileEditorProvider.Builder {
            val provider = this
            return if (provider is AsyncFileEditorProvider) {
                provider.createEditorAsync(project, file)
            } else {
                builder {
                    provider.createEditor(project, file)
                }
            }
        }

    }
}