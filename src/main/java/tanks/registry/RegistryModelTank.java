package tanks.registry;

import basewindow.IModel;
import tanks.Drawing;

import java.util.ArrayList;

public class RegistryModelTank
{
    public ArrayList<TankModelEntry> tankBaseModels = new ArrayList<>();
    public ArrayList<TankModelEntry> tankColorModels = new ArrayList<>();
    public ArrayList<TankModelEntry> turretBaseModels = new ArrayList<>();
    public ArrayList<TankModelEntry> turretModels = new ArrayList<>();
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

    public static IModel[] toModelArray(ArrayList<TankModelEntry> entries)
    {
        IModel[] models = new IModel[entries.size()];
        for (int i = 0; i < entries.size(); i++)
        {
            models[i] = Drawing.drawing.createModel(entries.get(i).dir);
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
}
