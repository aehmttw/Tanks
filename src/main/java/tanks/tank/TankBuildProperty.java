package tanks.tank;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TankBuildProperty
{
    String category() default "default";
}
