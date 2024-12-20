package tanks.gui.screen.leveleditor.selector;

import tanks.Game;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.OverlaySelectString;

import java.util.Base64;
import java.util.regex.Pattern;

public class SelectorText<T extends GameObject> extends LevelEditorSelector<T, String>
{
    public boolean encoded;

    @Override
    public void baseInit()
    {
        this.id = "string";
        this.title = "String Selector";

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
        // Comment from aehmttw: this could probably be done more reasonably if/when obstacles are tanksonified
        // lancelot: yes please this is cursed lol
        return Base64.getEncoder().encodeToString(encodeString(getObject()).getBytes()) + ";" + true;
    }

    @Override
    public void setMetadata(String data)
    {
        String[] stuff = data.split(";");
        String string = stuff[0];
        if (stuff.length > 1)
            encoded = Boolean.parseBoolean(stuff[1]);

        if (encoded)     // WARNING: DO NOT TRY THIS AT HOME!!!
            string = new String(Base64.getDecoder().decode(string.getBytes())).replaceAll("-o\\$8", "§");
        else
            string = decodeString(string);
        setObject(string);
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

    public String string()
    {
        return getObject();
    }
}
