package tanks.gui.screen;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Firework;
import tanks.minigames.Arcade;
import tanks.tank.Tank;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScreenArcadeBonuses extends Screen implements IDarkScreen
{
    public double age = 0;

    public double firstBonusTime = 200;
    public double interBonusTime = 100;
    public int bonusCount = 0;
    public int score;
    public int originalScore;
    public double lastPoints = -1000;
    public boolean odd = false;
    public int fireworksToSpawn = 0;
    public double fireworkCooldown = 0;
    public int pointsPerFirework = 10;

    ArrayList<Firework> spawnedFireworks = new ArrayList<>();
    ArrayList<Firework> fireworks1 = new ArrayList<>();
    ArrayList<Firework> fireworks2 = new ArrayList<>();

    public ArrayList<Bonus> bonuses = new ArrayList<>();

    public static class Bonus
    {
        public String name;
        public int value;

        public double red;
        public double green;
        public double blue;

        public Bonus(String name, int value, double r, double g, double b)
        {
            this.name = name;
            this.value = value;
            this.red = r;
            this.green = g;
            this.blue = b;
        }
    }

    public ScreenArcadeBonuses(Arcade a)
    {
        this.music = "arcade/drumroll.ogg";
        this.score = a.score;
        this.originalScore = a.score;

        ArrayList<Bonus> bonuses = new ArrayList<>();
        bonuses.add(new Bonus("Tank driver", 5, 255, 255, 40));
        bonuses.add(new Bonus("Arcade player", 5, 40, 40, 255));
        bonuses.add(new Bonus("Participation medal", 5, 255, 40, 40));

        if (a.survivedFrenzy)
            bonuses.add(new Bonus("Survived the frenzy!!!", 250, 255, 180, 0));
        else if (a.frenzyTanksDestroyed >= 20)
            bonuses.add(new Bonus("Frenzy god!!", 100, 255, 160, 0));
        else if (a.frenzyTanksDestroyed >= 15)
            bonuses.add(new Bonus("Frenzy maniac!", 60, 255, 127, 0));
        else if (a.frenzyTanksDestroyed >= 10)
            bonuses.add(new Bonus("Frenzy destroyer!", 40, 255, 40, 0));
        else if (a.frenzyTanksDestroyed >= 5)
            bonuses.add(new Bonus("Frenzy amateur", 20, 255, 40, 0));

        if (a.deathCount == 0)
            bonuses.add(new Bonus("No deaths!!!", 200, 40, 255, 255));
        else if (a.deathCount <= 1)
            bonuses.add(new Bonus("Master defender!!", 100, 40, 255, 160));
        else if (a.deathCount <= 3)
            bonuses.add(new Bonus("Extra careful defender!!", 50, 40, 255, 40));
        else if (a.deathCount <= 5)
            bonuses.add(new Bonus("Cautious defender!", 25, 160, 255, 40));
        else if (a.deathCount <= 7)
            bonuses.add(new Bonus("Wary defender", 10, 255, 255, 40));
        else if (a.deathCount >= 9)
            bonuses.add(new Bonus("Crash tester", 50, 255, 255, 40));

        if (a.maxChain > 5)
        {
            double c = 0.5 - Math.min(Arcade.max_power * 3, a.maxChain) / 30.0;
            if (c < 0)
                c += (int) (-c) + 1;

            double[] col = Game.getRainbowColor(c);

            bonuses.add(new Bonus(a.maxChain + " kill chain!", a.maxChain / 3 * 5, col[0], col[1], col[2]));
        }

        if (a.maxKillsPerFrame >= 2)
            bonuses.add(new Bonus(a.maxKillsPerFrame + "-kill explosion!", a.maxKillsPerFrame * 10, 255, 127, 0));

        if (a.score == 69)
            bonuses.add(new Bonus("Nice! ;)", 69, 255, 127, 0));

        if (a.score == 420)
            bonuses.add(new Bonus("MLG GAMER!!!", 100, 0, 180, 0));

        String digits = (a.score + "");

        if (a.score == 666)
            bonuses.add(new Bonus("Unholy score!", 100, 255, 0, 0));
        else if (digits.length() == 2 && a.score % 11 == 0)
                bonuses.add(new Bonus("Double digits!", 15, 160, 40, 255));
        else if (digits.length() == 3 && a.score % 111 == 0)
                bonuses.add(new Bonus("Triple digits!!", 40, 255, 40, 255));
        else if (digits.length() == 4 && a.score % 1111 == 0)
                bonuses.add(new Bonus("Quadruple digits!!", 125, 255, 40, 160));

        if (a.score % 1000 == 0)
            bonuses.add(new Bonus("Right on a thousand!!!", 100, 0, 255, 160));
        else if (a.score % 100 == 0)
            bonuses.add(new Bonus("Right on a hundred!!", 30, 0, 255, 255));
        else if (a.score % 10 == 0)
            bonuses.add(new Bonus("Multiple of 10!", 10, 0, 160, 255));

        if (a.kills >= a.bulletsFired)
            bonuses.add(new Bonus("Perfect aim!!!!", 200, 40, 255, 255));
        else if (a.kills >= a.bulletsFired * 0.8)
            bonuses.add(new Bonus("Bullseye aim!!!", 50, 40, 40, 255));
        else if (a.kills >= a.bulletsFired * 0.7)
            bonuses.add(new Bonus("Amazing aim!!", 40, 80, 40, 255));
        else if (a.kills >= a.bulletsFired * 0.6)
            bonuses.add(new Bonus("Awesome aim!!", 30, 160, 40, 255));
        else if (a.kills >= a.bulletsFired * 0.5)
            bonuses.add(new Bonus("Great aim!!", 20, 255, 40, 255));
        else if (a.kills >= a.bulletsFired * 0.4)
            bonuses.add(new Bonus("Good aim!", 10, 255, 40, 160));

        String most = null;
        int value = 0;
        for (String s: a.destroyedTanksValue.keySet())
        {
            if (a.destroyedTanksValue.get(s) > value)
            {
                value = a.destroyedTanksValue.get(s);
                most = s;
            }
        }
        Tank mostTank = Game.registryTank.getEntry(most).getTank(0, 0, 0);

        if (value >= 60)
            bonuses.add(new Bonus(Game.formatString(most) + " massacre!!!", 60, mostTank.secondaryColorR, mostTank.secondaryColorG, mostTank.secondaryColorB));
        else if (value >= 40)
            bonuses.add(new Bonus(Game.formatString(most) + " exterminator!!", 40, mostTank.secondaryColorR, mostTank.secondaryColorG, mostTank.secondaryColorB));
        else if (value >= 20)
            bonuses.add(new Bonus(Game.formatString(most) +  " destroyer!", 20, mostTank.secondaryColorR, mostTank.secondaryColorG, mostTank.secondaryColorB));

        while (!bonuses.isEmpty())
        {
            this.bonuses.add(bonuses.remove((int) (Math.random() * bonuses.size())));
        }

        Collections.sort(this.bonuses, new Comparator<Bonus>()
        {
            @Override
            public int compare(Bonus o1, Bonus o2)
            {
                return o2.value - o1.value;
            }
        });
    }

    @Override
    public void update()
    {
        if (this.age == 0)
            Game.game.window.soundPlayer.setMusicVolume(Game.musicVolume * 0.25f);

        Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
        this.age += Panel.frameFrequency;

        for (Effect e: Game.effects)
            e.update();

        Game.effects.removeAll(Game.removeEffects);

        for (int i = 0; i < 3; i++)
        {
            if (bonusCount == i && age >= firstBonusTime + interBonusTime * bonusCount)
            {
                bonusCount++;
                Drawing.drawing.playSound("bonus" + (i + 1) + ".ogg", 1f);
                for (int j = 0; j < this.bonuses.get(2 - i).value; j++)
                {
                    Drawing.drawing.setInterfaceFontSize(this.textSize);
                    Bonus b = this.bonuses.get(2 - i);
                    double size = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, b.name) / Drawing.drawing.interfaceScale;
                    addEffect(this.centerX, this.centerY + (i - 1) * 40, size, this.objHeight, Game.effects, 1 + b.value / 25.0, -1, 0.5, b.red, b.green, b.blue);
                }
                Game.game.window.soundPlayer.setMusicVolume(Game.musicVolume * (0.25f + 0.25f * bonusCount));
            }
        }

        if (age >= firstBonusTime + interBonusTime * 3 && bonusCount < 4)
        {
            Drawing.drawing.playSound("destroy.ogg");
            Drawing.drawing.playSound("win.ogg", 1.0f, true);
            this.music = "waiting_win.ogg";
            Panel.forceRefreshMusic = true;
            bonusCount = 4;

            if (!Game.effectsEnabled)
            {
                this.score += bonuses.get(0).value + bonuses.get(1).value + bonuses.get(2).value;
                this.lastPoints = this.age;
            }

            fireworksToSpawn = (pointsPerFirework - 1 + bonuses.get(0).value + bonuses.get(1).value + bonuses.get(2).value) / pointsPerFirework;
        }

        if (age >= firstBonusTime + interBonusTime * 5 && this.getFireworkArray().size() == 0)
        {
            Panel.winlose = Translation.translate("You scored %d points!", score);
            Game.screen = new ScreenInterlevel();
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        double heightFrac = Math.min(1, age / 25);
        double yPos = Drawing.drawing.interfaceSizeY * (1 - heightFrac / 2);
        Drawing.drawing.setColor(0, 0, 0, 127 * heightFrac);
        Drawing.drawing.fillInterfaceRect(this.centerX, yPos, this.objWidth * 2, this.objHeight * 10);
        Drawing.drawing.fillInterfaceRect(this.centerX, yPos, this.objWidth * 2 - 20, this.objHeight * 10 - 20);

        Drawing.drawing.setColor(255, 255, 255, 255 * heightFrac);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, yPos - 120, "Bonus points");

        for (Effect e: Game.effects)
            e.draw();

        for (Effect e: Game.effects)
            e.drawGlow();

        Drawing.drawing.setColor(255, 255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        for (int i = 0; i < 3; i++)
        {
            if (bonusCount > i)
            {
                int j = 2 - i;
                Drawing.drawing.setColor(bonuses.get(j).red, bonuses.get(j).green, bonuses.get(j).blue, 255);
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + (i - 1) * 40, bonuses.get(j).name);
                Drawing.drawing.displayInterfaceText(this.centerX + this.objWidth - 60, this.centerY + (i - 1) * 40, bonuses.get(j).value + "");
            }
        }

        if (bonusCount >= 4)
        {
            Drawing.drawing.setColor(255, 255, 255, 255);
            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + 120, "Total: " + (bonuses.get(0).value + bonuses.get(1).value + bonuses.get(2).value));
        }

        this.drawPoints();

        if (Game.effectsEnabled && !Game.game.window.drawingShadow)
        {
            fireworkCooldown -= Panel.frameFrequency;
            if (fireworksToSpawn > 0 && fireworkCooldown <= 0)
            {
                fireworksToSpawn--;
                fireworkCooldown = Math.random() * 5 + 2.5;
                Firework f = new Firework(Firework.FireworkType.rocket, this.centerX + (Math.random() - 0.5) * 120, this.centerY + 120, this.getFireworkArray());
                f.setRandomColor();
                f.setVelocity();
                f.maxAge /= 2;
                spawnedFireworks.add(f);
            }

            ArrayList<Firework> fireworks = getFireworkArray();
            Panel.frameFrequency *= 2;
            for (int i = 0; i < fireworks.size(); i++)
            {
                fireworks.get(i).drawUpdate(fireworks, getOtherFireworkArray());
            }
            Panel.frameFrequency /= 2;

            if (Game.glowEnabled)
            {
                for (int i = 0; i < getFireworkArray().size(); i++)
                {
                    fireworks.get(i).drawGlow();
                }
            }

            for (int i = 0; i < spawnedFireworks.size(); i++)
            {
                if (this.spawnedFireworks.get(i).age > this.spawnedFireworks.get(i).maxAge)
                {
                    this.spawnedFireworks.remove(i);
                    i--;
                    this.score = Math.min(this.originalScore + this.bonuses.get(0).value + this.bonuses.get(1).value + this.bonuses.get(2).value, this.score + this.pointsPerFirework);
                    this.lastPoints = this.age;
                }
            }

            fireworks.clear();

            odd = !odd;
        }
    }

    public void drawPoints()
    {
        double frac = Math.max(0, (25 - (age - lastPoints)) / 25);
        double alpha = (127 + 128 * frac);

        double heightFrac = Math.min(1, age / 25);

        Drawing.drawing.setColor(heightFrac * 255, heightFrac * 255, heightFrac * 255, alpha);

        double posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 175;
        double posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

        posX = posX * (1 - heightFrac) + this.centerX * heightFrac;
        posY = posY * (1 - heightFrac) + (this.objYSpace * 2) * heightFrac;

        Drawing.drawing.setInterfaceFontSize(36 * (1 + 0.25 * frac));
        String s = Translation.translate("Score: %d", score);
        double size = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale;
        Drawing.drawing.displayInterfaceText(posX - size / 2, posY, false, s);
    }

    public static void addEffect(double posX, double posY, double sizeX, double sizeY, ArrayList<Effect> glowEffects, double velocity, double mul, double max, double r, double g, double b)
    {
        Effect e = Effect.createNewEffect(posX, posY, Effect.EffectType.interfacePieceSparkle);
        e.radius = Math.random() * 10 - 5;

        if (mul == -1)
            mul = 2 * Math.max(0, (sizeY / 2 - 20) / sizeY);

        double total = (sizeX - sizeY) * 2 + sizeY * Math.PI;
        double rand = Math.random() * total;

        if (rand < sizeX - sizeY)
        {
            e.posX = posX + rand - (sizeX - sizeY) / 2;
            e.posY = posY + sizeY / 2 * mul;
            e.vY = velocity;
        }
        else if (rand < (sizeX - sizeY) * 2)
        {
            e.posX = posX + rand - (sizeX - sizeY) * 3 / 2;
            e.posY = posY - sizeY / 2 * mul;
            e.vY = -velocity;
        }
        else if (rand < (sizeX - sizeY) * 2 + sizeY * Math.PI / 2)
        {
            double a = (rand - (sizeX - sizeY) * 2) / sizeY * 2 - Math.PI / 2;
            e.posX = posX + (sizeX - sizeY) / 2;
            e.posX += sizeY / 2 * Math.cos(a) * mul;
            e.posY += sizeY / 2 * Math.sin(a) * mul;
            e.setPolarMotion(a, velocity);
        }
        else
        {
            double a = (rand - (sizeX - sizeY) * 2 + sizeY * Math.PI / 2) / sizeY * 2 + Math.PI / 2;
            e.posX = posX - (sizeX - sizeY) / 2;
            e.posX += sizeY / 2 * Math.cos(a) * mul;
            e.posY += sizeY / 2 * Math.sin(a) * mul;
            e.setPolarMotion(a, velocity);
        }

        //e.size = 0.5;
        e.colR = r;
        e.colG = g;
        e.colB = b;
        e.glowR = r / 4;
        e.glowG = g / 4;
        e.glowB = b / 4;
        double v = Math.random() * 0.5 + 0.25;
        e.vX *= v;
        e.vY *= v;
        e.maxAge *= (Math.random() + 0.5) * max;
        glowEffects.add(e);
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
