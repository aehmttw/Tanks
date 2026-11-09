package tanks.tankson;

import basewindow.BaseFile;
import basewindow.ComputerFileManager;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Selects game-save handling while leaving the shared desktop file manager format-agnostic. */
public class TanksFileManager extends ComputerFileManager
{
    private final BooleanSupplier reducedDiskSpace;

    public TanksFileManager(BooleanSupplier reducedDiskSpace)
    {
        this.reducedDiskSpace = Objects.requireNonNull(reducedDiskSpace);
    }

    @Override
    public BaseFile getFile(String path)
    {
        return path.toLowerCase(Locale.ROOT).endsWith(".tanks") ? new TanksFile(path, reducedDiskSpace) : super.getFile(path);
    }
}
