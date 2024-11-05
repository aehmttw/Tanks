package tanks.gui.screen;

import tanks.tank.TankAIControlled;
import tanks.tankson.Pointer;

public interface ITankScreen
{
    Pointer<TankAIControlled> addTank(TankAIControlled t);

    void removeTank(TankAIControlled t);

    void refreshTanks(TankAIControlled t);
}
