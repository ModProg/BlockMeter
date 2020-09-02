package de.modprog.blockmeter.measurebox.util.parser;

import net.minecraft.util.Identifier;

public class ParseIdentifier implements Parser<Identifier> {

    @Override
    public Identifier parse(String string) {
        return new Identifier(string);
    }

}
