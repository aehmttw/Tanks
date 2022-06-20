package tanks.tank;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TankProperty
{
    enum Category
    {
        appearanceGeneral,
            appearanceModel,
            appearanceColor,
        misc,
        movementGeneral,
            movementIdle,
            movementAvoid,
            movementPathfinding,
            movementOnSight,
        mines,
        firingGeneral,
            firingBehavior,
            firingPattern,
        spawning,
        transformation,
        lastStand}

    enum MiscType
    {
        none,
        base,
        color,
        turretBase,
        turret,
        emblem
    }

    Category category();
    String name();
    String desc() default "";
    MiscType miscType() default MiscType.none;
}
