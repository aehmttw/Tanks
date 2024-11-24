package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Firework;

import java.util.ArrayList;

public class DisplayFireworks
{
    public static double firework_frequency = 0.08;
    public static int initial_fireworks = 5;
    public static double crusade_win_firework_multiplier = 2;
    public static boolean debug = false;

    public boolean addOwnFireworks;

    ArrayList<Firework> fireworks1 = new ArrayList<>();
    ArrayList<Firework> fireworks2 = new ArrayList<>();

    public double fireworkMultiplier = 1;

    boolean odd = false;

    public DisplayFireworks()
    {
        this(true);
    }

    public DisplayFireworks(boolean addOwnFireworks)
    {
        this.addOwnFireworks = addOwnFireworks;
        if (Panel.win && Game.effectsEnabled && addOwnFireworks)
        {
            for (int i = 0; i < initial_fireworks * fireworkMultiplier; i++)
            {
                Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, getFireworkArray());
                f.setRandomColor();
                f.setVelocity();
                getFireworkArray().add(f);
            }

            if (Crusade.crusadeMode && Crusade.currentCrusade.win)
                this.fireworkMultiplier = crusade_win_firework_multiplier;
        }
    }

    public void draw()
    {
        ArrayList<Firework> fireworks = getFireworkArray();

        if (addOwnFireworks && ((!debug && Math.random() < firework_frequency * fireworkMultiplier * Panel.frameFrequency * Game.effectMultiplier) || (debug && Game.game.input.shoot.isValid())))
        {
            if (debug)
                Game.game.input.shoot.invalidate();
            Firework f = new Firework(Firework.FireworkType.rocket, (Math.random() * 0.6 + 0.2) * Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY, fireworks);
            f.setRandomColor();
            f.setVelocity();
            getFireworkArray().add(f);
        }

        for (int i = 0; i < getFireworkArray().size(); i++)
        {
            Firework f = fireworks.get(i);

            if (f.type != Firework.FireworkType.particle_group)
                f.drawUpdate(fireworks, getOtherFireworkArray());
        }

        Firework.shader.set();
        for (int i = 0; i < getFireworkArray().size(); i++)
        {
            Firework f = fireworks.get(i);

            if (f.type == Firework.FireworkType.particle_group)
                f.drawUpdate(fireworks, getOtherFireworkArray());
        }

        Firework.trailShader.set();
        for (int i = 0; i < getFireworkArray().size(); i++)
        {
            Firework f = fireworks.get(i);

            if (f.type == Firework.FireworkType.particle_group)
                f.explosion.drawTrail();
        }

        Game.game.window.shaderDefault.set();

        if (Game.glowEnabled)
        {
            for (int i = 0; i < getFireworkArray().size(); i++)
            {
                Firework f = fireworks.get(i);

                if (f.type != Firework.FireworkType.particle_group)
                    f.drawGlow();
            }

            Firework.shader.set();
            for (int i = 0; i < getFireworkArray().size(); i++)
            {
                Firework f = fireworks.get(i);

                if (f.type == Firework.FireworkType.particle_group)
                    f.drawGlow();
            }
            Game.game.window.shaderDefault.set();
        }

        //A fix to some glitchiness on ios
        Drawing.drawing.setColor(0, 0, 0, 0);
        Drawing.drawing.fillInterfaceRect(0, 0, 0, 0);

        fireworks.clear();
        odd = !odd;
    }

    public ArrayList<Firework> getFireworkArray()
    {
        if (odd)
            return fireworks2;
        else
            return fireworks1;
    }

    public ArrayList<Firework> getOtherFireworkArray()
    {
        if (odd)
            return fireworks1;
        else
            return fireworks2;
    }
}
