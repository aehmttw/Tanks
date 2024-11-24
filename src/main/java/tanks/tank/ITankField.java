package tanks.tank;

import tanks.tankson.ITanksONEditable;
import tanks.tankson.TanksONable;

@TanksONable("itank_field")
public interface ITankField extends ITanksONEditable
{
    Tank resolve();
}
