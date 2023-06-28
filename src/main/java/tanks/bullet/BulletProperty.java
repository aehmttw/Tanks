package tanks.bullet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BulletProperty
{
    enum Category
    {
        appearance,
        firing,
        travel,
        impact
    }

    String id();
    String name();
    String desc() default "";
    Category category();
}
