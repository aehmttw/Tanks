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
        general,
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
        transformationGeneral,
        lastStand,

        transformationOnSight,
        transformationOnHealth,
        transformationOnThreat,
        transformationOnProximity,
        transformationOnAlone,}

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
    String id();
    String name();
    String desc() default "";
    MiscType miscType() default MiscType.none;
}
