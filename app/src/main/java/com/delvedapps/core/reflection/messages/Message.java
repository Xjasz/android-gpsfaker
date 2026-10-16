package com.delvedapps.core.reflection.messages;

public abstract class Message<T> {
    private String messageDescription;

    @Override
    public String toString() {
        return String.format("Message{ %s, '%s' }", this.getClass().getSimpleName(), messageDescription);
    }

    public String getMessageDescription() {
        return messageDescription;
    }

    public void setMessageDescription(String messageDescription) {
        this.messageDescription = messageDescription;
    }
}