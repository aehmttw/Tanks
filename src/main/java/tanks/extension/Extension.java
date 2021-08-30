package tanks.extension;

import basewindow.BaseFile;
import tanks.Game;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.jar.JarFile;

public class Extension
{
    public final String name;
    public JarFile jarFile;

    public Extension(String name)
    {
        this.name = name;
    }

    // Register things like tanks, obstacles, items, or network events here
    public void setUp()
    {

    }

    // Use the methods below like registerImage to register resources your extension uses
    public void loadResources()
    {

    }

    public void registerImage(String image)
    {
        if (this.jarFile == null)
            return;

        String path = "images/" + image;
        try
        {
            Game.game.window.createImage(path, this.jarFile.getInputStream(this.jarFile.getEntry(path)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void registerSound(String sound)
    {
        if (this.jarFile == null)
            return;

        String path = "sounds/" + sound;
        try
        {
            Game.game.window.soundPlayer.createSound(path, this.jarFile.getInputStream(this.jarFile.getEntry(path)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void registerMusic(String music)
    {
        if (this.jarFile == null)
            return;

        String path = "music/" + music;
        try
        {
            Game.game.window.soundPlayer.createMusic(path, this.jarFile.getInputStream(this.jarFile.getEntry(path)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void registerMusicAsync(String music)
    {
        if (this.jarFile == null)
            return;

        String path = "music/" + music;
        try
        {
            Game.game.window.soundPlayer.loadMusic(path, this.jarFile.getInputStream(this.jarFile.getEntry(path)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getFileContents(String file)
    {
        try
        {
            ArrayList<String> strings = new ArrayList<>();
            if (this.jarFile == null)
            {
                return Game.game.fileManager.getInternalFileContents("/" + file);
            }
            else
            {
                InputStream i = this.jarFile.getInputStream(this.jarFile.getEntry(file));
                Scanner s = new Scanner(new InputStreamReader(i));

                while (s.hasNextLine())
                    strings.add(s.nextLine());

                i.close();
                s.close();

                return strings;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
