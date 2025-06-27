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
    double minValue() default Double.NEGATIVE_INFINITY;
    double maxValue() default Double.POSITIVE_INFINITY;

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
        complexString,
        music,
        spawnedTanks,
        bulletSound,
        itemIcon,
        name,
        defaultBuildForbidden,
        trails,
        alphaless
    }
}
