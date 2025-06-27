package tanks.gui.screen;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.bullet.BulletEffect;
import tanks.bullet.BulletEffectPropertyCategory;
import tanks.bullet.Trail;
import tanks.gui.Button;
import tanks.tank.Turret;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.util.ArrayList;

public class ScreenEditorBulletEffect extends ScreenEditorTanksONable<BulletEffect>
{
    public double trailLength;
    public double start;
    public double end;

    public ArrayList<Effect> particles = new ArrayList<>();
    public ArrayList<Effect> removeParticles = new ArrayList<>();

    public ScreenEditorBulletEffect(Pointer<BulletEffect> t, Screen screen)
    {
        super(t, screen);

        this.title = "Edit %s";
        this.objName = "bullet effect";
    }

    @Override
    public void setupTabs()
    {
        setTrailLength();
        this.currentTab = new TabTrail(this, "Trail", BulletEffectPropertyCategory.trail);
        new Tab(this, "Particles", BulletEffectPropertyCategory.particle);
        new Tab(this, "Glow", BulletEffectPropertyCategory.glow);
    }

    public void setTrailLength()
    {
        trailLength = 0;
        for (Trail t: this.target.get().trailEffects)
        {
            trailLength = Math.max(trailLength, t.maxLength + t.delay);
        }

        double length = Math.min(Drawing.drawing.interfaceSizeX * 0.6, trailLength * Bullet.bullet_size);
        start = Drawing.drawing.interfaceSizeX / 2 - length / 2;
        end = Drawing.drawing.interfaceSizeX / 2 + length / 2;
    }

    @Override
    public void update()
    {
        super.update();

        for (Effect e: this.particles)
        {
            e.update();

            if (e.age > e.maxAge)
                removeParticles.add(e);
        }

        BulletEffect be = this.target.get();
        if (be.enableParticles && Game.bulletTrails && Math.random() < Panel.frameFrequency * Game.effectMultiplier && Game.effectsEnabled)
        {
            Effect e = Effect.createNewEffect(start, 175, Effect.EffectType.interfacePiece);
            double var = 50;
            e.maxAge *= be.particleLifespan;

            double r1 = be.particleColor.red;
            double g1 = be.particleColor.green;
            double b1 = be.particleColor.blue;

            e.colR = Math.min(255, Math.max(0, r1 + Math.random() * var - var / 2));
            e.colG = Math.min(255, Math.max(0, g1 + Math.random() * var - var / 2));
            e.colB = Math.min(255, Math.max(0, b1 + Math.random() * var - var / 2));

            if (be.particleGlow <= 0)
                e.enableGlow = false;

            e.glowR = e.colR * (1 - be.particleGlow);
            e.glowG = e.colG * (1 - be.particleGlow);
            e.glowB = e.colB * (1 - be.particleGlow);

            e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Bullet.bullet_size / 50.0 * be.particleSpeed);
            e.vX += 3.125;
            particles.add(e);
        }

        particles.removeAll(removeParticles);
        removeParticles.clear();
    }

    @Override
    public void draw()
    {
        this.setTrailLength();

        super.draw();

        double y = 175;
        BulletEffect e = this.target.get();

        for (Effect f: this.particles)
        {
            f.draw();
        }

        for (Effect f: this.particles)
        {
            f.drawGlow();
        }

        for (Trail t : e.trailEffects)
        {
            t.drawForInterface(start, end, y, Bullet.bullet_size, trailLength);
        }

        if (!e.overrideGlowColor)
            Drawing.drawing.setColor(Turret.calculateSecondaryColor(0) * e.glowIntensity, Turret.calculateSecondaryColor(150) * e.glowIntensity, Turret.calculateSecondaryColor(255) * e.glowIntensity, 255, e.glowGlowy ? 1 : 0);
        else
            Drawing.drawing.setColor(e.glowColor.red * e.glowIntensity, e.glowColor.green * e.glowIntensity, e.glowColor.blue * e.glowIntensity, 255, e.glowGlowy ? 1 : 0);

        Drawing.drawing.fillInterfaceGlow(start, y, Bullet.bullet_size * e.glowSize, Bullet.bullet_size * e.glowSize,  !e.glowGlowy);

        Drawing.drawing.setColor(Turret.calculateSecondaryColor(0) * e.glowIntensity, Turret.calculateSecondaryColor(150) * e.glowIntensity, Turret.calculateSecondaryColor(255) * e.glowIntensity, 255, e.glowGlowy ? 1 : 0);
        Drawing.drawing.fillInterfaceOval(start, y, Bullet.bullet_size, Bullet.bullet_size);
        Drawing.drawing.setColor(0, 150, 255, 255, e.luminance);
        Drawing.drawing.fillInterfaceOval(start, y, Bullet.bullet_size * 0.6, Bullet.bullet_size * 0.6);
    }

    public class TabTrail extends Tab
    {
        public TabTrail(ScreenEditorTanksONable<BulletEffect> screen, String name, String category)
        {
            super(screen, name, category);

            this.trails = screen.target.get().trailEffects;

            this.setupTrails();

            this.next.image = "icons/forward.png";
            this.next.imageSizeX = 25;
            this.next.imageSizeY = 25;
            this.next.imageXOffset = 145;

            this.prev.image = "icons/back.png";
            this.prev.imageSizeX = 25;
            this.prev.imageSizeY = 25;
            this.prev.imageXOffset = -145;

            this.last.image = "icons/last.png";
            this.last.imageSizeX = 20;
            this.last.imageSizeY = 20;
            this.last.imageXOffset = 0;

            this.first.image = "icons/first.png";
            this.first.imageSizeX = 20;
            this.first.imageSizeY = 20;
            this.first.imageXOffset = 0;

            this.last.posX = this.next.posX + screen.objXSpace / 2 + screen.objHeight / 2;
            this.last.posY = this.next.posY;

            this.first.posX = this.previous.posX - screen.objXSpace / 2 - screen.objHeight / 2;
            this.first.posY = this.previous.posY;

            this.next.enabled = false;
            this.prev.enabled = false;
        }

        public ArrayList<Trail> trails;

        public int page = 0;
        public static final int trails_per_page = 5;
        public static final int buttons_start = 300;
        public static final int buttons_spacing = 80;

        public double padding = 80;

        Button next = new Button(screen.centerX + screen.objXSpace / 2, screen.centerY + screen.objYSpace * 4.75, screen.objWidth, screen.objHeight, "Next page", () -> page++);

        Button prev = new Button(screen.centerX - screen.objXSpace / 2, screen.centerY + screen.objYSpace * 4.75, screen.objWidth, screen.objHeight, "Previous page", () -> page--);

        Button first = new Button(screen.centerX - screen.objXSpace - screen.objHeight * 2, screen.centerY + screen.objYSpace * 4.75, screen.objHeight, screen.objHeight, "", () -> page = 0);

        Button last = new Button(screen.centerX + screen.objXSpace + screen.objHeight * 2, screen.centerY + screen.objYSpace * 4.75, screen.objHeight, screen.objHeight, "", new Runnable()
        {
            @Override
            public void run()
            {
                page = (buttons.size() - 1) / trails_per_page;
            }
        }
        );

        public ArrayList<Button> buttons = new ArrayList<>();

        public void setupTrails()
        {
            this.buttons.clear();
            for (int i = 0; i < this.trails.size(); i++)
            {
                if (this.trails.get(i) == null)
                {
                    this.trails.remove(i);
                    i--;
                    continue;
                }

                int j = i;
                this.buttons.add(new Button(Drawing.drawing.interfaceSizeX / 2, buttons_start + buttons_spacing * (i % trails_per_page), Drawing.drawing.interfaceSizeX * 0.6 + padding, 60, "", () ->
                {
                    ScreenEditorTrail s = new ScreenEditorTrail(this.trails, new ArrayListIndexPointer<>(this.trails, j, true), Game.screen);
                    s.onComplete = this::setupTrails;
                    Game.screen = s;
                }));
            }

            this.buttons.add(new Button(Drawing.drawing.interfaceSizeX / 2, buttons_start + buttons_spacing * (this.trails.size() % trails_per_page), 60, 60, "+", () ->
            {
                this.trails.add(new Trail());
                ScreenEditorTrail s = new ScreenEditorTrail(this.trails, new ArrayListIndexPointer<>(this.trails, this.trails.size() - 1, true), Game.screen);
                s.onComplete = this::setupTrails;
                Game.screen = s;
            }));
        }

        @Override
        public void update()
        {
            for (int i = trails_per_page * page; i < trails_per_page * (page + 1); i++)
            {
                if (i < this.buttons.size())
                    buttons.get(i).update();
            }

            if (trails.size() >= trails_per_page)
            {
                prev.enabled = page > 0;
                next.enabled = page < trails.size() / trails_per_page;

                prev.update();
                next.update();
            }

            if (trails.size() >= trails_per_page * 2)
            {
                first.enabled = prev.enabled;
                last.enabled = next.enabled;
                first.update();
                last.update();
            }
        }

        @Override
        public void drawOver()
        {
            for (int i = trails_per_page * page; i < trails_per_page * (page + 1); i++)
            {
                if (i < this.trails.size())
                {
                    if (buttons.get(i).selected)
                        this.trails.get(i).drawForInterface(start, end, 175, Bullet.bullet_size, ((ScreenEditorBulletEffect)this.screen).trailLength, true);
                }
            }
        }

        @Override
        public void draw()
        {
            double max = 0;
            for (Trail t: this.trails)
            {
                max = Math.max(max, t.maxLength + t.delay);
            }

            for (int i = trails_per_page * page; i < trails_per_page * (page + 1); i++)
            {
                if (i < this.trails.size())
                {
                    buttons.get(i).draw();
                    this.trails.get(i).drawForInterface(start, end, buttons_start + buttons_spacing * (i % trails_per_page), Bullet.bullet_size, max);
                }
                else if (i == this.trails.size())
                    this.buttons.get(i).draw();
            }

            if (trails.size() >= trails_per_page)
            {
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.setColor(0, 0, 0);
                Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 20 + Drawing.drawing.interfaceSizeY / 2 + screen.objYSpace * 3.75,
                        Translation.translate("Page %d of %d", (page + 1), (1 + trails.size() / trails_per_page)));

                next.draw();
                prev.draw();
            }

            if (trails.size() >= trails_per_page * 2)
            {
                first.draw();
                last.draw();
            }
        }
    }
}
