package tanks.tank;

import tanks.Player;

/**
 * Represents a player's tank on the server - either a TankPlayer or a TankPlayerRemote
 * (No tanks on the field will implement this if the game is currently a client connected elsewhere)
 */
public interface IServerPlayerTank
{
    Player getPlayer();
}
