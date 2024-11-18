package tanks.gui.screen;

import tanks.tank.TankAIControlled;
import tanks.tankson.Pointer;

public interface ITankScreen
{
    default Pointer<TankAIControlled> addTank(TankAIControlled t)
    {
        return addTank(t, true);
    }

    Pointer<TankAIControlled> addTank(TankAIControlled t, boolean select);

    void removeTank(TankAIControlled t);

    void refreshTanks(TankAIControlled t);
}
