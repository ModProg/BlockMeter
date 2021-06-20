package win.baruna.blockmeter;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "blockmeter")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean minimalLabelSize = false;
    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean deleteBoxesOnDisable = true;
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean sendBoxes = true;
    public boolean showBoxesWhenDisabled = false;
    public boolean backgroundForLabels = true;

    @ConfigEntry.Gui.Excluded
    public boolean incrementColor = true;
    @ConfigEntry.Gui.Excluded
    public boolean innerDiagonal = false;
    @ConfigEntry.Gui.Excluded
    public boolean showOtherUsersBoxes = false;
    @ConfigEntry.Gui.Excluded
    public int colorIndex = 0;
}
