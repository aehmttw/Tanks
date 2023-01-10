package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.tank.TankPlayer;

public class ScreenInfo extends Screen implements IConditionalOverlayScreen, IDarkScreen
{
    public Screen previous;
    public String title;
    public String[] text;

    public ScreenInfo(Screen screen, String title, String[] text)
    {
        if (screen != null)
        {
            this.enableMargins = screen.enableMargins;
            this.previous = screen;
            this.music = previous.music;
            this.musicID = previous.musicID;
        }

        this.title = title;
        this.text = text;
    }

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            if (Game.screen instanceof ScreenGame && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
                ((ScreenGame) Game.screen).overlay = null;

            if (previous != null)
                Game.screen = previous;
        }
    }
    );

    @Override
    public void update()
    {
        Game.game.window.showKeyboard = false;
        back.update();
    }

    @Override
    public void draw()
    {
        if (this.previous != null)
            this.previous.draw();

        Drawing.drawing.setColor(0, 0, 0, 64);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 700 * this.objWidth / 350, 400 * this.objHeight / 40);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 680 * this.objWidth / 350, 380 * this.objHeight / 40);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 2.5, this.title);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(255, 255, 255);

        int diff = 300;

        if (Drawing.drawing.interfaceScaleZoom > 1)
            diff = 390;

        for (int i = 0; i < text.length; i++)
        {
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 - diff, Drawing.drawing.interfaceSizeY / 2 + (i - (text.length - 1) / 2.0) * this.objYSpace / 2, this.text[i], false);
        }

        back.draw();
    }

    @Override
    public double getOffsetX()
    {
        return previous.getOffsetX();
    }

    @Override
    public double getOffsetY()
    {
        return previous.getOffsetY();
    }

    @Override
    public double getScale()
    {
        return previous.getScale();
    }

    @Override
    public boolean isOverlayEnabled()
    {
        if (previous instanceof IConditionalOverlayScreen)
            return ((IConditionalOverlayScreen) previous).isOverlayEnabled();

        return previous instanceof ScreenGame || previous instanceof ILevelPreviewScreen || previous instanceof IOverlayScreen;
    }

    @Override
    public void setupLayoutParameters()
    {
        Drawing.drawing.interfaceScaleZoom = Drawing.drawing.interfaceScaleZoomDefault;
        Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
        Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;
        this.centerX = Drawing.drawing.interfaceSizeX / 2;
        this.centerY = Drawing.drawing.interfaceSizeY / 2;
    }
}
