package com.github.andrasferenczi.swingrendering.util

import javax.swing.JComponent

inline fun JComponent.addToParentIfNotAdded(
    parent: JComponent?,
    add: (parent: JComponent, child: JComponent) -> Unit,
) {
    val container = parent ?: return

    val alreadyExists = container.isAncestorOf(this)
    if (!alreadyExists) {
        add(container, this)
    }
}

inline fun JComponent?.withUiUpdate(action: (JComponent) -> Unit) {
    this
        ?.also(action)
        ?.also {
            it.revalidate()
            it.repaint()
        }
}