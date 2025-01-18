package tanks.translation;

import basewindow.BaseFile;
import tanks.Game;

import java.util.ArrayList;
import java.util.HashMap;

public class Translation
{
    public static Translation currentTranslation = null;

    public String name;
    public String fileName;
    public HashMap<String, String> translations = new HashMap<>();

    public void initialize(ArrayList<String> translations)
    {
        boolean first = true;

        for (String s: translations)
        {
            if (first)
            {
                first = false;
                this.name = s;
                continue;
            }

            String[] parts = s.split("=");

            if (parts.length > 1 && !parts[1].isEmpty())
                this.translations.put(parts[0], parts[1]);
        }
    }

    public Translation(String fileName)
    {
        if (fileName.startsWith("internal/"))
            fileName = fileName.substring(9);

        this.fileName = "internal/" + fileName;

        ArrayList<String> translations = Game.game.fileManager.getInternalFileContents("/translations/" + fileName);
        this.initialize(translations);
    }

    public Translation(BaseFile f)
    {
        this.fileName = f.path;

        ArrayList<String> texts = new ArrayList<>();

        try
        {
            f.startReading();

            while (f.hasNextLine())
            {
                texts.add(f.nextLine());
            }

            f.stopReading();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        this.initialize(texts);
    }


    public String getTranslation(String s)
    {
        String t = translations.get(s);

        if (t == null)
            return s;

        return t;
    }

    public String getTranslation(String s, Object... objects)
    {
        String t = translations.get(s);

        if (t == null)
            t = s;

        return String.format(t, objects);
    }

    public static String translate(String s)
    {
        if (currentTranslation == null)
            return s;

        return currentTranslation.getTranslation(s);
    }

    public static String translate(String s, Object... objects)
    {
        if (currentTranslation == null)
            return String.format(s, objects);

        return currentTranslation.getTranslation(s, objects);
    }

    public static void setCurrentTranslation(String s)
    {
        if (s.equals("null"))
            currentTranslation = null;
        else if (s.startsWith("internal/"))
            currentTranslation = new Translation(s);
        else
        {
            BaseFile f = Game.game.fileManager.getFile(s);

            if (f.exists())
                currentTranslation = new Translation(f);
        }
    }
}
