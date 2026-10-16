package com.delvedapps.core.reflection.injectors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface Inject {
    int id();
}