package tanks.tank;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TankProperty
{
    enum Category
    {
        appearanceGeneral,
            appearanceEmblem,
            appearanceTurretBase,
            appearanceTurretBarrel,
            appearanceBody,
            appearanceTreads,
            appearanceGlow,
            appearanceTracks,
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
            transformationOnSight,
            transformationOnHealth,
            transformationOnThreat,
            transformationOnProximity,
            transformationOnAlone,
        lastStand,
    }

    enum MiscType
    {
        none,
        baseModel,
        colorModel,
        turretBaseModel,
        turretModel,
        emblem,
        color
    }

    Category category();
    String id();
    String name();
    String desc() default "";
    MiscType miscType() default MiscType.none;
}
