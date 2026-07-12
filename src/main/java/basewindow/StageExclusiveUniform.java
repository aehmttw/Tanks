package basewindow;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Apply to group uniforms used only in specific stages of a shader group
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StageExclusiveUniform
{
    int[] value();
}
