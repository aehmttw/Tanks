package tanks.tank;

import tanks.Player;

public class TankPlayerBot extends TankPurple implements IServerPlayerTank
{
    public Player player;

    public TankPlayerBot(double x, double y, double angle, Player p)
    {
        super(p.username, x, y, angle);
        this.colorR = 0;
        this.colorG = 150;
        this.colorB = 255;
        this.showName = true;
        this.player = p;

        this.bulletItem.networkIndex = 0;
        this.mineItem.networkIndex = -1;
    }

    @Override
    public Player getPlayer()
    {
        return this.player;
    }
}
