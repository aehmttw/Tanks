package tanks.bullet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BulletProperty
{
<<<<<<< HEAD
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
=======
    String id();
    String name();
    String desc() default "";
>>>>>>> 6058165 (more changes to net optimization and such)
}
