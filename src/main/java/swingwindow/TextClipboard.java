package swingwindow;

import java.awt.*;
import java.awt.datatransfer.*;

public class TextClipboard implements ClipboardOwner
{
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {

    }

    public void setClipboard(String string)
    {
        StringSelection stringSelection = new StringSelection(string);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    public String getClipboard()
    {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText)
        {
            try
            {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return result;
    }

}