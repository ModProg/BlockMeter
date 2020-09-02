package de.modprog.blockmeter.measurebox.util.parser;

import net.minecraft.util.DyeColor;

public class ParseDyeColor implements Parser<DyeColor> {

    @Override
    public DyeColor parse(String string) {
        return DyeColor.byName(string, DyeColor.BLACK);
    }

}
