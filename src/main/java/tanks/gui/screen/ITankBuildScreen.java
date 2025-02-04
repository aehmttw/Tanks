package tanks.gui.screen;

import tanks.tank.TankPlayer;
import tanks.tankson.Pointer;

public interface ITankBuildScreen
{
    default Pointer<TankPlayer.ShopTankBuild> addTank(TankPlayer.ShopTankBuild t)
    {
        return addTank(t, true);
    }

    Pointer<TankPlayer.ShopTankBuild> addTank(TankPlayer.ShopTankBuild t, boolean select);

    void removeTank(TankPlayer t);

    void refreshTanks(TankPlayer t);
}
