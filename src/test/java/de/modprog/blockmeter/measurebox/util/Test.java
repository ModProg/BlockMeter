package de.modprog.blockmeter.measurebox.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

import net.minecraft.util.math.BlockPos;

public class Test {
    @ParameterizedTest
    @JSONSource(jsons = { "[(1,2,3), false]" }, classes = {
            BlockPosParse.class,
            Boolean.class })
    void test(BlockPos first,
            boolean second) {
        assertThat(first).isEqualTo(new BlockPos(1, 2, 3));
        assertThat(second).isFalse();
    }

}

class And implements ArgumentsAggregator {

    @Override
    public Object aggregateArguments(ArgumentsAccessor arguments,
            ParameterContext context) throws ArgumentsAggregationException {

        return arguments.getBoolean(0) && arguments.getBoolean(1);
    }

}
