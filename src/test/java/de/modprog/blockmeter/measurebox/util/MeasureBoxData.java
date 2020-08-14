package de.modprog.blockmeter.measurebox.util;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MeasureBoxData implements ArgumentsProvider {
    public final BlockPos bp1;
    public final BlockPos bp2;
    public final Identifier dimension;
    public final DyeColor color;
    public final boolean finished;
    public final int mode;
    public final int orientation;

    MeasureBoxData(BlockPos bp1, BlockPos bp2, Identifier identifier, DyeColor color, boolean finished,
            int mode, int orientation) {
        this.bp1 = bp1;
        this.bp2 = bp2;
        this.dimension = identifier;
        this.color = color;
        this.finished = finished;
        this.mode = mode;
        this.orientation = orientation;
    }

    @Override
    public String toString() {
        return bp1.toShortString();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(new Object[] {
                new MeasureBoxData(new BlockPos(0, 1, 2), new BlockPos(10, 5, 2), new Identifier("minecraft:overworld"),
                        DyeColor.BLACK, true, 0, 0),
                new MeasureBoxData(new BlockPos(-30, 200, -1012), new BlockPos(10, 3, -100),
                        new Identifier("minecraft:overworld"), DyeColor.ORANGE, false, 5, 3),
                new MeasureBoxData(new BlockPos(10300, 1, 2), new BlockPos(10000, 5, 2),
                        new Identifier("minecraft:overworld"), DyeColor.WHITE, true, 20, 4) })
                .map(Arguments::of);
    }

}