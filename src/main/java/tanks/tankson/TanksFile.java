package tanks.tankson;

import basewindow.ComputerFile;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Desktop .tanks saves: plain text by default, or an automatically detected ObjectBuffer container. */
public class TanksFile extends ComputerFile
{
    private final BooleanSupplier reducedDiskSpace;
    private StringWriter compactText;

    public TanksFile(String path, BooleanSupplier reducedDiskSpace)
    {
        super(path);
        this.reducedDiskSpace = Objects.requireNonNull(reducedDiskSpace);
    }

    @Override
    protected BufferedReader openReader() throws IOException
    {
        return TanksSaveCodec.openReader(new FileInputStream(file));
    }

    @Override
    public void startWriting() throws FileNotFoundException
    {
        if (!reducedDiskSpace.getAsBoolean())
        {
            compactText = null;
            super.startWriting();
            return;
        }

        compactText = new StringWriter();
        printWriter = new PrintWriter(compactText);
    }

    @Override
    public void stopWriting()
    {
        super.stopWriting();
        if (compactText == null)
            return;

        String text = compactText.toString();
        compactText = null;
        byte[] bytes = TanksSaveCodec.encode(text, true);

        // Encode before replacing the previous save, and commit through a temporary sibling file.
        Path target = file.toPath().toAbsolutePath();
        Path temporary = null;
        try
        {
            temporary = Files.createTempFile(target.getParent(), ".tanks-save-", ".tmp");
            Files.write(temporary, bytes);
            try
            {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException e)
            {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to save " + path, e);
        }
        finally
        {
            if (temporary != null)
            {
                try
                {
                    Files.deleteIfExists(temporary);
                }
                catch (IOException e)
                {
                    temporary.toFile().deleteOnExit();
                }
            }
        }
    }
}
