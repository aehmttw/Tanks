package tanks.tank;

import basewindow.Model;
import tanks.Drawing;
import tanks.Game;

import java.util.HashMap;

public class TankModels
{
    public static FullTankModel tank, checkerboard, fixed, cross, horizontalStripes, verticalStripes, diagonalStripes, arrow, camo, flames;
    public static HashMap<String, FullTankModel> fullTankModels = new HashMap<>();

    public static class FullTankModel
    {
        public Model base;
        public Model color;
        public Model turretBase;
        public Model turret;
        public String texture;

        public FullTankModel(String name)
        {
            base = Drawing.drawing.createModel("/models/" + name + "/base/");
            color = Drawing.drawing.createModel("/models/" + name + "/color/");
            turretBase = Drawing.drawing.createModel("/models/" + name + "/turretbase/");
            turret = Drawing.drawing.createModel("/models/" + name + "/turret/");
            texture = "/models/" + name + "/texture.png";
            Game.registerTankModel("/models/" + name);
            fullTankModels.put(name, this);
            fullTankModels.put("/models/" + name + "/base/", this);
            fullTankModels.put("/models/" + name + "/color/", this);
            fullTankModels.put("/models/" + name + "/turretbase/", this);
            fullTankModels.put("/models/" + name + "/turret/", this);
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
        flames = new FullTankModel("tankflames");
        Tank.health_model = Drawing.drawing.createModel("/models/tankhealth/");

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
        Game.registerTankEmblem("angry.png");
        Game.registerTankEmblem("snowflake.png");
        Game.registerTankEmblem("curve.png");
        Game.registerTankEmblem("star.png");
        Game.registerTankEmblem("pinwheel.png");
    }
}
