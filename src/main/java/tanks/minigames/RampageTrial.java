package tanks.minigames;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.event.*;
import tanks.tank.*;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RampageTrial extends Minigame
{
    public double age = 0;

    public double lastHit = -1000;
    public int chain = 0;
    public double lastRampage = 0;
    public double finishTimer = 0;
    public boolean finished = false;

    public int rampageDuration = 500;
    public int rampageExitDuration = 200;
    public int maxPower = 8;
    public static String[] rampage_titles = new String[]{"Rampage!", "Extra rampage!", "Super rampage!", "Mega rampage!!", "Giga rampage!!", "Insane rampage!!", "Ultimate rampage!!!", "Godlike rampage!!!"};

    public HashMap<Player, Double> playerDeathTimes = new HashMap<>();
    public HashMap<Player, Tank> playerDeaths = new HashMap<>();

    public double chainOpacity = 1;

    public Random random;

    public RampageTrial(String level)
    {
        super(level);
        this.customLevelEnd = true;
        this.forceSpeedrunTimer = true;
        this.noLose = true;
        this.disableEndMusic = true;
        this.customIntroMusic = true;
        this.showItems = true;
        this.introMusic = "arcade/battle_intro.ogg";
        this.disableFriendlyFire = true;

        if (Game.deterministicMode)
            this.random = new Random(0);
        else
            this.random = new Random();
    }

    @Override
    public void loadLevel()
    {
        super.loadLevel();
        Game.playerTank.team = Game.playerTeamNoFF;
    }

    @Override
    public boolean levelEnded()
    {
        return finished;
    }

    @Override
    public void onKill(Tank attacker, Tank target)
    {
        if (ScreenPartyLobby.isClient)
            return;

        if (target instanceof IServerPlayerTank && !ScreenPartyLobby.isClient)
        {
            playerDeathTimes.put(((IServerPlayerTank) target).getPlayer(), this.age);
            playerDeaths.put(((IServerPlayerTank) target).getPlayer(), target);
        }

        if ((attacker instanceof IServerPlayerTank) && !(target instanceof IServerPlayerTank))
        {
            Drawing.drawing.playSound("hit_chain.ogg", (float) Math.pow(2, Math.min(maxPower * 3 - 1, chain) / 12.0), 0.5f);
            chain++;
            Game.eventsOut.add(new EventArcadeHit(chain, target.posX, target.posY, target.size / 2, target.coinValue));

            int power = Math.min(maxPower, chain / 3);

            Effect e = Effect.createNewEffect(target.posX, target.posY, target.size / 2, Effect.EffectType.chain);
            e.radius = chain;
            Game.effects.add(e);

            if (chain % 3 == 0)
            {
                setRampage(chain / 3);
            }

            lastHit = age;

            for (Movable m : Game.movables)
            {
                if (m instanceof Tank)
                {
                    //Tank t = (Tank) m;

                    if (chain / 3 > 0)
                    {
                        double duration = rampageDuration + rampageExitDuration;
                        double detAge = rampageExitDuration;
                        m.addStatusEffect(StatusEffect.arcade_rampage[power - 1], 0, detAge, duration);
                    }
                }
            }
        }
    }

    public void setRampage(int value)
    {
        if (!ScreenPartyLobby.isClient)
            Game.eventsOut.add(new EventArcadeRampage(value));
        else
            this.chain = value * 3;

        value = Math.min(maxPower, value);

        if (value <= 0)
        {
            chain = 0;
            for (int i = 1; i <= 8; i++)
            {
                Drawing.drawing.removeSyncedMusic("arcade/rampage" + i + ".ogg", 2000);
            }
        }
        else
        {
            lastRampage = age;
            Drawing.drawing.playSound("rampage.ogg", (float) Math.pow(2, (value - 1) / 12.0));
            if (chain / 3 <= 8)
            {
                Drawing.drawing.addSyncedMusic("arcade/rampage" + value + ".ogg", Game.musicVolume, true, 500);
            }
        }
    }

    @Override
    public void update()
    {
        age += Panel.frameFrequency;

        if (chain > 0)
            chainOpacity = Math.min(1, chainOpacity + Panel.frameFrequency / 20);
        else
            chainOpacity = Math.max(0, chainOpacity - Panel.frameFrequency / 20);

        if (!ScreenPartyLobby.isClient)
        {
            for (int i = 0; i < this.includedPlayers.size(); i++)
            {
                if (!Game.players.contains(this.includedPlayers.get(i)))
                {
                    this.includedPlayers.remove(i);
                    i--;
                }
            }

            if (age - lastHit > rampageDuration)
            {
                setRampage(0);
            }

            ArrayList<Player> alivePlayers = new ArrayList<>();
            ArrayList<Player> totalPlayers = new ArrayList<>();

            int enemies = 0;
            for (Movable m : Game.movables)
            {
                if (m instanceof IServerPlayerTank)
                {
                    totalPlayers.add(((IServerPlayerTank) m).getPlayer());
                    alivePlayers.add(((IServerPlayerTank) m).getPlayer());
                }

                if (m instanceof Crate)
                {
                    m = ((Crate) m).tank;
                    if (m instanceof IServerPlayerTank)
                        totalPlayers.add(((IServerPlayerTank) m).getPlayer());
                }

                else if (m instanceof Tank && m.team != Game.playerTeamNoFF)
                {
                    enemies++;
                }
            }

            finished = enemies <= 0;

            this.availablePlayerSpawns.clear();
            for (int i = 0; i < this.playerSpawnsX.size(); i++)
            {
                this.availablePlayerSpawns.add(i);
            }

            for (Player p: this.includedPlayers)
            {
                if (playerDeathTimes.get(p) != null && age - playerDeathTimes.get(p) >= 500)
                {
                    respawnPlayer(p);
                }
            }
        }
    }

    public void respawnPlayer(Player p)
    {
        playerDeathTimes.remove(p);
        Tank old = playerDeaths.remove(p);

        TankPlayer t = new TankPlayer(old.posX, old.posY, old.angle);
        t.team = Game.playerTeamNoFF;
        t.player = p;
        t.colorR = p.colorR;
        t.colorG = p.colorG;
        t.colorB = p.colorB;
        t.secondaryColorR = p.colorR2;
        t.secondaryColorG = p.colorG2;
        t.secondaryColorB = p.colorB2;
        t.invulnerabilityTimer = 250;
        Game.movables.add(new Crate(t, 1000));
        Game.eventsOut.add(new EventAirdropTank(t, 1000));
    }

    public String getRampageTitle()
    {
        if (chain < 3)
            return "";

        String prefix = (chain / 3 - rampage_titles.length + 1) + "x ";

        if (chain / 3 - 1 < rampage_titles.length)
            prefix = "";

        return prefix + Translation.translate(rampage_titles[Math.min(chain / 3 - 1, rampage_titles.length - 1)]);
    }

    @Override
    public void draw()
    {
        if (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).paused && ((ScreenGame) Game.screen).screenshotMode)
            return;

        if (age - lastRampage < 200 && chain >= 3)
        {
            int power = Math.min(maxPower, chain / 3);

            double c = 0.5 - (power) * 3.0 / 30;
            if (c < 0)
                c += (int) (-c) + 1;

            double[] col = Game.getRainbowColor(c);
            double frac = (1 - Math.max(0, age - lastRampage - 100) / 100.0);
            double mul = (1 + (1 - Math.min(1, (age - lastRampage) / 25.0)));

            Drawing.drawing.setInterfaceFontSize(50 * mul);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 5, Drawing.drawing.interfaceSizeY / 2 + 5, getRampageTitle());
            Drawing.drawing.setColor(col[0], col[1], col[2], frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, getRampageTitle());

            Drawing.drawing.setInterfaceFontSize(20 * mul);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 2.5, Drawing.drawing.interfaceSizeY / 2 + 33 * mul + 2.5, "+%d%% faster!", power * 20);
            Drawing.drawing.setColor(col[0], col[1], col[2], frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 33 * mul, "+%d%% faster!", power * 20);
        }

        this.drawChainTimer();
    }

    public void drawChainTimer()
    {
        double pulse = 5 * (1 - Math.min(1, (age - lastHit) / 25));
        if (chain < 2)
            pulse = 1;

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255, 128 * (100 - Game.player.hotbar.percentHidden) / 100.0 * chainOpacity);
        else
            Drawing.drawing.setColor(0, 0, 0, 128 * (100 - Game.player.hotbar.percentHidden) / 100.0 * chainOpacity);

        int x = (int) ((Drawing.drawing.interfaceSizeX / 2));
        int y = (int) (Drawing.drawing.interfaceSizeY - 100 + Game.player.hotbar.percentHidden - Game.player.hotbar.verticalOffset);

        double c = 0.5 - Math.min(maxPower * 3, chain) / 30.0;
        if (c < 0)
            c += (int) (-c) + 1;

        double[] col = Game.getRainbowColor(c);

        double lh = lastHit;

        if (this.age <= 0)
            chainOpacity = 0;

        Drawing.drawing.fillInterfaceRect(x, y, 350 * chainOpacity + pulse, 5 + pulse);

        if (chain > 0)
        {
            double xo = 350 * (1 - chainOpacity) / 2;
            Drawing.drawing.setColor(col[0], col[1], col[2], chainOpacity * (100 - Game.player.hotbar.percentHidden) * 2.55);
            Drawing.drawing.fillInterfaceProgressRect(x, y, 350 * chainOpacity + pulse, 5 + pulse, Math.max(0, 1 - (this.age - lh) / rampageDuration));

            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
            Drawing.drawing.fillInterfaceOval(x + xo - 175 - pulse / 2, y, 18 + pulse, 18 + pulse);
            Drawing.drawing.setInterfaceFontSize(12 + pulse);
            Drawing.drawing.setColor(255, 255, 255, (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
            Drawing.drawing.drawInterfaceText(x + xo - 175 - pulse / 2, y, chain + "");
        }

        double mul = (1 + 0.5 * (1 - Math.min(1, (age - lastRampage) / 25)));
        Drawing.drawing.setInterfaceFontSize(24 * mul);
        Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
        Drawing.drawing.drawInterfaceText(x + 2, y + 2 - 17, getRampageTitle());
        Drawing.drawing.setColor(Math.min(255, col[0]), Math.min(255, col[1]), Math.min(255, col[2]), (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
        Drawing.drawing.drawInterfaceText(x, y - 17, getRampageTitle());
    }
}
