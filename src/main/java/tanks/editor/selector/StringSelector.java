package tanks.editor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectString;

import java.util.Base64;
import java.util.regex.Pattern;

public class StringSelector<T extends GameObject> extends LevelEditorSelector<T>
{
    public String string;
    public boolean encoded;

    @Override
    public void baseInit()
    {
        this.id = "string";
        this.title = "String Selector";
        this.property = "string";

        super.baseInit();
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectString(Game.screen, editor, this);
    }

    @Override
    public String getMetadata()
    {
        // WARNING: DO NOT TRY THIS AT HOME!!!
        return Base64.getEncoder().encodeToString(encodeString(string).getBytes()) + ";" + true;
    }

    @Override
    public void setMetadata(String data)
    {
        String[] stuff = data.split(";");
        this.string = stuff[0];
        if (stuff.length > 1)
            encoded = Boolean.parseBoolean(stuff[1]);

        if (encoded)     // WARNING: DO NOT TRY THIS AT HOME!!!
            this.string = new String(Base64.getDecoder().decode(this.string.getBytes())).replaceAll("-o\\$8", "§");
        else
            this.string = decodeString(string);
    }

    @Override
    public void changeMetadata(int add)
    {

    }

    public static final Pattern allowedReg = Pattern.compile("[^a-zA-Z0-9 !\"@#$%&'()*+\\[\\]<=>:;,\\-./{|}~^âăîşţàçæèéêëïôœùúûüÿáíóñ¡¿äöå]");

    public static String encodeString(String s)
    {
        return allowedReg.matcher(s).replaceAll("-o\\$8");
    }

    public static String decodeString(String s)
    {
        return s.replaceAll("-o\\$8", "§");
    }
}
