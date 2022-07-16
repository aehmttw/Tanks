package tanks.tank;

import basewindow.Model;
import tanks.Drawing;
import tanks.Game;

public class TankModels
{
    public static Model base_model;
    public static Model color_model;
    public static Model turret_model;
    public static Model turret_base_model;

    public static Model mimic_base_model;
    public static Model mimic_color_model;
    public static Model mimic_turret_model;
    public static Model mimic_turret_base_model;

    public static Model fixed_color_model;

    public static Model horizontalstripes_color_model;

    public static Model cross_turret_model;

    public static Model diagonalstripes_base_model;

    public static Model arrow_color_model;

    public static Model camo_base_model;
    public static Model camo_color_model;
    public static Model camo_turret_model;
    public static Model camo_turret_base_model;

    public static void initialize()
    {
        base_model = Drawing.drawing.createModel("/models/tank/base/");
        color_model = Drawing.drawing.createModel("/models/tank/color/");
        turret_base_model = Drawing.drawing.createModel("/models/tank/turretbase/");
        turret_model = Drawing.drawing.createModel("/models/tank/turret/");
        Game.registerTankModel("/models/tank");

        mimic_base_model = Drawing.drawing.createModel("/models/tankmimic/base/");
        mimic_color_model = Drawing.drawing.createModel("/models/tankmimic/color/");
        mimic_turret_model = Drawing.drawing.createModel("/models/tankmimic/turret/");
        mimic_turret_base_model = Drawing.drawing.createModel("/models/tankmimic/turretbase/");
        Game.registerTankModel("/models/tankmimic");

        fixed_color_model = Drawing.drawing.createModel("/models/tankfixed/color/");
        Game.registerTankModel("/models/tankfixed");

        cross_turret_model = Drawing.drawing.createModel("/models/tankcross/turret/");
        Game.registerTankModel("/models/tankcross");

        Game.registerTankModel("/models/tankverticalstripes");

        horizontalstripes_color_model = Drawing.drawing.createModel("/models/tankhorizontalstripes/color/");
        Game.registerTankModel("/models/tankhorizontalstripes");

        Game.registerTankModel("/models/tankdiagonalstripes");
        diagonalstripes_base_model = Drawing.drawing.createModel("/models/tankdiagonalstripes/base/");

        Game.registerTankModel("/models/tankarrow");
        arrow_color_model = Drawing.drawing.createModel("/models/tankarrow/color/");

        Game.registerTankModel("/models/tankcamoflauge");
        camo_base_model = Drawing.drawing.createModel("/models/tankcamoflauge/base/");
        camo_color_model = Drawing.drawing.createModel("/models/tankcamoflauge/color/");
        camo_turret_model = Drawing.drawing.createModel("/models/tankcamoflauge/turret/");
        camo_turret_base_model = Drawing.drawing.createModel("/models/tankcamoflauge/turretbase/");


        Game.registerTankEmblem("medic.png");
        Game.registerTankEmblem("player_spawn.png");
        Game.registerTankEmblem("bang.png");
        Game.registerTankEmblem("laser.png");
        Game.registerTankEmblem("x.png");
        Game.registerTankEmblem("circle.png");
        Game.registerTankEmblem("circle_outline.png");
        Game.registerTankEmblem("circle_double.png");
        Game.registerTankEmblem("electric.png");
        Game.registerTankEmblem("squares.png");
        Game.registerTankEmblem("square.png");
    }
}
