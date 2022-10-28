package tanks.gui.screen;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.minigames.Arcade;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
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

        ArrayList<Bonus> bonuses = new ArrayList<>();
        bonuses.add(new Bonus("Tank driver", 5, 255, 255, 40));
        bonuses.add(new Bonus("Arcade player", 5, 40, 40, 255));
        bonuses.add(new Bonus("Participation medal", 5, 255, 40, 40));

        if (a.survivedFrenzy)
            bonuses.add(new Bonus("Survived the frenzy!!!", 250, 255, 180, 0));

        if (a.deathCount == 0)
            bonuses.add(new Bonus("No deaths!!!", 200, 40, 255, 255));
        else if (a.deathCount <= 1)
            bonuses.add(new Bonus("Master defender!!", 100, 40, 255, 160));
        else if (a.deathCount <= 3)
            bonuses.add(new Bonus("Extra careful defender!!", 50, 40, 255, 40));
        else if (a.deathCount <= 5)
            bonuses.add(new Bonus("Cautious defender!", 25, 160, 255, 40));
        else if (a.deathCount <= 8)
            bonuses.add(new Bonus("Wary defender", 10, 255, 255, 40));

        else if (a.deathCount >= 12)
            bonuses.add(new Bonus("Crash tester", 50, 255, 255, 40));

        if (a.maxChain > 5)
        {
            double c = 0.5 - Math.min(Arcade.max_power * 3, a.maxChain) / 30.0;
            if (c < 0)
                c += (int) (-c) + 1;

            double[] col = Game.getRainbowColor(c);

            bonuses.add(new Bonus(a.maxChain + " kill chain!", a.maxChain / 3 * 5, col[0], col[1], col[2]));
        }

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
                for (int j = 0; j < 100; j++)
                {
                    Button.addEffect(this.centerX, this.centerY + (i - 1) * 40, this.objWidth, this.objHeight, Game.effects, 2, -1, 0.5);
                }
                Game.game.window.soundPlayer.setMusicVolume(Game.musicVolume * 0.25f + 0.25f * bonusCount);
            }
        }

        if (age >= firstBonusTime + interBonusTime * 3 && bonusCount < 4)
        {
            Drawing.drawing.playSound("destroy.ogg");
            this.music = "waiting_win.ogg";
            Panel.forceRefreshMusic = true;
            bonusCount = 4;
        }

        if (age >= firstBonusTime + interBonusTime * 5)
        {
            this.score += bonuses.get(0).value + bonuses.get(1).value + bonuses.get(2).value;
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
    }
}
