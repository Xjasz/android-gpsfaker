package com.delvedapps.core.reflection.messages;

import com.delvedapps.core.reflection.listeners.Listener;

public final class BackRequestMessage<T> extends Message {
    public final T object;

    public BackRequestMessage(T object) {
        this.object = object;
        setMessageDescription(object.toString());
    }

    public void respond(Object object) {
        Listener.send(new BackResponseMessage<>(object));
    }
}