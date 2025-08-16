package tanks.tank;

import basewindow.Model;
import tanks.Drawing;
import tanks.Game;
import tanks.registry.RegistryModelTank;
import tanks.tankson.Serializable;
import tanks.tankson.TanksONable;

import java.util.ArrayList;

public class TankModels
{
    public static FullTankModel plainTankModel;
    public static FullTankModel skinnedTankModel;

    public static TankSkin tank;
    public static TankSkin checkerboard;
    public static TankSkin fixed;
    public static TankSkin cross;
    public static TankSkin horizontalStripes;
    public static TankSkin verticalStripes;
    public static TankSkin diagonalStripes;
    public static TankSkin arrow;
    public static TankSkin camo;
    public static TankSkin flames;

    public static class FullTankModel
    {
        public Model base;
        public Model color;
        public Model turretBase;
        public Model turret;

        public FullTankModel(String name)
        {
            base = Drawing.drawing.getModel("/models/" + name + "/base/");
            color = Drawing.drawing.getModel("/models/" + name + "/color/");
            turretBase = Drawing.drawing.getModel("/models/" + name + "/turretbase/");
            turret = Drawing.drawing.getModel("/models/" + name + "/turret/");
            Game.registerTankModel("/models/" + name);
        }
    }

    public static class TankSkin implements Serializable
    {
        public String name;

        public String base;
        public String color;
        public String turretBase;
        public String turret;

        public TankSkin(String name)
        {
            this.name = name;
            base = "/models/skins/" + name + "/texture.png";
            color = "/models/skins/" + name + "/texture.png";
            turretBase = "/models/skins/" + name + "/texture.png";
            turret = "/models/skins/" + name + "/texture.png";
            Game.registerTankSkin(this);
        }

        public TankSkin(String name, boolean full)
        {
            this.name = name;

            if (full)
            {
                base = "/models/skins/" + name + "/base.png";
                color = "/models/skins/" + name + "/color.png";
                turretBase = "/models/skins/" + name + "/turretbase.png";
            }
            else
            {
                base = "/models/skins/" + name + "/texture.png";
                color = "/models/skins/" + name + "/texture.png";
                turretBase = "/models/skins/" + name + "/texture.png";
            }
            turret = "/models/skins/" + name + "/turret.png";
            Game.registerTankSkin(this);
        }

        @Override
        public String serialize()
        {
            return this.name;
        }

        @Override
        public Serializable deserialize(String s)
        {
            return Game.registryModelTank.tankSkins.get(s);
        }
    }

    public static void registerTankEmblems()
    {
        ArrayList<String> emblems = Game.game.fileManager.getInternalFileContents("/images/emblems/emblems.txt");

        for (String s: emblems)
        {
            Game.registryModelTank.tankEmblems.add(new RegistryModelTank.TankModelEntry("emblems/" + s + ".png"));
        }
    }

    public static void registerTankSkins()
    {
        ArrayList<String> skins = Game.game.fileManager.getInternalFileContents("/models/skins/skins.txt");

        for (String s: skins)
        {
            String[] sections = s.split(",");
            if (sections.length == 1)
                new TankSkin(s);
            else
                new TankSkin(sections[0], Boolean.parseBoolean(sections[1]));
        }
    }

    public static void initialize()
    {
        plainTankModel = new FullTankModel("tank");
        skinnedTankModel = new FullTankModel("tankskinned");

        registerTankSkins();

        tank = Game.registryModelTank.tankSkins.get("tank");
        checkerboard = Game.registryModelTank.tankSkins.get("tank_checkerboard");
        fixed = Game.registryModelTank.tankSkins.get("tank_fixed");
        cross = Game.registryModelTank.tankSkins.get("tank_cross");
        verticalStripes = Game.registryModelTank.tankSkins.get("tank_vertical_stripes");
        horizontalStripes = Game.registryModelTank.tankSkins.get("tank_horizontal_stripes");
        diagonalStripes = Game.registryModelTank.tankSkins.get("tank_diagonal_stripes");
        arrow = Game.registryModelTank.tankSkins.get("tank_arrow");
        camo = Game.registryModelTank.tankSkins.get("tank_camoflauge");
        flames = Game.registryModelTank.tankSkins.get("tank_flames");

        Tank.health_model = Drawing.drawing.getModel("/models/tankhealth/");

        registerTankEmblems();

    }
}
