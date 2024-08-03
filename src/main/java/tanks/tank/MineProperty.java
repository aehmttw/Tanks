package tanks.tank;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MineProperty
{
    String id();
    String name();
    String desc() default "";
}
