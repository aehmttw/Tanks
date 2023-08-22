package tanks.rendering;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apply onto a shader group for terrain rendering to specify what draw layer (from 0 to 9) renderers using that shader should draw on.
 * If this annotation is not included, draw layer defaults to 5. Layers are drawn 0 to 9.
 * Transparent things should generally draw at larger numbers.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RendererDrawLayer
{
    int value();
}
