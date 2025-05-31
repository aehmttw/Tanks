package tanks.minigames;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.*;
import tanks.hotbar.Hotbar;
import tanks.item.Item;
import tanks.item.ItemShield;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;
import tanks.tank.*;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Arcade extends Minigame
{
    public double age = 0;

    public double lastHit = -1000;
    public int chain = 0;
    public double lastRampage = 0;
    public boolean frenzy = false;
    public double finishTimer = 0;
    public double waveSize = 2;
    public double waveTriggerCount = 3;

    public static final int rampage_duration = 500;
    public static final int rampage_exit_duration = 200;
    public static final int max_power = 8;
    public static String[] rampage_titles = new String[]{"Rampage!", "Extra rampage!", "Super rampage!", "Mega rampage!!", "Giga rampage!!", "Insane rampage!!", "Ultimate rampage!!!", "Godlike rampage!!!"};

    public ArrayList<Tank> spawnedTanks = new ArrayList<>();
    public ArrayList<Tank> spawnedFrenzyTanks = new ArrayList<>();

    public HashSet<String> musics = new HashSet<>();

    public HashSet<String> playingMusics = new HashSet<>();
    public HashSet<String> prevMusics = new HashSet<>();

    public double timer = 13950;
    public double frenzyTime = -1000;

    public int score = 0;

    public int maxChain = 0;
    public int deathCount = 0;
    public boolean survivedFrenzy = false;
    public int bulletsFired = 0;
    public int kills = 0;
    public int frenzyTanksDestroyed = 0;
    public int killsThisFrame = 0;
    public int maxKillsPerFrame = 0;

    public HashMap<String, Integer> destroyedTanks = new HashMap<>();
    public HashMap<String, Integer> destroyedTanksValue = new HashMap<>();

    public HashMap<Player, Double> playerDeathTimes = new HashMap<>();

    public HashMap<String, Item.ItemStack<?>> itemsMap = new HashMap<>();
    public HashMap<String, String> tankItemsMap = new HashMap<>();

    public double chainOpacity = 1;

    public Random random;

    public ArrayList<ItemDrop> drops = new ArrayList<>();

    public Arcade(String level)
    {
        super(level);
        //this.flashBackground = true;
        this.customLevelEnd = true;
        this.hideSpeedrunTimer = true;
        this.noLose = true;
        this.disableEndMusic = true;
        this.customIntroMusic = true;
        this.showItems = true;
        this.introMusic = "arcade/battle_intro.ogg";
        this.disableFriendlyFire = true;
        this.removeMusicWhenDead = true;

        if (Game.deterministicMode)
            this.random = new Random(0);
        else
            this.random = new Random();

        ArrayList<String> items = Game.game.fileManager.getInternalFileContents("/items/items.tanks");
        if (!ScreenPartyLobby.isClient)
        {
            int id = 0;
            for (String si : items)
            {
                Item.ItemStack<?> i = Item.ItemStack.fromString(null, si);
                i.item.name = Translation.translate(i.item.name);

                itemNumbers.put(i.item.name, id + 1);
                id++;

                itemsMap.put(i.item.name, i);
            }

            tankItemsMap.put("mint", "Fire bullet");
            tankItemsMap.put("yellow", "Mega mine");
            tankItemsMap.put("red", "Laser");
            tankItemsMap.put("green", "Bouncy fire bullet");
            tankItemsMap.put("blue", "Zap");
            tankItemsMap.put("medic", "Shield");
            tankItemsMap.put("cyan", "Freezing bullet");
            tankItemsMap.put("orange", "Flamethrower");
            tankItemsMap.put("maroon", "Mega bullet");
            tankItemsMap.put("mustard", "Artillery shell");
            tankItemsMap.put("orangered", "Explosive bullet");
            tankItemsMap.put("darkgreen", "Mini bullet");
            tankItemsMap.put("black", "Dark fire bullet");
            tankItemsMap.put("salmon", "Homing bullet");
            tankItemsMap.put("lightblue", "Air");
            tankItemsMap.put("lightpink", "Laser");
            tankItemsMap.put("gold", "Zap");
            tankItemsMap.put("purple", "Block");
            tankItemsMap.put("magenta", "Block");
        }
        else
        {
            for (String si : items)
            {
                this.clientShop.add(new Item.ShopItem(Item.ItemStack.fromString(null, si)));
            }
        }
    }

    @Override
    public void loadLevel()
    {
        super.loadLevel();
        Game.playerTank.team = Game.playerTeamNoFF;
        Game.currentLevel.synchronizeMusic = true;
    }

    @Override
    public boolean levelEnded()
    {
        return false;
    }

    @Override
    public void onLevelEndQuick()
    {
        for (Movable m: Game.movables)
        {
            if (m instanceof Tank && m instanceof IServerPlayerTank && !m.destroy)
            {
                survivedFrenzy = true;
                break;
            }
        }
    }

    @Override
    public void onBulletFire(Bullet b)
    {
        if (b.tank instanceof IServerPlayerTank)
        {
            bulletsFired++;
        }
    }

    @Override
    public void onKill(Tank attacker, Tank target)
    {
        if (ScreenPartyLobby.isClient)
            return;

        if (tankItemsMap.get(target.name) != null)
        {
            Item.ItemStack<?> i = itemsMap.get(tankItemsMap.get(target.name)).getCopy();
            i.stackSize *= target.coinValue / 2;

            if (i instanceof ItemShield.ItemStackShield)
                i.stackSize /= 2;

            ItemDrop d = new ItemDrop(target.posX, target.posY, i);
            d.registerNetworkID();
            drops.add(d);
            Game.eventsOut.add(new EventItemDrop(d));
        }

        killsThisFrame++;

        if (target instanceof IServerPlayerTank && !ScreenPartyLobby.isClient)
        {
            playerDeathTimes.put(((IServerPlayerTank) target).getPlayer(), this.age);
        }

        if ((attacker instanceof IServerPlayerTank) && !(target instanceof IServerPlayerTank))
        {
            if (spawnedFrenzyTanks.contains(target))
                frenzyTanksDestroyed++;

            if (!destroyedTanks.containsKey(target.name))
                destroyedTanks.put(target.name, 0);

            if (!destroyedTanksValue.containsKey(target.name))
                destroyedTanksValue.put(target.name, 0);

            destroyedTanks.put(target.name, destroyedTanks.get(target.name) + 1);
            destroyedTanksValue.put(target.name, destroyedTanksValue.get(target.name) + target.coinValue);
            kills++;

            score += target.coinValue;

            String s = "hit_chain_2.ogg";
            if (Game.playerTank != null && !Game.playerTank.destroy)
                s = "hit_chain.ogg";

            Drawing.drawing.playSound(s, (float) Math.pow(2, Math.min(max_power * 3 - 1, chain) / 12.0), 0.5f);
            chain++;
            Game.eventsOut.add(new EventArcadeHit(chain, target.posX, target.posY, target.size / 2, target.coinValue));
            maxChain = Math.max(chain, maxChain);

            int power = Math.min(max_power, chain / 3);

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
                        double duration = rampage_duration + rampage_exit_duration;
                        double detAge = rampage_exit_duration;

                        if (frenzy)
                        {
                            duration = 0;
                            detAge = 0;
                        }

                        m.addStatusEffect(StatusEffect.arcade_rampage[power - 1], 0, detAge, duration);
                    }
                }

                /*if (m instanceof AreaEffect)
                {
                    AttributeModifier c = new AttributeModifier("rampage_speed", "speed", AttributeModifier.Operation.multiply, power / 5.0);
                    c.duration = rampage_duration + rampage_exit_duration;
                    c.deteriorationAge = rampage_exit_duration;
                    m.addUnduplicateAttribute(c);
                }*/
            }
        }
    }

    public void setRampage(int value)
    {
        if (!ScreenPartyLobby.isClient)
            Game.eventsOut.add(new EventArcadeRampage(value));
        else
            this.chain = value * 3;

        value = Math.min(max_power, value);

        if (value <= 0)
        {
            chain = 0;
            this.musics.clear();
        }
        else
        {
            lastRampage = age;
            String s = "rampage2.ogg";

            if (Game.playerTank != null && !Game.playerTank.destroy)
                s = "rampage.ogg";

            Drawing.drawing.playSound(s, (float) Math.pow(2, (value - 1) / 12.0));
            score += value * 5;
            if (chain / 3 <= 8)
            {
                this.musics.add("arcade/rampage" + value + ".ogg");
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

        if (frenzy)
            this.customLevelEnd = false;

        if (!ScreenPartyLobby.isClient)
        {
            Game.movables.addAll(drops);
            drops.clear();

            for (int i = 0; i < this.includedPlayers.size(); i++)
            {
                if (!Game.players.contains(this.includedPlayers.get(i)))
                {
                    this.includedPlayers.remove(i);
                    i--;
                }
            }

            if (age - lastHit > rampage_duration && !frenzy)
            {
                setRampage(0);
            }

            if (killsThisFrame > maxKillsPerFrame)
                maxKillsPerFrame = killsThisFrame;

            this.killsThisFrame = 0;

            ArrayList<Player> alivePlayers = new ArrayList<>();
            ArrayList<Player> totalPlayers = new ArrayList<>();

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

            }

            if (this.spawnedTanks.size() <= (Math.min(this.chain, max_power * 6) / (waveTriggerCount / 3)) + waveTriggerCount && alivePlayers.size() > 0 && timer > 0)
            {
                int count = (int) ((this.random.nextDouble() * this.waveSize + this.waveSize));
                for (int i = 0; i < count; i++)
                {
                    spawnTank();
                }
            }

            for (int i = 0; i < this.spawnedTanks.size(); i++)
            {
                if (this.spawnedTanks.get(i).destroy)
                {
                    this.spawnedTanks.remove(i);
                    i--;
                }
            }

            this.availablePlayerSpawns.clear();
            for (int i = 0; i < this.playerSpawnsX.size(); i++)
            {
                this.availablePlayerSpawns.add(i);
            }

            if (alivePlayers.size() <= 0 && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing)
            {
                Game.eventsOut.add(new EventArcadeClearMovables());

                for (Movable m : Game.movables)
                {
                    if (m instanceof Crate && (((Crate) m).tank instanceof IServerPlayerTank))
                        continue;

                    setRampage(0);

                    m.destroy = true;
                    spawnedTanks.clear();
                }

                if (totalPlayers.size() < this.includedPlayers.size() && !frenzy)
                {
                    deathCount++;

                    for (Player p : this.includedPlayers)
                    {
                        if (!totalPlayers.contains(p))
                        {
                            this.respawnPlayer(p);
                            totalPlayers.add(p);
                        }
                    }
                }
            }

            if (timer <= 0 && !frenzy)
            {
                if (alivePlayers.size() >= this.includedPlayers.size())
                {
                    frenzy = true;
                    frenzyTime = age;
                    Drawing.drawing.playSound("rampage.ogg");
                    Game.eventsOut.add(new EventArcadeFrenzy());

                    for (int i = 0; i < 30; i++)
                    {
                        spawnTank();
                    }
                }
                else
                {
                    for (Player p: this.includedPlayers)
                    {
                        if (!totalPlayers.contains(p))
                        {
                            respawnPlayer(p);
                        }
                    }
                }
            }
            else if (!frenzy)
            {
                for (Player p: this.includedPlayers)
                {
                    if (playerDeathTimes.get(p) != null && age - playerDeathTimes.get(p) >= 500)
                    {
                        respawnPlayer(p);
                    }
                }
            }
        }

        if (timer > 0)
        {
            int seconds = (int) (timer / 100 + 0.5);
            int secondHalves = (int) (timer / 50);
            timer -= Panel.frameFrequency;

            int newSeconds = (int) (timer / 100 + 0.5);
            int newSecondHalves = (int) (timer / 50);

            if (seconds <= 5)
            {
                if (newSecondHalves < secondHalves)
                    Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);
            }
            else if (newSeconds < seconds && seconds <= 10)
                Drawing.drawing.playSound("tick.ogg", 2f, 0.5f);

            if (seconds > newSeconds && (newSeconds == 10 || newSeconds == 30 || newSeconds == 60))
                Drawing.drawing.playSound("timer.ogg");
        }

        this.playingMusics.clear();

        if (Game.playerTank != null && !Game.playerTank.destroy)
            this.playingMusics.addAll(this.musics);

        for (String s: this.playingMusics)
        {
            if (!this.prevMusics.contains(s))
                Drawing.drawing.addSyncedMusic(s, Game.musicVolume, true, 500);
        }

        for (String s: this.prevMusics)
        {
            if (!this.playingMusics.contains(s))
                Drawing.drawing.removeSyncedMusic(s, 2000);
        }

        this.prevMusics.clear();
        this.prevMusics.addAll(this.playingMusics);
    }

    public void respawnPlayer(Player p)
    {
        playerDeathTimes.remove(p);

        int r;
        if (this.availablePlayerSpawns.size() > 0)
            r = this.availablePlayerSpawns.remove((int) (this.random.nextDouble() * this.availablePlayerSpawns.size()));
        else
            r = (int) (this.playerSpawnsX.size() * this.random.nextDouble());

        TankPlayer t = new TankPlayer(this.playerSpawnsX.get(r), this.playerSpawnsY.get(r), this.playerSpawnsAngle.get(r));
        t.team = Game.playerTeamNoFF;
        t.player = p;
        t.colorR = p.colorR;
        t.colorG = p.colorG;
        t.colorB = p.colorB;
        t.secondaryColorR = p.colorR2;
        t.secondaryColorG = p.colorG2;
        t.secondaryColorB = p.colorB2;
        double h = this.random.nextDouble() * 400 + 800;
        Game.movables.add(new Crate(t, h));
        Game.eventsOut.add(new EventAirdropTank(t, h));
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

        /*this.flashTimer -= Panel.frameFrequency;
        if (flashTimer <= 0)
        {
            flashTimer += 37.5;

            if (chain >= max_power * 3)
                flashTimer /= 2;

            for (int i = 0; i < Game.tilesFlash.length; i++)
            {
                for (int j = 0; j < Game.tilesFlash[i].length; j++)
                {
                    if (Math.random() < 0.01 * (chain / 3))
                    {
                        Game.tilesFlash[i][j] = 0.5;
                    }
                }
            }
        }

        for (int i = 0; i < Game.tilesFlash.length; i++)
        {
            for (int j = 0; j < Game.tilesFlash[i].length; j++)
            {
                Game.tilesFlash[i][j] = Math.max(0, Game.tilesFlash[i][j] - 0.005 * Panel.frameFrequency);
            }
        }*/

        if (age - lastRampage < 200 && chain >= 3)
        {
            int power = Math.min(max_power, chain / 3);

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
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 2.5, Drawing.drawing.interfaceSizeY / 2 + 33 * mul + 2.5, "+%d points!", power * 5);
            Drawing.drawing.setColor(col[0], col[1], col[2], frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 33 * mul, "+%d points!", power * 5);
        }

        if (age - frenzyTime < 400)
        {
            double frac = (1 - Math.max(0, age - frenzyTime - 100) / 100.0);
            double mul = (1 + (1 - Math.min(1, (age - frenzyTime) / 25.0)));
            double[] col = new double[]{255, 180, 0};

            Drawing.drawing.setInterfaceFontSize(75 * mul);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 5, Drawing.drawing.interfaceSizeY / 2 + 5, "Tank frenzy!");
            Drawing.drawing.setColor(col[0], col[1], col[2], frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Tank frenzy!");

            col[1] = 100;
            Drawing.drawing.setInterfaceFontSize(30 * mul);
            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 2.5, Drawing.drawing.interfaceSizeY / 2 + 40 * mul + 2.5, "Destroy as many tanks as you can!");
            Drawing.drawing.setColor(col[0], col[1], col[2], frac * 255);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 40 * mul, "Destroy as many tanks as you can!");
        }

        this.drawTimer();
        this.drawPoints();

        if (ScreenGame.finishedQuick)
        {
            if (finishTimer <= 0)
                Game.eventsOut.add(new EventArcadeEnd(survivedFrenzy));

            finishTimer += Panel.frameFrequency;
            double alpha = Obstacle.draw_size / Game.tile_size;
            double mul = (1 + (1 - Math.min(1, (finishTimer) / 25.0))) * alpha;
            Drawing.drawing.setInterfaceFontSize(100 * mul);

            if (!survivedFrenzy)
            {
                Drawing.drawing.setColor(100, 25, 25);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 5 * mul, Drawing.drawing.interfaceSizeY / 2 + 5 * mul, "Game over!");
                Drawing.drawing.setColor(200, 50, 50);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Game over!");
            }
            else
            {
                Drawing.drawing.setColor(127, 90, 0);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2 + 5 * mul, Drawing.drawing.interfaceSizeY / 2 + 5 * mul, "Victory!");
                Drawing.drawing.setColor(255, 180, 0);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Victory!");
            }
        }

        this.drawChainTimer();
    }

    @Override
    public void loadInterlevelScreen()
    {
        Game.screen = new ScreenArcadeBonuses(this);
    }

    public void spawnTank()
    {
        double destX = 0;
        double destY = 0;

        boolean found = false;

        boolean horizontal = this.random.nextDouble() < 0.5;
        boolean other = this.random.nextDouble() < 0.5;

        int xBound = Game.currentSizeX;
        int yBound = Game.currentSizeY;

        if (horizontal)
            yBound = 4;
        else
            xBound = 4;

        for (int i = 0; i < 5; i++)
        {
            int x = (int) (this.random.nextDouble() * xBound);
            int y = (int) (this.random.nextDouble() * yBound);

            if (other && !horizontal)
                x += Game.currentSizeX - 4;
            else if (other)
                y += Game.currentSizeY - 4;

            if (!Game.game.solidGrid[x][y])
            {
                found = true;

                for (Movable m: Game.movables)
                {
                    if ((m instanceof IServerPlayerTank) && (Math.pow(m.posX - (x * Game.tile_size), 2) + Math.pow(m.posY - (y * Game.tile_size), 2) <= Math.pow(Game.tile_size * 5, 2)))
                    {
                        found = false;
                        break;
                    }
                }

                if (found)
                {
                    destX = (x + 0.5) * Game.tile_size;
                    destY = (y + 0.5) * Game.tile_size;

                    break;
                }
            }
        }

        RegistryTank.TankEntry e = Game.registryTank.getRandomTank(this.random);

        while (e.name.equals("blue") || e.name.equals("red") || (!frenzy && e.weight < 1.0 / Math.max(1, chain - 2)))
            e = Game.registryTank.getRandomTank(this.random);

        Tank t = e.getTank(destX, destY, (int)(this.random.nextDouble() * 4));
        t.team = Game.enemyTeamNoFF;

        double h = this.random.nextDouble() * 400 + 800;
        Game.eventsOut.add(new EventAirdropTank(t, h));
        this.spawnedTanks.add(t);

        if (frenzy)
            this.spawnedFrenzyTanks.add(t);

        Game.movables.add(new Crate(t, h));
    }

    public void drawTimer()
    {
        int secondsTotal = (int) (timer / 100 + 0.5);
        double secondsFrac = (timer / 100 + 0.5) - secondsTotal;

        int seconds60 = secondsTotal % 60;
        int minutes = secondsTotal / 60;

        double sizeMul = 1;
        double alpha = 127;
        double red = 0;

        if (((ScreenGame) Game.screen).playing)
        {
            if (secondsTotal == 60 || secondsTotal == 30 || secondsTotal <= 10)
            {
                sizeMul = 1.5;

                if (secondsFrac > 0.4 && secondsFrac <= 0.8 && secondsTotal > 9)
                    alpha = 0;

                if (secondsTotal <= 9)
                    red = Math.max(0, secondsFrac * 2 - 1) * 255;

                if (secondsTotal <= 5 && red == 0)
                    red = Math.max(0, secondsFrac * 2) * 255;
            }
            else if (secondsTotal == 59 || secondsTotal == 29)
                sizeMul = 1.0 + Math.max((timer / 100 - secondsTotal), 0);
        }

        String st = Translation.translate("Time: ");
        String s = st + minutes + ":" + seconds60;
        if (seconds60 < 10)
            s = st + minutes + ":0" + seconds60;

        Drawing.drawing.setInterfaceFontSize(32 * sizeMul);
        Drawing.drawing.setColor(red, 0, 0, (alpha + red / 2) * Obstacle.draw_size / Game.tile_size);

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255 - red, 255 - red, (alpha + red / 2) * Obstacle.draw_size / Game.tile_size);

        double posX = Drawing.drawing.interfaceSizeX / 2;
        double posY = 50;

        if (ScreenGame.finishedQuick)
        {
            Drawing.drawing.setInterfaceFontSize(32);
            Drawing.drawing.setColor(0, 0, 0, 127 * Obstacle.draw_size / Game.tile_size);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255, 127);
        }

        if (timer > 0)
            Drawing.drawing.displayInterfaceText(posX, posY, s);
    }

    public void drawPoints()
    {
        double frac = Math.max(0, (25 - (age - lastHit)) / 25);
        double frac2 = Obstacle.draw_size / Game.tile_size;

        if (ScreenGame.finishedQuick)
            frac2 = 1;

        double alpha = (127 + 128 * frac) * frac2;

        if (Level.isDark() || (Game.screen instanceof IDarkScreen && Panel.win && Game.effectsEnabled))
            Drawing.drawing.setColor(255, 255, 255, alpha);
        else
            Drawing.drawing.setColor(0, 0, 0, alpha);

        double posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2 + Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 175;
        double posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;

        Drawing.drawing.setInterfaceFontSize(36 * (1 + 0.25 * frac));
        String s = Translation.translate("Score: %d", score);
        double size = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale;
        Drawing.drawing.displayInterfaceText(posX - size / 2, posY, false, s);

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
        int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - 100 + Game.player.hotbar.percentHidden - Game.player.hotbar.verticalOffset);

        double c = 0.5 - Math.min(max_power * 3, chain) / 30.0;
        if (c < 0)
            c += (int) (-c) + 1;

        double[] col = Game.getRainbowColor(c);

        double lh = lastHit;

        if (frenzy)
            lh = age;

        if (this.age <= 0)
            chainOpacity = 0;

        Drawing.drawing.fillInterfaceRect(x, y, Hotbar.bar_width * chainOpacity + pulse, 5 + pulse);

        if (chain > 0)
        {
            double xo = Hotbar.bar_width * (1 - chainOpacity) / 2;
            Drawing.drawing.setColor(col[0], col[1], col[2], chainOpacity * (100 - Game.player.hotbar.percentHidden) * 2.55);
            Drawing.drawing.fillInterfaceProgressRect(x, y, Hotbar.bar_width * chainOpacity + pulse, 5 + pulse, Math.max(0, 1 - (this.age - lh) / rampage_duration));

            Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
            Drawing.drawing.fillInterfaceOval(x + xo - Hotbar.bar_width / 2 - pulse / 2, y, 18 + pulse, 18 + pulse);
            Drawing.drawing.setInterfaceFontSize(12 + pulse);
            Drawing.drawing.setColor(255, 255, 255, (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
            Drawing.drawing.drawInterfaceText(x + xo - Hotbar.bar_width / 2 - pulse / 2, y, chain + "");
        }

        if (frenzy)
        {
            double mul = (1 + 0.5 * (1 - Math.min(1, (age - frenzyTime) / 25.0)));

            Drawing.drawing.setInterfaceFontSize(15 * mul);
            Drawing.drawing.setColor(255 / 2.0, 180 / 2.0, 0, 2.55 * (100 - Game.player.hotbar.percentHidden));
            Drawing.drawing.displayInterfaceText(x + 2, y + 2, "Tank frenzy!");
            Drawing.drawing.setColor(255, 180, 0, 2.55 * (100 - Game.player.hotbar.percentHidden));
            Drawing.drawing.displayInterfaceText(x, y, "Tank frenzy!");
        }

        double mul = (1 + 0.5 * (1 - Math.min(1, (age - lastRampage) / 25)));
        Drawing.drawing.setInterfaceFontSize(24 * mul);
        Drawing.drawing.setColor(col[0] / 2, col[1] / 2, col[2] / 2, (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
        Drawing.drawing.drawInterfaceText(x + 2, y + 2 - 17, getRampageTitle());
        Drawing.drawing.setColor(Math.min(255, col[0]), Math.min(255, col[1]), Math.min(255, col[2]), (100 - Game.player.hotbar.percentHidden) * 2.55 * chainOpacity);
        Drawing.drawing.drawInterfaceText(x, y - 17, getRampageTitle());
    }
}
