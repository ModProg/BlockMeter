package de.modprog.blockmeter.measurebox.util;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

public class JSONProvider
        implements ArgumentsProvider, AnnotationConsumer<JSONSource> {
    private String[] jsons;
    private Class[] classes;

    JSONProvider() {}

    @Override
    public void accept(JSONSource source) {
        jsons = source.jsons();
        classes = source.classes();
    }

    public Stream provideArguments(ExtensionContext context) {
        final Gson gson = new Gson();
        return Stream.of(jsons)
                .map((json) -> json.substring(1, json.length() - 1))
                .map((json) -> json.split(",")).map((objectsStrings) -> {
                    Object[] objects = new Object[objectsStrings.length];
                    for (int i = 0; i < objectsStrings.length; i++) {
                        if (classes[i].isInstance(Parser.class))
                            ;
                        try {
                            objects[i] = classes[i].getConstructor().newInstance();
                        } catch (InstantiationException | IllegalAccessException
                                | IllegalArgumentException
                                | InvocationTargetException
                                | NoSuchMethodException
                                | SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                objects[i] = gson.fromJson(objectsStrings[i], classes[i]);
            }
            return objects;
        }).map(Arguments::of);
    }

}