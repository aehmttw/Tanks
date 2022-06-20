package tanks.gui.screen;

import tanks.tank.TankAIControlled;

public interface ITankScreen
{
    void addTank(TankAIControlled t);

    void removeTank(TankAIControlled t);

    void refreshTanks();
}
