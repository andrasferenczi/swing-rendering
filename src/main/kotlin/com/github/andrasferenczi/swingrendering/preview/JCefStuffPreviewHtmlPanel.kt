package com.github.andrasferenczi.swingrendering.preview

import com.intellij.ui.jcef.JCEFHtmlPanel
import javax.swing.JComponent

class JCefStuffPreviewHtmlPanel(
) : JCEFHtmlPanel(
    isOffScreenRendering, null, null
) {

    init {
        val html = createHtml()

        setHtml(html)
    }

    val htmlComponent: JComponent
        get() = super.getComponent()

    companion object {

        const val isOffScreenRendering: Boolean = true

        private fun createHtml(): String {
            // language=HTML
            return """
                <!DOCTYPE html>
                <html lang="en">
                <script>$BALL_JS</script>
                <body style="margin: 0; padding: 0;">
                    <canvas id="myCanvas" height="500" width="100" style="background: #eee"></canvas>
                </body>
                </html>
            """.trimIndent()
        }

        // language=JavaScript
        private const val BALL_JS = """
(function() {
    const action = () => {
        let previousInterval;

        function restart({width, height}) {
            console.log({width, height})

            let canvas = document.getElementById("myCanvas");

            canvas.width = width;
            canvas.height = height;
            canvas.style.width = "" + width + "px";
            canvas.style.height = "" + height + "px";

            let ctx = canvas.getContext("2d");
            let ballRadius = 10;
            let x = width / 2;
            let y = height / 2;
            let dx = 2;
            let dy = -2;

            function drawBall() {
                ctx.beginPath();
                ctx.arc(x, y, ballRadius, 0, Math.PI * 2);
                ctx.fillStyle = "#0095DD";
                ctx.fill();
                ctx.closePath();
            }

            function draw() {
                ctx.clearRect(0, 0, width, height);
                drawBall();

                if (x + dx > width - ballRadius || x + dx < ballRadius) {
                    dx = -dx;
                }
                if (y + dy > height - ballRadius || y + dy < ballRadius) {
                    dy = -dy;
                }

                x += dx;
                y += dy;
            }

            if (previousInterval) {
                clearInterval(previousInterval);
            }
            previousInterval = setInterval(draw, 10);
        }

        addEventListener('resize', (event) => {
            const width = event.target.innerWidth;
            const height = event.target.innerHeight;

            restart({width, height})
        });

        restart({width: window.innerWidth, height: window.innerHeight})
    };
    if (document.readyState === "loading" || document.readyState === "uninitialized") {
        document.addEventListener("DOMContentLoaded", () => action(), { once: true });
    } else {
        action();
    }
})();
        """

    }


}