package de.modprog.blockmeter.measurebox.util;

import java.util.Arrays;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MBDAggregator implements ArgumentsAggregator {

    @Override
    public Object aggregateArguments(ArgumentsAccessor arguments, ParameterContext context)
            throws ArgumentsAggregationException {
        return new MeasureBoxData(bp(arguments.getString(0)), bp(arguments.getString(1)),
                arguments.get(2, Identifier.class), dc(arguments.getString(3)),
                arguments.getString(4).equals("finished"), 0, 0);
    }

    private BlockPos bp(String s) {
        final int[] array = Arrays.stream(s.split("[/|-]")).mapToInt(Integer::parseInt).toArray();
        if (array.length > 0)
            return new BlockPos(array[0], array[1], array[2]);
        throw new ArgumentsAggregationException("not enough numbers in BlockPos");
    }

    private DyeColor dc(String s) {
        return DyeColor.valueOf(s.toUpperCase());
    }

}
