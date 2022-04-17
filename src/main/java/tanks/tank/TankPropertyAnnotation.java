package tanks.tank;

import tanks.Function;

public @interface TankPropertyAnnotation
{
    public enum Category {appearanceGeneral, appearanceModel, appearanceColor, misc, movementGeneral, movementIdle, movementAvoid, movementPathfinding, movementOnSight, mines, firingGeneral, firingBehavior, spawning}

    Category category();
    String name();
    String desc() default "";
}
