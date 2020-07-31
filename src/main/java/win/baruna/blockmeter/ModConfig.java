package win.baruna.blockmeter;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "blockmeter")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean minimalLabelSize = false;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean deleteBoxesOnDisable = true;
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean sendBoxes = true;
    public boolean showBoxesWhenDisabled = false;

    @ConfigEntry.Gui.Excluded
    public boolean incrementColor = true;
    @ConfigEntry.Gui.Excluded
    public boolean innerDiagonal = false;
    @ConfigEntry.Gui.Excluded
    public boolean showOtherUsersBoxes = false;
    @ConfigEntry.Gui.Excluded
    public int colorIndex = 0;
}
