package win.baruna.blockmeter;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "modid")
public class ModConfig implements ConfigData{
    boolean toggleA = true;
    boolean toggleB = false;
    
    @ConfigEntry.Gui.CollapsibleObject
    InnerStuff stuff = new InnerStuff();
    
    @ConfigEntry.Gui.Excluded
    InnerStuff invisibleStuff = new InnerStuff();
    
    static class InnerStuff {
        int a = 0;
        int b = 1;
    }
    
}