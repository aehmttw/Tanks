package tanks.tankson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MetadataProperty
{
    String id();
    String name() default "";
    String image() default "";
    String keybind() default "";
    String selector();
}
