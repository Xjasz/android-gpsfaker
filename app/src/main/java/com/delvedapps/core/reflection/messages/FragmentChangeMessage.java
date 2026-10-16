package com.delvedapps.core.reflection.messages;

public final class FragmentChangeMessage<T> extends Message {
    public final T object;
    public final int container;
    public final int inAnimation;
    public final int outAnimation;

    public FragmentChangeMessage(T object, int container, int inAnimation, int outAnimation) {
        this.object = object;
        this.container = container;
        this.inAnimation = inAnimation;
        this.outAnimation = outAnimation;
        setMessageDescription(object.toString());
    }
}