package tanks.registry;

import basewindow.IModel;
import tanks.Drawing;
import tanks.tank.TankModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RegistryModelTank
{
    public ArrayList<TankModelEntry> tankBaseModels = new ArrayList<>();
    public ArrayList<TankModelEntry> tankColorModels = new ArrayList<>();
    public ArrayList<TankModelEntry> turretBaseModels = new ArrayList<>();
    public ArrayList<TankModelEntry> turretModels = new ArrayList<>();

    public LinkedHashMap<String, TankModels.TankSkin> tankSkins = new LinkedHashMap<>();

    public ArrayList<TankModelEntry> tankEmblems = new ArrayList<>();

    public RegistryModelTank()
    {
        this.tankEmblems.add(new TankModelEntry(null));
    }

    public static class TankModelEntry
    {
        public String dir;

        public TankModelEntry(String dir)
        {
            this.dir = dir;
        }
    }

    public void registerFullModel(String dir)
    {
        this.tankBaseModels.add(new TankModelEntry(dir + "/base/"));
        this.tankColorModels.add(new TankModelEntry(dir + "/color/"));
        this.turretBaseModels.add(new TankModelEntry(dir + "/turretbase/"));
        this.turretModels.add(new TankModelEntry(dir + "/turret/"));
    }

    public void registerSkin(TankModels.TankSkin skin)
    {
        this.tankSkins.put(skin.name, skin);
    }

    public static IModel[] toModelArray(ArrayList<TankModelEntry> entries)
    {
        IModel[] models = new IModel[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            models[i] = Drawing.drawing.getModel(entries.get(i).dir);
        }

        return models;
    }

    public static String[] toStringArray(ArrayList<TankModelEntry> entries)
    {
        String[] models = new String[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            models[i] = entries.get(i).dir;
        }

        return models;
    }

    public ArrayList<TankModels.TankSkin> getBaseSkins()
    {
        ArrayList<TankModels.TankSkin> skins = new ArrayList<>();
        for (TankModels.TankSkin tankSkin : tankSkins.values())
        {
            if (tankSkin.base != null)
                skins.add(tankSkin);
        }

        return skins;
    }

    public ArrayList<TankModels.TankSkin> getColorSkins()
    {
        ArrayList<TankModels.TankSkin> skins = new ArrayList<>();
        for (TankModels.TankSkin tankSkin : tankSkins.values())
        {
            if (tankSkin.color != null)
                skins.add(tankSkin);
        }

        return skins;
    }

    public ArrayList<TankModels.TankSkin> getTurretSkins()
    {
        ArrayList<TankModels.TankSkin> skins = new ArrayList<>();
        for (TankModels.TankSkin tankSkin : tankSkins.values())
        {
            if (tankSkin.turret != null)
                skins.add(tankSkin);
        }

        return skins;
    }

    public ArrayList<TankModels.TankSkin> getTurretBaseSkins()
    {
        ArrayList<TankModels.TankSkin> skins = new ArrayList<>();
        for (TankModels.TankSkin tankSkin : tankSkins.values())
        {
            if (tankSkin.turretBase != null)
                skins.add(tankSkin);
        }

        return skins;
    }
}
