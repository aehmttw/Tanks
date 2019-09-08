package tanks.gui.screen;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.*;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.hotbar.Item;
import tanks.network.Client;
import tanks.obstacle.Obstacle;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankRemote;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ScreenGame extends Screen
{
	public boolean playing = false;
	public boolean paused = false;

	public boolean shopScreen = false;
	public int shopPage = 0;
	public int rows = 6;
	public int yoffset = -150;

	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;

	public boolean cancelCountdown = false;
	public String name = null;

	public ArrayList<Item> shop = new ArrayList<Item>();
	public boolean screenshotMode = false;

	public boolean ready = false;

	@SuppressWarnings("unchecked")
	protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	
	Button play = new Button(Drawing.drawing.interfaceSizeX-200, Drawing.drawing.interfaceSizeY-50, 350, 40, "Play", new Runnable()
	{
		@Override
		public void run() 
		{
			playing = true;
			Game.player.cooldown = 20;
		}
	}
			);

	Button readyButton = new Button(Drawing.drawing.interfaceSizeX-200, Drawing.drawing.interfaceSizeY-50, 350, 40, "Ready", new Runnable()
	{
		@Override
		public void run() 
		{
			if (ScreenPartyLobby.isClient)
				Game.eventsOut.add(new EventPlayerReady());
			else
			{
				ScreenPartyHost.readyPlayers.add(Game.clientID);
				Game.eventsOut.add(new EventUpdateReadyCount(ScreenPartyHost.readyPlayers.size()));

				//synchronized(ScreenPartyHost.server.connections)
				{
					if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.server.connections.size() + 1)
					{
						Game.eventsOut.add(new EventBeginLevelCountdown());
						cancelCountdown = false;
					}
				}
			}
			ready = true;
		}
	}
			);

	Button startNow = new Button(Drawing.drawing.interfaceSizeX-580, Drawing.drawing.interfaceSizeY-50, 350, 40, "Start now", new Runnable()
	{
		@Override
		public void run() 
		{
			if (ScreenPartyHost.isServer)
			{
				Game.eventsOut.add(new EventBeginLevelCountdown());
				cancelCountdown = false;
			}
			ready = true;
		}
	}
			);


	Button enterShop = new Button(Drawing.drawing.interfaceSizeX-200, Drawing.drawing.interfaceSizeY-110, 350, 40, "Shop", new Runnable()
	{
		@Override
		public void run() 
		{
			cancelCountdown = true;
			shopScreen = true;
		}
	}
			);

	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run() 
		{
			paused = false;
			Game.player.cooldown = 20;
		}
	}
			);

	Button resumeLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run() 
		{
			paused = false;
			Game.player.cooldown = 20;
		}
	}
			);

	Button closeMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Close menu", new Runnable()
	{
		@Override
		public void run() 
		{
			paused = false;
			Game.player.cooldown = 20;
		}
	}
			);

	Button closeMenuLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Close menu", new Runnable()
	{
		@Override
		public void run() 
		{
			paused = false;
			Game.player.cooldown = 20;
		}
	}
			);

	Button newLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Generate a new level", new Runnable()
	{
		@Override
		public void run() 
		{
			playing = false;
			Game.startTime = 400;
			paused = false;
			Game.reset();

			if (ScreenPartyHost.isServer)
			{
				ready = false;
				readyButton.enabled = true;
				cancelCountdown = true;
				ScreenPartyHost.readyPlayers.clear();
			}
		}
	}
			);

	Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Edit the level", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
			ScreenLevelBuilder s = new ScreenLevelBuilder(name);
			Game.loadLevel(new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);
			Game.screen = s;
		}
	}
			);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
		}
	}
			);
	
	Button quitHigherPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
			ScreenInterlevel.tutorial = false;
		}
	}
			);

	Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Back to my levels", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.cleanUp();
			System.gc();
			Game.screen = new ScreenPlaySavedLevels();
			ScreenInterlevel.fromSavedLevels = false;

			if (ScreenPartyHost.isServer)
			{
				ScreenPartyHost.readyPlayers.clear();
				Game.eventsOut.add(new EventReturnToLobby());
			}
		}
	}
			);

	Button quitPartyGame = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.cleanUp();
			System.gc();
			Game.screen = ScreenPartyHost.activeScreen;	
			ScreenPartyHost.readyPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
			);

	Button exitParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Leave party", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.cleanUp();
			System.gc();
			Game.screen = new ScreenJoinParty();
			Client.handler.ctx.close();
			ScreenPartyLobby.connections.clear();
		}
	}
			);

	Button quitCrusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			Crusade.currentCrusade.remainingLives--;
			Game.exitToTitle();
		}
	}
	, "Note! You will lose a life for quitting---in the middle of a level------You will be able to return to the crusade---through the crusade button on---the play screen.");

	Button quitCrusadeFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Quit to title", new Runnable()
	{
		@Override
		public void run() 
		{
			Crusade.crusadeMode = false;
			Crusade.currentCrusade = null;
			Game.exitToTitle();
		}
	}
	, "Note! You will lose a life for quitting---in the middle of a level------Since you do not have any other lives left,---your progress will be lost!");

	Button exitShop = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Exit shop", new Runnable()
	{
		@Override
		public void run() 
		{
			shopScreen = false;
		}
	}
			);

	Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run() 
		{
			shopPage++;
		}
	}
			);

	Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run() 
		{
			shopPage--;
		}
	}
			);

	ArrayList<Button> shopItemButtons = new ArrayList<Button>();

	public ScreenGame()
	{
		Game.startTime = 400;

		if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
			cancelCountdown = true;

		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		for (int i = 0; i < this.drawables.length; i++)
		{
			this.drawables[i] = new ArrayList<IDrawable>();
		}
	}

	public ScreenGame(String s)
	{
		this();
		this.name = s;
	}

	public ScreenGame(ArrayList<Item> shop)
	{
		this();
		this.shop = shop;
		for (int i = 0; i < this.shop.size(); i++)
		{
			final int j = i;
			Item item = this.shop.get(j);

			String price = "Price: " + item.price + " ";
			if (item.price == 0)
				price = "Price: Free!";
			else if (item.price == 1)
				price += "coin";
			else
				price += "coins";

			this.shopItemButtons.add(new Button(0, 0, 350, 40, item.name, new Runnable()
			{
				@Override
				public void run() 
				{
					int pr = shop.get(j).price;
					if (Panel.panel.hotbar.currentCoins.coins >= pr)
					{
						if (Panel.panel.hotbar.currentItemBar.addItem(shop.get(j)))
							Panel.panel.hotbar.currentCoins.coins -= pr;
					}
				}
			}, price
					));
		}

		for (int i = 0; i < shopItemButtons.size(); i++)
		{
			int page = i / (rows * 3);
			int offset = 0;

			if (page * rows * 3 + rows < shopItemButtons.size())
				offset = -190;

			if (page * rows * 3 + rows * 2 < shopItemButtons.size())
				offset = -380;

			shopItemButtons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

			if (i / rows % 3 == 0)
				shopItemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
			else if (i / rows % 3 == 1)
				shopItemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
			else
				shopItemButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;
		}
	}

	@Override
	public void update()
	{
		if (ScreenPartyLobby.isClient)
			ScreenPartyLobby.chatbox.update(false);
		else if (ScreenPartyHost.isServer)
			ScreenPartyHost.chatbox.update(false);

		Panel.panel.hotbar.update();

		if (Game.enable3d)
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if (o.replaceTiles)
					o.postOverride();
			}

		if (Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_ESCAPE))
		{
			if (!Panel.pausePressed)
			{
				if (shopScreen)
					shopScreen = false;
				else
					this.paused = !this.paused;
			}

			Panel.pausePressed = true;
		}
		else
			Panel.pausePressed = false;

		if (Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_F1))
		{
			this.screenshotMode = !this.screenshotMode;
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_F1);
		}

		if (Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_I))
		{
			Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_I);
		}

		if (paused)
		{
			if (ScreenPartyLobby.isClient)
			{
				closeMenuLowerPos.update();
				exitParty.update();
			}
			else if (ScreenPartyHost.isServer)
			{
				if (ScreenInterlevel.fromSavedLevels)
				{
					closeMenuLowerPos.update();
					back.update();
				}
				else
				{
					closeMenu.update();
					newLevel.update();
					quitPartyGame.update();
				}
			}
			else if (ScreenInterlevel.fromSavedLevels)
			{
				resumeLowerPos.update();
				back.update();
			}
			else if (ScreenInterlevel.tutorialInitial)
			{
				resumeLowerPos.update();
			}
			else if (ScreenInterlevel.tutorial)
			{
				resumeLowerPos.update();
				quitHigherPos.update();
			}
			else
			{
				if (name == null)
				{
					if (!Crusade.crusadeMode)
						newLevel.update();
				}
				else
					edit.update();

				if (!Crusade.crusadeMode)
					quit.update();
				else
				{
					if (Crusade.currentCrusade.remainingLives > 1)
						quitCrusade.update();
					else
						quitCrusadeFinalLife.update();
				}

				if (!Crusade.crusadeMode)
					resume.update();
				else
					resumeLowerPos.update();
			}

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				return;
		}

		if (!playing && Game.startTime >= 0)
		{
			if (shopScreen)
			{
				Panel.panel.hotbar.hidden = false;
				Panel.panel.hotbar.hideTimer = 100;

				this.exitShop.update();

				if (shopItemButtons.size() > (1 + shopPage) * rows * 3)
					next.update();

				if (shopPage > 0)
					this.previous.update();

				for (int i = 0; i < this.shopItemButtons.size(); i++)
					this.shopItemButtons.get(i).update();
			}
			else
			{
				if (!this.shop.isEmpty())
					enterShop.update();

				if (Game.autostart && !cancelCountdown)
					Game.startTime -= Panel.frameFrequency;

				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
					play.update();
				else
				{
					if (this.cancelCountdown)
					{
						readyButton.enabled = !this.ready;

						if (this.ready)
							readyButton.text = "Waiting... (";
						else
							readyButton.text = "Ready (";

						if (ScreenPartyHost.isServer)
						{
							//synchronized(ScreenPartyHost.server.connections)
							{
								readyButton.text += ScreenPartyHost.readyPlayers.size() + "/" + (ScreenPartyHost.server.connections.size() + 1) + ")";
							}
						}
						else
							readyButton.text += ScreenPartyLobby.readyPlayers + "/" + ScreenPartyLobby.connections.size() + ")";

						readyButton.update();
					}
					else
					{
						readyButton.enabled = false;
						readyButton.text = "Starting in " + ((int)(Game.startTime / 100) + 1);
					}
				}

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.update();
			}

			if (!finished)
			{
				Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size + Panel.frameFrequency);
			}
		}
		else
		{
			playing = true;

			//System.out.println(Panel.frameFrequency);

			Obstacle.draw_size = Math.min(Obstacle.obstacle_size, Obstacle.draw_size);
			ArrayList<Team> aliveTeams = new ArrayList<Team>();

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				m.update();

				if (m instanceof Tank)
				{
					if (m.team == null)
					{
						if (m instanceof TankPlayer)
							aliveTeams.add(new Team(Game.clientID.toString()));
						else if (m instanceof TankRemote && ((TankRemote)m).tank != null && ((TankRemote)m).tank instanceof TankPlayer)
							aliveTeams.add(new Team(((TankPlayer)((TankRemote)m).tank).clientID.toString()));
						else
							aliveTeams.add(new Team("*"));
					}
					else if (!aliveTeams.contains(m.team))
						aliveTeams.add(m.team);
				}
			}

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if (o.update)
					o.update();
			}


			for (int i = 0; i < Game.effects.size(); i++)
			{
				Game.effects.get(i).update();
			}

			for (int i = 0; i < Game.belowEffects.size(); i++)
			{
				Game.belowEffects.get(i).update();
			}

			Panel.panel.hotbar.update();

			if (aliveTeams.size() <= 1)
			{
				ScreenGame.finished = true;
				Game.bulletLocked = true;

				if (ScreenGame.finishTimer > 0)
				{
					ScreenGame.finishTimer -= Panel.frameFrequency;
					if (ScreenGame.finishTimer < 0)
						ScreenGame.finishTimer = 0;
				}
				else
				{
					boolean noMovables = true;

					for (int m = 0; m < Game.movables.size(); m++)
					{
						Movable mo = Game.movables.get(m);
						if (mo instanceof Bullet || mo instanceof Mine)
						{
							noMovables = false;
							mo.destroy = true;
						}
					}

					if (Game.effects.size() <= 0 && noMovables)
					{
						Obstacle.draw_size = Math.max(0, Obstacle.draw_size - Panel.frameFrequency);
						for (int i = 0; i < Game.movables.size(); i++)
							Game.movables.get(i).destroy = true;

						if (Obstacle.draw_size <= 0)
						{
							if (aliveTeams.contains(Game.player.team) || (aliveTeams.size() > 0 && aliveTeams.get(0).name.equals(Game.clientID.toString())))
							{
								Panel.winlose = "Victory!";
								Panel.win = true;
							}
							else
							{
								Panel.winlose = "You were destroyed!";
								Panel.win = false;
							}

							if (ScreenPartyHost.isServer)
							{
								Game.cleanUp();
								Game.screen = ScreenPartyHost.activeScreen;

								String s = "**";
								if (aliveTeams.size() > 0)
									s = aliveTeams.get(0).name;

								ScreenPartyHost.readyPlayers.clear();
								Game.eventsOut.add(new EventLevelEnd(s));

								System.gc();
							}
							else if (!Game.currentLevel.remote)
							{
								if (name != null)
									Game.exit(name);
								else
									Game.exit();
							}
						}
					}
				}
			}
			else
				Game.bulletLocked = false;
		}

		for (int i = 0; i < Game.removeMovables.size(); i++)
			Game.movables.remove(Game.removeMovables.get(i));

		for (int i = 0; i < Game.removeObstacles.size(); i++)
			Game.obstacles.remove(Game.removeObstacles.get(i));

		for (int i = 0; i < Game.removeEffects.size(); i++)
		{
			Effect e = Game.removeEffects.get(i);
			Game.effects.remove(e);
			Game.recycleEffects.add(e);

		}

		for (int i = 0; i < Game.removeBelowEffects.size(); i++)
		{
			Effect e = Game.removeBelowEffects.get(i);
			Game.belowEffects.remove(e);
			Game.recycleEffects.add(e);
		}

		Game.removeMovables.clear();
		Game.removeObstacles.clear();
		Game.removeEffects.clear();
		Game.removeBelowEffects.clear();
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		for (int i = 0; i < Game.belowEffects.size(); i++)
			drawables[0].add(Game.belowEffects.get(i));

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);
			drawables[m.drawLevel].add(m);
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			drawables[o.drawLevel].add(o);
		}

		for (int i = 0; i < Game.effects.size(); i++)
			drawables[7].add(Game.effects.get(i));


		for (int i = 0; i < this.drawables.length; i++)
		{
			for (int j = 0; j < this.drawables[i].size(); j++)
			{
				IDrawable d = this.drawables[i].get(j);

				if (d != null)
					d.draw();
			}

			drawables[i].clear();
		}

		if (!playing) 
		{
			if (Crusade.crusadeMode)
			{
				Drawing.drawing.setColor(0, 0, 0, 127);
				Drawing.drawing.setFontSize(100);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Battle " + (Crusade.currentCrusade.currentLevel + 1));
			}

			if (shopScreen)
			{		
				Drawing.drawing.setFontSize(24);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Shop");
	
				this.exitShop.draw();

				if (shopItemButtons.size() > (1 + shopPage) * rows * 3)
					next.draw();

				if (shopPage > 0)
					this.previous.draw();

				for (int i = 0; i < this.shopItemButtons.size(); i++)
					this.shopItemButtons.get(i).draw();
			}
			else
			{
				if (!this.shop.isEmpty())
					enterShop.draw();

				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
					play.draw();
				else
					readyButton.draw();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.draw();

				if (Game.autostart && !cancelCountdown)
				{
					Drawing.drawing.setColor(127, 127, 127);
					Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 35, 320, 3);
					Drawing.drawing.setColor(255, 127, 0);
					Drawing.drawing.fillInterfaceProgressRect(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 35, 320, 3, Game.startTime / 400);
				}
			}
		}

		Panel.panel.hotbar.draw();

		if (paused && !screenshotMode)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			if (ScreenPartyLobby.isClient)
			{
				closeMenuLowerPos.draw();
				exitParty.draw();
			}
			else if (ScreenPartyHost.isServer)
			{
				if (ScreenInterlevel.fromSavedLevels)
				{
					closeMenuLowerPos.draw();
					back.draw();
				}
				else
				{
					closeMenu.draw();
					newLevel.draw();
					quitPartyGame.draw();
				}
			}
			else if (ScreenInterlevel.fromSavedLevels)
			{
				resumeLowerPos.draw();
				back.draw();
			}
			else if (ScreenInterlevel.tutorialInitial)
			{
				resumeLowerPos.draw();
			}
			else if (ScreenInterlevel.tutorial)
			{
				resumeLowerPos.draw();
				quitHigherPos.draw();
			}
			else
			{
				if (name == null)
				{
					if (!Crusade.crusadeMode)
						newLevel.draw();
				}
				else
					edit.draw();

				if (!Crusade.crusadeMode)
				{
					quit.draw();
				}
				else
				{
					if (Crusade.currentCrusade.remainingLives > 1)
						quitCrusade.draw();
					else
						quitCrusadeFinalLife.draw();
				}	

				if (!Crusade.crusadeMode)
					resume.draw();
				else
					resumeLowerPos.draw();
			}

			Drawing.drawing.setColor(0, 0, 0);

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Game paused");
			else
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Game menu");
		}

		Drawing.drawing.setInterfaceFontSize(24);
		if (ScreenPartyLobby.isClient)
		{
			ScreenPartyLobby.chatbox.draw(false);
			long time = System.currentTimeMillis();
			for (int i = 0; i < ScreenPartyLobby.chat.size(); i++)
			{
				ChatMessage c = ScreenPartyLobby.chat.get(i);
				if (time - c.time <= 30000 || ScreenPartyLobby.chatbox.selected)
				{
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
				}
			}
		}
		else if (ScreenPartyHost.isServer)
		{
			ScreenPartyHost.chatbox.draw(false);
			long time = System.currentTimeMillis();
			for (int i = 0; i < ScreenPartyHost.chat.size(); i++)
			{
				ChatMessage c = ScreenPartyHost.chat.get(i);
				if (time - c.time <= 30000 || ScreenPartyHost.chatbox.selected)
				{
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
				}
			}
		}

	}

}
