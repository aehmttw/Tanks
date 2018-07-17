package tanks;

import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

public class ScreenSavedLevels extends Screen
{
	public static final String levelDir = Game.directoryPath + "/levels";
	
	int rows = 6;
	int yoffset = -150;
	int page = 0;

	Button quit = new Button(350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			Game.exitToTitle();
		}
	}
			);
	
	Button newLevel = new Button(350, 40, "New level", new Runnable()
	{
		@Override
		public void run() 
		{
			/*String name = "level_";
			int number = buttons.size() + 1;
			
			if (number < 10)
				name += "0";
			
			if (number < 100)
				name += "0";
			
			name += number;*/
			String name = System.currentTimeMillis() + ".tanks";
			
			Game.screen = new ScreenLevelBuilder(name);
		}
	}
			);
	
	Button next = new Button(350, 40, "Next page", new Runnable()
	{
		@Override
		public void run() 
		{
			page++;
		}
	}
			);
	
	Button previous = new Button(350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run() 
		{
			page--;
		}
	}
			);
	
	ArrayList<Button> buttons = new ArrayList<Button>();
	
	
	public ScreenSavedLevels()
	{
		if (!Files.exists(Paths.get(Game.homedir + levelDir)))
		{
			new File(Game.homedir + levelDir).mkdir();
		}
		
		ArrayList<Path> levels = new ArrayList<Path>();

		try
		{
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(Game.homedir + levelDir));
						
			Iterator<Path> irritator = ds.iterator();
			while (irritator.hasNext())
			{
				levels.add(irritator.next());
			}
			
			ds.close();
		}
		catch (IOException e)
		{
			Game.exitToCrash(e);
		}
		
		for (Path l: levels)
		{
			String[] pathSections = l.toString().replaceAll("\\\\", "/").split("/");
			
			buttons.add(new Button(350, 40, pathSections[pathSections.length - 1].split("\\.")[0], new Runnable()
			{
				@Override
				public void run() 
				{
					Game.loadLevel(l);
					Game.screen = new ScreenLevelBuilder(pathSections[pathSections.length - 1]);
				}
			}
					));
		}
		
		
	}

	@Override
	public void update()
	{
		int offset = 0;
		
		if (page * rows * 3 + rows < buttons.size())
			offset = -190;
		
		if (page * rows * 3 + rows * 2 < buttons.size())
			offset = -380;
			
		for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows, buttons.size()); i++)
		{
			buttons.get(i).update(Window.sizeX / 2 + offset, Window.sizeY / 2 + yoffset + (i - page * rows * 3) * 60);
		}
		
		for (int i = page * rows * 3 + rows; i < Math.min(page * rows * 3 + rows * 2, buttons.size()); i++)
		{
			buttons.get(i).update(Window.sizeX / 2 + offset + 380, Window.sizeY / 2 + yoffset + (i - page * rows * 3 - rows) * 60);
		}
		
		for (int i = page * rows * 3 + rows * 2; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
		{
			buttons.get(i).update(Window.sizeX / 2 + offset + 380 * 2, Window.sizeY / 2 + yoffset + (i - page * rows * 3 - rows * 2) * 60);
		}
		
		quit.update(Window.sizeX / 2 - 190, Window.sizeY / 2 + 300);
		newLevel.update(Window.sizeX / 2 + 190, Window.sizeY / 2 + 300);
		
		if (page > 0)
			previous.update(Window.sizeX / 2 - 190, Window.sizeY / 2 + 240);
		
		if (buttons.size() > (1 + page) * rows * 3)
			next.update(Window.sizeX / 2 + 190, Window.sizeY / 2 + 240);
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);
		
		int offset = 0;
		
		if (page * rows * 3 + rows < buttons.size())
			offset = -190;
		
		if (page * rows * 3 + rows * 2 < buttons.size())
			offset = -380;
			
		for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows, buttons.size()); i++)
		{
			buttons.get(i).draw(g, Window.sizeX / 2 + offset, Window.sizeY / 2 + yoffset + (i - page * rows * 3) * 60);
		}
		
		for (int i = page * rows * 3 + rows; i < Math.min(page * rows * 3 + rows * 2, buttons.size()); i++)
		{
			buttons.get(i).draw(g, Window.sizeX / 2 + offset + 380, Window.sizeY / 2 + yoffset + (i - page * rows * 3 - rows) * 60);
		}
		
		for (int i = page * rows * 3 + rows * 2; i < Math.min(page * rows * 3 + rows * 3, buttons.size()); i++)
		{
			buttons.get(i).draw(g, Window.sizeX / 2 + offset + 380 * 2, Window.sizeY / 2 + yoffset + (i - page * rows * 3 - rows * 2) * 60);
		}
	
		quit.draw(g, Window.sizeX / 2 - 190, Window.sizeY / 2 + 300);
		newLevel.draw(g, Window.sizeX / 2 + 190, Window.sizeY / 2 + 300);

		Window.drawText(g, Window.sizeX / 2, Window.sizeY / 2 - 210, "My levels");
		
		if (page > 0)
			previous.draw(g, Window.sizeX / 2 - 190, Window.sizeY / 2 + 240);
		
		if (buttons.size() > (1 + page) * rows * 3)
			next.draw(g, Window.sizeX / 2 + 190, Window.sizeY / 2 + 240);

	}

}
