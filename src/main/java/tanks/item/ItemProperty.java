package tanks.item;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ItemProperty
{
    String id();
    String name();
    String desc() default "";
}
