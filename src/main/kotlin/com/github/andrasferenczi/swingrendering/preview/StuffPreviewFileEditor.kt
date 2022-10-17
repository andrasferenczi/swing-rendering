package com.github.andrasferenczi.swingrendering.preview

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.beans.PropertyChangeListener
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JPanel

class StuffPreviewFileEditor(
    private val myProject: Project,
    private val myFile: VirtualFile,
) : UserDataHolderBase(), FileEditor {

    @Volatile
    private var previewPanel: PreviewPanelUi? = null

    private var htmlPanel: JCefStuffPreviewHtmlPanel? = null

    private val mySwingAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)

    private val rootPanelWrapper = JPanel(BorderLayout()).also {
        it.addComponentListener(AttachPanelOnVisibilityChangeListener())
    }

    init {
        runLoadingSwitchTest()
    }

    /**
     * This makes sure that loading is appearing and hiding every 5 seconds
     */
    private fun runLoadingSwitchTest() {
        var index = 0

        val isRunning = AtomicBoolean(true)

        val thread = Thread {

            while (isRunning.get()) {
                Thread.sleep(5_000)

                val loading = index % 2 == 0
                index++

                mySwingAlarm.addRequest({
                    println("Switching [id: ${myFile.path} ($index)] to [loading: $loading]")

                    if (loading) {
                        previewPanel?.showLoading("Loading $index")
                    } else {
                        previewPanel?.showContent()
                    }
                }, 200, ModalityState.stateForComponent(component))
            }
        }

        Disposer.register(this) { isRunning.set(false) }

        thread.start()
    }


    private inner class AttachPanelOnVisibilityChangeListener : ComponentAdapter() {
        override fun componentShown(e: ComponentEvent?) {
            mySwingAlarm.addImmediateRequest {
                if (previewPanel == null) {
                    attachHtmlPanel()
                }
            }
        }

        override fun componentHidden(e: ComponentEvent?) {
            mySwingAlarm.addImmediateRequest {
                detachHtmlPanel()
            }
        }
    }

    private fun Alarm.addImmediateRequest(request: Runnable) {
        if (this.isDisposed) {
            return
        }

        this.addRequest(
            request,
            0,
            ModalityState.stateForComponent(component),
        )
    }

    private fun attachHtmlPanel() {
        val newHtmlPanel = JCefStuffPreviewHtmlPanel()
        val newUi = PreviewPanelUi.create(
            project = myProject,
            parent = rootPanelWrapper,
            content = newHtmlPanel.htmlComponent,
        )

        previewPanel = newUi
        htmlPanel = newHtmlPanel

        if (rootPanelWrapper.isShowing) {
            rootPanelWrapper.validate()
        }

        rootPanelWrapper.repaint()
        putUserData(PREVIEW_BROWSER, newHtmlPanel)
    }

    private fun detachHtmlPanel() {
        val previewPanel = previewPanel ?: return
        rootPanelWrapper.remove(previewPanel.rootPanel)
        disposeLocal()
        putUserData(PREVIEW_BROWSER, null)
    }

    private fun disposeLocal() {
        listOfNotNull(previewPanel, htmlPanel).forEach { Disposer.dispose(it) }
    }

    override fun dispose() {
        disposeLocal()
    }

    override fun getComponent(): JComponent {
        return rootPanelWrapper
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return previewPanel?.rootPanel
    }

    override fun getName(): String {
        return "Stuff Preview"
    }

    override fun setState(state: FileEditorState) {
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }


    companion object {
        private val PREVIEW_BROWSER = Key.create<JCefStuffPreviewHtmlPanel>("PREVIEW_BROWSER")
    }

}