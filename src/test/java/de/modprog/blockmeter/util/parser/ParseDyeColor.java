package de.modprog.blockmeter.util.parser;

import net.minecraft.util.DyeColor;

public class ParseDyeColor implements Parser<DyeColor> {

    @Override
    public DyeColor parse(String string) {
        return DyeColor.byId(string, DyeColor.BLACK);
    }

}
