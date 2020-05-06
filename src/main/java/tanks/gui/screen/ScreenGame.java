package tanks.gui.screen;

import basewindow.InputCodes;
import basewindow.transformation.RotationAboutPoint;
import basewindow.transformation.Transformation;
import basewindow.transformation.Translation;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.*;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.hotbar.Item;
import tanks.network.Client;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;

public class ScreenGame extends Screen
{
	public boolean playing = false;
	public boolean paused = false;

	public boolean shopScreen = false;
	public int shopPage = 0;
	public int rows = 6;
	public int yoffset = -150;

	public double slant = 0;

	public static boolean finished = false;
	public static double finishTimer = 100;
	public static double finishTimerMax = 100;

	public boolean cancelCountdown = false;
	public String name = null;

	public ArrayList<Item> shop = new ArrayList<Item>();
	public boolean screenshotMode = false;

	public boolean ready = false;

	public static boolean versus = false;
	public String title = "";

	public RotationAboutPoint slantRotation;
	public Translation slantTranslation;
	public ArrayList<Transformation> transformations = new ArrayList<Transformation>();

	@SuppressWarnings("unchecked")
	protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	Button play = new Button(Drawing.drawing.interfaceSizeX-200, Drawing.drawing.interfaceSizeY-50, 350, 40, "Play", new Runnable()
	{
		@Override
		public void run()
		{
			playing = true;
			Game.playerTank.cooldown = 20;
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
					if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size())
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

	Button pause = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button zoom = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
		}
	}
	);

	Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button resumeLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Continue playing", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button closeMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Close menu", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
		}
	}
	);

	Button closeMenuLowerPos = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Close menu", new Runnable()
	{
		@Override
		public void run()
		{
			paused = false;
			Game.playerTank.cooldown = 20;
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

			if (ScreenPartyHost.isServer)
			{
				ready = false;
				readyButton.enabled = true;
				cancelCountdown = true;
				ScreenPartyHost.readyPlayers.clear();
				ScreenPartyHost.includedPlayers.clear();
			}

			if (versus)
			{
				Game.cleanUp();
				new Level(LevelGeneratorVersus.generateLevelString()).loadLevel();
			}
			else
				Game.reset();
		}
	}
	);

	Button edit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Edit the level", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			ScreenLevelBuilder s = new ScreenLevelBuilder(name);
			Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);
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
			Panel.panel.zoomTimer = 0;
			Game.screen = new ScreenPlaySavedLevels();
			ScreenInterlevel.fromSavedLevels = false;

			if (ScreenPartyHost.isServer)
			{
				ScreenPartyHost.readyPlayers.clear();
				ScreenPartyHost.includedPlayers.clear();
				Game.eventsOut.add(new EventReturnToLobby());
			}
		}
	}
	);

	Button quitPartyGame = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back to party", new Runnable()
	{
		@Override
		public void run()
		{
			Game.cleanUp();
			System.gc();
			Panel.panel.zoomTimer = 0;
			Game.screen = ScreenPartyHost.activeScreen;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
			versus = false;
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
			Panel.panel.zoomTimer = 0;
			ScreenPartyLobby.isClient = false;
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

			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}

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

	Button quitCrusadeParty = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Back to party", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.crusadeMode = false;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof TankPlayer && !Game.movables.get(i).destroy)
					((TankPlayer) Game.movables.get(i)).player.remainingLives--;
				else if (Game.movables.get(i) instanceof TankPlayerRemote && !Game.movables.get(i).destroy)
					((TankPlayerRemote) Game.movables.get(i)).player.remainingLives--;
			}

			Panel.panel.zoomTimer = 0;
			Game.cleanUp();
			System.gc();
			Game.screen = ScreenPartyHost.activeScreen;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
			, "Note! All players will lose a life for---quitting in the middle of a level.");

	Button quitCrusadePartyFinalLife = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Back to party", new Runnable()
	{
		@Override
		public void run()
		{
			Crusade.crusadeMode = false;
			Crusade.currentCrusade = null;

			Panel.panel.zoomTimer = 0;
			Game.cleanUp();
			System.gc();
			Game.screen = ScreenPartyHost.activeScreen;
			ScreenPartyHost.readyPlayers.clear();
			ScreenPartyHost.includedPlayers.clear();
			Game.eventsOut.add(new EventReturnToLobby());
		}
	}
			, "Note! All players will lose a life for---quitting in the middle of a level------Since nobody has any other lives left,---the crusade will end!");


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

	public ArrayList<Button> shopItemButtons = new ArrayList<Button>();

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


		slantRotation = new RotationAboutPoint(Game.game.window, 0, 0, 0, 0.5, 0.5, -1);
		slantTranslation = new Translation(Game.game.window, 0, 0, 0);
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

			Game.eventsOut.add(new EventAddShopItem(i, item.name, price));
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

		Game.eventsOut.add(new EventSortShopButtons());
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

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE))
		{
			if (shopScreen)
				shopScreen = false;
			else
				this.paused = !this.paused;

			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_F1))
		{
			this.screenshotMode = !this.screenshotMode;
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_F1);
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_I))
		{
			Drawing.drawing.movingCamera = !Drawing.drawing.movingCamera;
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_I);
		}

		if (!finished)
		{
			Obstacle.draw_size = Math.min(Game.tank_size, Obstacle.draw_size + Panel.frameFrequency);
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
				else if (Crusade.crusadeMode)
				{
					closeMenuLowerPos.update();

					if (Crusade.currentCrusade.finalLife())
						quitCrusadePartyFinalLife.update();
					else
						quitCrusadeParty.update();
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
					if (Crusade.currentCrusade.finalLife())
						quitCrusadeFinalLife.update();
					else
						quitCrusade.update();
				}

				if (!Crusade.crusadeMode)
					resume.update();
				else
					resumeLowerPos.update();
			}

			if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
				return;
		}
		else if (Game.game.window.touchscreen)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100;
			else
				hStep = 100;

			pause.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			pause.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;
			pause.update();

			zoom.posX = pause.posX - hStep;
			zoom.posY = pause.posY + vStep;

			if (Drawing.drawing.enableMovingCamera)
				zoom.update();

			TankPlayer.controlStick.update();
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

				for (int i = shopPage * rows * 3; i < Math.min(shopPage * rows * 3 + rows * 3, shopItemButtons.size()); i++)
					this.shopItemButtons.get(i).update();
			}
			else
			{
				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
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
							if (!ScreenPartyHost.includedPlayers.contains(Game.clientID))
							{
								readyButton.text = "Spectating... (";
								readyButton.enabled = false;
							}

							readyButton.text += ScreenPartyHost.readyPlayers.size() + "/" + ScreenPartyHost.includedPlayers.size() + ")";
						}
						else
						{
							if (!ScreenPartyLobby.includedPlayers.contains(Game.clientID))
							{
								readyButton.text = "Spectating... (";
								readyButton.enabled = false;
							}

							readyButton.text += ScreenPartyLobby.readyPlayers + "/" + ScreenPartyLobby.includedPlayers.size() + ")";
						}

						readyButton.update();
					}
					else
					{
						readyButton.enabled = false;
						readyButton.text = "Starting in " + ((int)(Game.startTime / 100) + 1);
					}
				}

				if (!this.shopItemButtons.isEmpty() && readyButton.enabled)
					enterShop.update();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
				{
					startNow.update();
				}
			}
		}
		else
		{
			playing = true;

			//System.out.println(Panel.frameFrequency);

			Obstacle.draw_size = Math.min(Obstacle.obstacle_size, Obstacle.draw_size);
			ArrayList<Team> aliveTeams = new ArrayList<Team>();

			for (int i = 0; i < Game.effects.size(); i++)
			{
				Game.effects.get(i).update();
			}

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
						else if (m instanceof TankPlayerRemote)
							aliveTeams.add(new Team(((TankPlayerRemote) m).player.clientID.toString()));
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
							Panel.levelPassed = false;

							for (int i = 0; i < Game.players.size(); i++)
							{
								if (Game.players.get(i) != null && Game.players.get(i).tank != null && aliveTeams.contains(Game.players.get(i).tank.team) || (aliveTeams.size() > 0 && aliveTeams.get(0).name.equals(Game.players.get(i).clientID.toString())))
								{
									Panel.levelPassed = true;

									if (Crusade.crusadeMode)
										Panel.winlose = "Battle cleared!";

									break;
								}
							}

							if (aliveTeams.contains(Game.playerTank.team) || (aliveTeams.size() > 0 && aliveTeams.get(0).name.equals(Game.clientID.toString())))
							{
								if (Crusade.crusadeMode)
									Panel.winlose = "Battle cleared!";
								else
									Panel.winlose = "Victory!";

								Panel.win = true;
							}
							else
							{
								if (Crusade.crusadeMode)
									Panel.winlose = "Battle failed!";
								else
									Panel.winlose = "You were destroyed!";

								Panel.win = false;
							}

							if (ScreenPartyHost.isServer)
							{
								Game.cleanUp();

								String s = "**";

								if (aliveTeams.size() > 0)
									s = aliveTeams.get(0).name;

								ScreenPartyHost.readyPlayers.clear();
								Game.eventsOut.add(new EventLevelEnd(s));

								if (Crusade.crusadeMode)
								{
									Crusade.currentCrusade.levelFinished(Panel.levelPassed);
									EventReturnToCrusade e = new EventReturnToCrusade(Crusade.currentCrusade);
									e.execute();
									Game.eventsOut.add(e);

									for (int i = 0; i < Game.players.size(); i++)
									{
										Game.eventsOut.add(new EventUpdateRemainingLives(Game.players.get(i)));
									}
								}
								else
									Game.screen = new ScreenPartyInterlevel();

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

	public void setPerspective()
	{
		if (Game.angledView && Game.framework == Game.Framework.lwjgl)
		{
			if (this.playing && !this.paused && !ScreenGame.finished)
				slant = Math.min(1, slant + 0.01 * Panel.frameFrequency);
			else if (ScreenGame.finished)
				slant = Math.max(0, slant - 0.01 * Panel.frameFrequency);

			this.slantRotation.pitch = this.slant * -Math.PI / 16;
			this.slantTranslation.y = -this.slant * 0.05;

			Game.game.window.transformations.add(this.slantTranslation);
			Game.game.window.transformations.add(this.slantRotation);

			/*Game.game.window.addOffset(true,  -(Game.playerTank.posX + 200 * Math.cos(Game.playerTank.angle)) / Drawing.drawing.sizeX,  -(Game.playerTank.posY + 200 * Math.sin(Game.playerTank.angle)) / Drawing.drawing.sizeY, -0.3);
			Game.game.window.addAngle(true, 0, 0, Game.playerTank.angle + Math.PI / 2);
			Game.game.window.addOffset(true, 0.5, 0.5, 0);
			Game.game.window.addAngle(true, 0, -Math.PI * 0.4, 0);*/

			Game.game.window.loadPerspective();
		}
	}

	@Override
	public void draw()
	{
		this.showDefaultMouse = !(!this.paused && this.playing && Game.angledView);

		this.setPerspective();

		this.drawDefaultBackground();

		Drawing drawing = Drawing.drawing;

		for (int i = 0; i < Game.belowEffects.size(); i++)
			drawables[0].add(Game.belowEffects.get(i));

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			drawables[m.drawLevel].add(m);

			if (m.showName)
				drawables[m.nameTag.drawLevel].add(m.nameTag);
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			drawables[o.drawLevel].add(o);
		}

		for (int i = 0; i < Game.effects.size(); i++)
			drawables[7].add(Game.effects.get(i));

		if (Game.game.window.touchscreen)
		{
			drawables[9].add(TankPlayer.controlStick);

			if (TankPlayer.shootStickEnabled)
				drawables[9].add(TankPlayer.shootStick);
		}

		for (int i = 0; i < this.drawables.length; i++)
		{
			if (i == 5 && Game.enable3d)
			{
				Drawing.drawing.setColor(174, 92, 16);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Obstacle.obstacle_size / 2, 0, drawing.sizeX + Obstacle.obstacle_size * 2, Obstacle.obstacle_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Obstacle.obstacle_size / 2, 0, drawing.sizeX + Obstacle.obstacle_size * 2, Obstacle.obstacle_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(-Obstacle.obstacle_size / 2, drawing.sizeY / 2, 0, Obstacle.obstacle_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX + Obstacle.obstacle_size / 2, drawing.sizeY / 2, 0, Obstacle.obstacle_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
			}

			for (int j = 0; j < this.drawables[i].size(); j++)
			{
				IDrawable d = this.drawables[i].get(j);

				if (d != null)
					d.draw();
			}

			if (i == 9 && (Game.playerTank instanceof IPlayerTank && ((IPlayerTank) Game.playerTank).showTouchCircle()))
			{
				Drawing.drawing.setColor(255, 127, 0, 63);
				Drawing.drawing.fillInterfaceOval(Drawing.drawing.toInterfaceCoordsX(Game.playerTank.posX),
						Drawing.drawing.toInterfaceCoordsY(Game.playerTank.posY),
						((IPlayerTank) Game.playerTank).getTouchCircleSize(), ((IPlayerTank) Game.playerTank).getTouchCircleSize());
			}

			drawables[i].clear();
		}

		if (Game.game.window.touchscreen && TankPlayer.shootStickEnabled)
		{
			double size = TankPlayer.mineButton.sizeX * Obstacle.draw_size / Obstacle.obstacle_size;
			Drawing.drawing.setColor(255, 127, 0, 64);
			Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size, size);

			Drawing.drawing.setColor(255, 255, 0, 64);
			Drawing.drawing.fillInterfaceOval(TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, size * 0.8, size * 0.8);

			//Drawing.drawing.setColor(255, 255, 255, 64);
			//Drawing.drawing.drawInterfaceImage("/mine.png", TankPlayer.mineButton.posX, TankPlayer.mineButton.posY, TankPlayer.mineButton.sizeX, TankPlayer.mineButton.sizeY);
		}

		if (!this.showDefaultMouse)
			Panel.panel.drawMouseTarget(true);

		if (Game.framework == Game.Framework.lwjgl)
		{
			Game.game.window.transformations.clear();
			Game.game.window.loadPerspective();
		}

		//Game.game.window.setAngles(0, 0, 0);
		//Game.game.window.setOffsets(0,  0, 0);

		if (!paused && Game.game.window.touchscreen)
		{
			pause.draw();
			Drawing.drawing.drawInterfaceImage("/pause.png", pause.posX, pause.posY, 40, 40);

			if (Drawing.drawing.enableMovingCamera)
			{
				zoom.draw();

				if (Drawing.drawing.movingCamera)
					Drawing.drawing.drawInterfaceImage("/zoom_out.png", zoom.posX, zoom.posY, 40, 40);
				else
					Drawing.drawing.drawInterfaceImage("/zoom_in.png", zoom.posX, zoom.posY, 40, 40);
			}
		}

		if (!playing)
		{
			if (Crusade.crusadeMode)
			{
				Drawing.drawing.setColor(0, 0, 0, 127);
				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Battle " + (Crusade.currentCrusade.currentLevel + 1));
			}

			if (!title.equals(""))
			{
				Drawing.drawing.setColor(0, 0, 0, 127);
				Drawing.drawing.setInterfaceFontSize(100);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, title);
			}

			if (shopScreen)
			{
				Drawing.drawing.setInterfaceFontSize(24);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Shop");

				this.exitShop.draw();

				if (shopItemButtons.size() > (1 + shopPage) * rows * 3)
					next.draw();

				if (shopPage > 0)
					this.previous.draw();

				for (int i = Math.min(shopPage * rows * 3 + rows * 3, shopItemButtons.size()) - 1; i >= shopPage * rows * 3; i--)
					shopItemButtons.get(i).draw();
			}
			else
			{
				if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
					play.draw();
				else
					readyButton.draw();

				if (!this.shopItemButtons.isEmpty() && this.readyButton.enabled)
					enterShop.draw();

				if (ScreenPartyHost.isServer && this.cancelCountdown)
					startNow.draw();

				if ((ScreenPartyHost.isServer || ScreenPartyLobby.isClient || Game.autostart) && !cancelCountdown)
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
				else if (Crusade.crusadeMode)
				{
					closeMenuLowerPos.draw();

					if (Crusade.currentCrusade.finalLife())
						quitCrusadePartyFinalLife.draw();
					else
						quitCrusadeParty.draw();
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
					if (Crusade.currentCrusade.finalLife())
						quitCrusadeFinalLife.draw();
					else
						quitCrusade.draw();
				}

				if (!Crusade.crusadeMode)
					resume.draw();
				else
					resumeLowerPos.draw();
			}

			Drawing.drawing.setInterfaceFontSize(24);
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
