package com.github.andrasferenczi.swingrendering.preview

import com.github.andrasferenczi.swingrendering.util.addToParentIfNotAdded
import com.github.andrasferenczi.swingrendering.util.withUiUpdate
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.AnimatedIcon
import com.intellij.util.ui.TimerUtil
import java.awt.Color
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import javax.swing.*

interface PreviewPanelUi : Disposable {

    val rootPanel: JComponent

    fun showContent()

    fun showLoading(text: String)

    companion object {

        private const val CONTENT_Z_INDEX = 25
        private const val OVERLAY_Z_INDEX = 50

        /**
         * @param parent where the ui will be set up
         * @param content the iframe technically
         */
        fun create(
            project: Project,
            parent: JComponent,
            content: JComponent,
            loadingBackgroundColor: Color = EditorColorsManager.getInstance().globalScheme.defaultBackground,
        ): PreviewPanelUi {
            val parentDisposable = Disposer.newDisposable()
            // this is only a safeguard, will be disposed on detach
            Disposer.register(project, parentDisposable)

            val loadingTextLabel = JLabel(
                "Loading ...",
                AnimatedIcon.Default(),
                SwingConstants.CENTER,
            )

            val loadingPanel = JPanel(GridBagLayout()).also {
                it.add(loadingTextLabel)
                it.border = BorderFactory.createEmptyBorder()
                it.background = loadingBackgroundColor

                it.addMouseListener(object : MouseAdapter() {
                    // meant for consuming events in the panel
                })
            }

            val overlayPaintTimer: Timer = TimerUtil
                .createNamedTimer("repaint_flicker_solution", 7)
                .also {
                    it.isRepeats = true
                    it.addActionListener {
                        loadingPanel.validate()
                        loadingPanel.revalidate()
                        parent.validate()
                        parent.revalidate()
                        parent.repaint()
                    }
                    it.start()
                }

            val mainPanel = JPanel().also {
                it.layout = OverlayLayout(it)

                it.add(content, CONTENT_Z_INDEX, 0)
                it.add(loadingPanel, OVERLAY_Z_INDEX, 0)
            }

            parent.add(mainPanel)

            return object : PreviewPanelUi {

                override fun dispose() {
                    Disposer.dispose(parentDisposable)
                    overlayPaintTimer.stop()
                }

                override val rootPanel: JComponent
                    get() = mainPanel

                override fun showContent() {
                    mainPanel.withUiUpdate {
                        mainPanel.remove(loadingPanel)
                    }
                }

                override fun showLoading(text: String) {
                    mainPanel.withUiUpdate {
                        loadingTextLabel.text = text

                        loadingPanel.addToParentIfNotAdded(mainPanel) { parent, child ->
                            parent.add(
                                child,
                                OVERLAY_Z_INDEX,
                                0
                            )
                        }
                    }
                }
            }
        }

    }

}