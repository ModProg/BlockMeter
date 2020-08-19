package de.modprog.blockmeter.measurebox.util;

public interface Parser<T> {
    T parse(String string);
}