package tanks.tankson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Property
{
    String id();
    String name() default "";
    String desc() default "";
    String category() default "";
    MiscType miscType() default MiscType.none;
    boolean nullable() default false;

    enum MiscType
    {
        none,
        baseModel,
        colorModel,
        turretBaseModel,
        turretModel,
        emblem,
        color,
        description,
        music,
        spawnedTanks
    }
}
