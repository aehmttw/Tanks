package tanks.tank;

import basewindow.Model;
import tanks.Drawing;
import tanks.Game;

public class TankModels
{
    public static FullTankModel tank;
    public static FullTankModel checkerboard;
    public static FullTankModel fixed;
    public static FullTankModel cross;
    public static FullTankModel horizontalStripes;
    public static FullTankModel verticalStripes;
    public static FullTankModel diagonalStripes;
    public static FullTankModel arrow;
    public static FullTankModel camo;

    public static class FullTankModel
    {
        public Model base;
        public Model color;
        public Model turretBase;
        public Model turret;

        public FullTankModel(String name)
        {
            base = Drawing.drawing.createModel("/models/" + name + "/base/");
            color = Drawing.drawing.createModel("/models/" + name + "/color/");
            turretBase = Drawing.drawing.createModel("/models/" + name + "/turretbase/");
            turret = Drawing.drawing.createModel("/models/" + name + "/turret/");
            Game.registerTankModel("/models/" + name);
        }
    }

    public static void initialize()
    {
        tank = new FullTankModel("tank");
        checkerboard = new FullTankModel("tankmimic");
        fixed = new FullTankModel("tankfixed");
        cross = new FullTankModel("tankcross");
        verticalStripes = new FullTankModel("tankverticalstripes");
        horizontalStripes = new FullTankModel("tankhorizontalstripes");
        diagonalStripes = new FullTankModel("tankdiagonalstripes");
        arrow = new FullTankModel("tankarrow");
        camo = new FullTankModel("tankcamoflauge");


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
