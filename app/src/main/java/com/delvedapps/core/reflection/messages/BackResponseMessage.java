package com.delvedapps.core.reflection.messages;

public final class BackResponseMessage<T> extends Message {
    public final T object;

    public BackResponseMessage(T object) {
        this.object = object;
        setMessageDescription(object.toString());
    }
}