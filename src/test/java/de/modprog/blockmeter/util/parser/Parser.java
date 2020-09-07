package de.modprog.blockmeter.util.parser;

public interface Parser<T> {
    T parse(String string);
}
