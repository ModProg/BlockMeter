package de.modprog.blockmeter.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.google.gson.Gson;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import de.modprog.blockmeter.util.parser.Parser;

public class JSONProvider
        implements ArgumentsProvider, AnnotationConsumer<JSONSource> {
    private String[] jsons;
    @SuppressWarnings("rawtypes")
    private Class[] classes;

    JSONProvider() {}

    @Override
    public void accept(JSONSource source) {
        jsons = source.jsons();
        classes = source.classes();
    }

    @SuppressWarnings("unchecked")
    public Stream<? extends Arguments> provideArguments(
            ExtensionContext context) {
        Gson gson = new Gson();
        return Stream.of(jsons).map(this::split).map((objectsStrings) -> {
            Builder<Object> builder = Stream.builder();
            for (int i = 0; i < objectsStrings.length; i++) {

                if (Parser.class.isAssignableFrom(classes[i]))

                    try {
                        builder.add(
                                ((Parser<?>) classes[i]
                                        .getConstructor()
                                        .newInstance())
                                                .parse(trimspaces(
                                                        objectsStrings[i])));
                    } catch (InstantiationException | IllegalAccessException
                            | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException
                            | SecurityException e) {
                        e.printStackTrace();
                    }
                else {
                    builder.add(gson.fromJson(objectsStrings[i], classes[i]));
                }
            }
            return Arguments.of(builder.build().toArray());
        });
    }

    private String trimspaces(String string) {
        return string.replaceAll("^\\s*|\\s*$", "");
    }

    public String[] split(String input) {
        int parans = 0;
        ArrayList<String> output = new ArrayList<String>();
        output.add("");
        for (char c: input.toCharArray()) {
            switch (c) {
            case '(':
            case '{':
            case '[':
                parans++;
                break;
            case ')':
            case ']':
            case '}':
                parans--;
            }
            if (parans == 0 && c == ',')
                output.add("");
            else
                output.set(output.size() - 1,
                        output.get(output.size() - 1) + c);
        }
        return output.toArray(new String[] {});
    }

}
