/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.mcp.service;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;

/**
 * Suspends Gephi's viz-engine world updater around external write sections.
 *
 * The macOS wedge happens because the renderer's world updater re-acquires the
 * graph read lock near-continuously; pausing it while we hold the write lock
 * removes that pressure entirely (VizEngine exposes public pauseUpdating() /
 * resumeUpdating() for exactly this). Access goes through reflection on the
 * concrete VizController's getEngine() so this class compiles against
 * visualization-api only and degrades to a no-op wherever there is no engine
 * (Gephi Toolkit, headless, or older Gephi versions).
 *
 * Pause/resume is reference-counted: NanoHTTPD serves requests on multiple
 * threads, so concurrent write sections must not resume the renderer while a
 * sibling section still holds it paused.
 */
final class RenderPause {

    private static final Logger LOGGER = Logger.getLogger(RenderPause.class.getName());
    private static final Object GATE = new Object();
    private static int depth = 0;
    private static Object pausedEngine = null;

    private RenderPause() {
    }

    static void pause() {
        synchronized (GATE) {
            depth++;
            if (depth > 1) return;               // already paused by a sibling section
            Object engine = engine();
            if (engine == null) return;           // headless / toolkit / no view: no-op
            try {
                engine.getClass().getMethod("pauseUpdating").invoke(engine);
                pausedEngine = engine;
            } catch (Throwable t) {
                LOGGER.log(Level.FINE, "Renderer pause unavailable", t);
                pausedEngine = null;
            }
        }
    }

    static void resume() {
        synchronized (GATE) {
            if (depth == 0) return;               // defensive: unmatched resume
            depth--;
            if (depth > 0 || pausedEngine == null) return;
            try {
                pausedEngine.getClass().getMethod("resumeUpdating").invoke(pausedEngine);
            } catch (Throwable t) {
                LOGGER.log(Level.FINE, "Renderer resume failed", t);
            } finally {
                pausedEngine = null;
            }
        }
    }

    /** The live VizEngine instance, or null when no visualization is available. */
    private static Object engine() {
        try {
            VisualizationController controller =
                Lookup.getDefault().lookup(VisualizationController.class);
            if (controller == null) return null;
            Method getEngine = controller.getClass().getMethod("getEngine");
            Object result = getEngine.invoke(controller);
            if (result instanceof Optional) {
                return ((Optional<?>) result).orElse(null);
            }
            return result;
        } catch (Throwable t) {
            LOGGER.log(Level.FINE, "Viz engine not reachable", t);
            return null;
        }
    }
}
