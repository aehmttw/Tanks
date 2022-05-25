package tanks.tank;

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

    Category category();
    String name();
    String desc() default "";
}
