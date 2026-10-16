package com.delvedapps.core.reflection.messages;

public final class NetworkMessage<T> extends Message {
    public final T object;
    public final String response;

    public NetworkMessage(T object, String response) {
        this.object = object;
        this.response = response;
        setMessageDescription(object.toString());
    }
}