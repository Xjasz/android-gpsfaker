package com.delvedapps.core.reflection.messages;

import android.location.Location;

public final class LocationMessage extends Message {
    public final Integer object;
    public final Location location;

    public LocationMessage(Integer object, Location location) {
        this.object = object;
        this.location = location;
        setMessageDescription(object.toString());
    }
}