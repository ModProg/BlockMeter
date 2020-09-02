package de.modprog.blockmeter.measurebox.util.parser;

import java.util.stream.Stream;

import net.minecraft.util.math.BlockPos;

public class ParseBlockPos implements Parser<BlockPos> {

    @Override
    public BlockPos parse(String string) {
        Integer[] split = Stream
                .of(string.substring(1, string.length() - 1).split("\\|"))
                .map(Integer::parseInt)
                .toArray(Integer[]::new);
        return new BlockPos(split[0], split[1], split[2]);
    }

}
