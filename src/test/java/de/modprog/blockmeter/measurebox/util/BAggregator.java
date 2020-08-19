package de.modprog.blockmeter.measurebox.util;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

public class BAggregator implements ArgumentsAggregator {


    @Override
    public Object aggregateArguments(ArgumentsAccessor arguments, ParameterContext context)
            throws ArgumentsAggregationException {
                System.out.println(context.getIndex());
        return arguments.getString(context.getIndex()).equals("true");
    }

}
