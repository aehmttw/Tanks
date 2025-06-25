package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ScreenCinematicTitle extends Screen implements ISeparateBackgroundScreen
{
    double t = 0;
    double t2 = 0;

    boolean shot = false;
    public Tank logo;
    public Mine mine;

    public ScreenCinematicTitle()
    {
//        this.logo = new TankDummyLoadingScreen(-200, Drawing.drawing.sizeY / 2);
        this.logo.size *= 2;
        this.logo.invulnerable = true;
        this.logo.drawAge = 50;
        this.logo.depthTest = false;
        Game.movables.clear();
        this.logo.networkID = 0;
        Game.movables.add(logo);
        ScreenGame.finished = false;

//        mine = new Mine(this.centerX + 200, this.centerY, this.logo, this.logo.mineItem);
        mine.size *= 2;
        mine.explosion.radius *= 2;
        mine.timer = 100000;
    }

    @Override
    public void drawWithoutBackground()
    {
        Drawing.drawing.setInterfaceFontSize(70);

        double c = 0.5 - (8) * 3.0 / 30;
        if (c < 0)
            c = 1 + c;

        double[] col = Game.getRainbowColor(c);

        Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2);
        Drawing.drawing.drawInterfaceText(this.centerX + 5, this.centerY - 50 + 5, "Get bonuses");
        Drawing.drawing.drawInterfaceText(this.centerX + 5, this.centerY + 50 + 5,"based on your performance!");
        Drawing.drawing.setColor(col[0], col[1], col[2]);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 50, "Get bonuses");
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 50,"based on your performance!");

//        Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2);
//        Drawing.drawing.drawInterfaceText(this.centerX + 5, this.centerY + 5, "...and even crazier!");
//        Drawing.drawing.setColor(col[0], col[1], col[2]);
//        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "...and even crazier!");

//        Drawing.drawing.setColor(174, 92, 16);
//        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
//
//        Drawing.drawing.setColor(255, 255, 255);
//        Drawing.drawing.drawInterfaceImage("turret.png", this.centerX, this.centerY, Drawing.drawing.interfaceSizeX, Drawing.drawing.interfaceSizeY);
//
//        Drawing.drawing.setColor(255, 255, 255, 127);
//        Drawing.drawing.fillInterfaceRect(Math.min(t * 20 / 2, Drawing.drawing.interfaceSizeX / 2), this.centerY, Math.min(t * 20, Drawing.drawing.interfaceSizeX), 280);
//
//
//        if (mine.destroy)
//        {
//            t2 = Math.min(t2 + Panel.frameFrequency * 2, 50);
//
//            Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255));
//            Drawing.drawing.setInterfaceFontSize(160 * t2 / 50);
//            Drawing.drawing.drawInterfaceText(mine.posX + 8, mine.posY - t2 * 0.5 + 8, "Tanks");
//            Drawing.drawing.setInterfaceFontSize(64 * t2 / 50);
//            Drawing.drawing.setColor(0, 0, 0);
//            Drawing.drawing.drawInterfaceText(mine.posX + 4, mine.posY + t2 * 1.5 + 4, "The Crusades");
//
//            Drawing.drawing.setColor(0, 150, 255);
//            Drawing.drawing.setInterfaceFontSize(160 * t2 / 50);
//            Drawing.drawing.drawInterfaceText(mine.posX, mine.posY - t2 * 0.5, "Tanks");
//            Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255));
//            Drawing.drawing.setInterfaceFontSize(64 * t2 / 50);
//            Drawing.drawing.drawInterfaceText(mine.posX, mine.posY + t2 * 1.5, "The Crusades");
//        }
//
//        if (t >= 600)
//        {
//            double a = Math.min(t - 600, 50) * 2.55 * 2;
//            Drawing.drawing.setColor(180, 180, 180, a);
//            Drawing.drawing.fillInterfaceGlow(this.centerX, this.centerY + 170, 600, 140);
//            Drawing.drawing.fillInterfaceGlow(this.centerX, this.centerY + 340, 1200, 140);
//            Drawing.drawing.setColor(0, 0, 0, a);
//            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 170, "Update 1.5");
//            Drawing.drawing.setInterfaceFontSize(64 * t2 / 75);
//            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + 340, "Now with the crazy Arcade Mode!");
//
//        }
//
//        for (int i = 0; i < Game.tracks.size(); i++)
//        {
//            Game.tracks.get(i).draw();
//        }
//
//        for (int i = Game.movables.size() - 1; i >= 0; i--)
//        {
//            Game.movables.get(i).draw();
//        }
//
//        for (int i = 0; i < Game.effects.size(); i++)
//        {
//            Game.effects.get(i).draw();
//        }
//
//        for (int i = 0; i < Game.effects.size(); i++)
//        {
//            Game.effects.get(i).drawGlow();
//        }
    }

    @Override
    public void update()
    {
//        if (t > 75 && !Game.movables.contains(mine) && !mine.destroy)
//            Game.movables.add(mine);
//
//        if (t > 0 && t < 100)
//            this.logo.vX = 2.5;
//        else if (t >= 100)
//        {
//            if (!shot)
//            {
//                Drawing.drawing.playGlobalSound("shoot.ogg");
//
//                Bullet b = new Bullet(this.logo.posX, this.logo.posY, 0, this.logo, this.logo.bullet);
//                b.size *= 2;
//                b.setPolarMotion(this.logo.angle, 3.125);
//                this.logo.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0);
//
//                b.moveOut(50 / 3.125 * this.logo.size / Game.tile_size);
//                b.effect = Bullet.BulletEffect.trail;
//
//                Game.movables.add(b);
//                shot = true;
//            }
//
//            this.logo.vX *= 0.95;
//        }
//
//        Obstacle.draw_size = Game.tile_size;
//        for (int i = 0; i < Game.tracks.size(); i++)
//        {
//            Game.tracks.get(i).update();
//        }
//
//        for (int i = 0; i < Game.movables.size(); i++)
//        {
//            Movable m = Game.movables.get(i);
//            m.preUpdate();
//            m.update();
//        }
//
//        for (int i = 0; i < Game.effects.size(); i++)
//        {
//            Game.effects.get(i).update();
//        }
//
//        Game.tracks.removeAll(Game.removeTracks);
//        Game.removeTracks.clear();
//
//        Game.movables.removeAll(Game.removeMovables);
//        Game.removeMovables.clear();
//
//        Game.effects.removeAll(Game.removeEffects);
//        Game.removeEffects.clear();
    }

    @Override
    public void draw()
    {
        t += Panel.frameFrequency;

        this.drawDefaultBackground();
        this.drawWithoutBackground();
    }
}
