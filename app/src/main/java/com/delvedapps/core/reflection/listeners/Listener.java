package com.delvedapps.core.reflection.listeners;

import android.util.Log;
import android.view.View;

import com.delvedapps.core.reflection.controllers.BaseController;
import com.delvedapps.core.reflection.messages.Message;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Listener {
    private static final String TAG = Listener.class.getSimpleName();
    private static final Map<Class<? extends Message>, List<Invoker>> activeListeners = new HashMap<>();

    private static final class Invoker {
        public final BaseController controller;
        public final Method method;
        public final Listen annotation;

        public Invoker(BaseController controller, Method method) {
            this.controller = controller;
            this.method = method;
            this.annotation = method.getAnnotation(Listen.class);
        }
    }

    public synchronized static void register(BaseController controller) {
        for (Method method : controller.getClass().getMethods())
            if (method.isAnnotationPresent(Listen.class)) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != 1) {
                    throw new RuntimeException(String.format("invalid method for Listen annotation, found %d parameters, expecting 1", parameters.length));
                } else if (!parameters[0].getClass().isInstance(Message.class)) {
                    throw new RuntimeException(String.format("invalid method for Listen annotation, found %s when expecting Class<? extends Message>", parameters[0].getClass().getSimpleName()));
                } else {
                    Class<? extends Message> paramType = (Class<? extends Message>) parameters[0];
                    if (!activeListeners.containsKey(paramType))
                        activeListeners.put(paramType, new CopyOnWriteArrayList<Invoker>());
                    activeListeners.get(paramType).add(0, new Invoker(controller, method));
                }
            }
    }

    public synchronized static void unregister(BaseController controller) {
        for (List<Invoker> invokersByClass : activeListeners.values())
            for (Invoker invoker : invokersByClass)
                if (invoker.controller.equals(controller))
                    invokersByClass.remove(invoker);
    }

    public synchronized static void send(final Message message) {
        if (activeListeners.containsKey(message.getClass())) {
            for (final Invoker invoker : activeListeners.get(message.getClass())) {
                View view = invoker.controller.getView();
                Log.i(TAG, message.getMessageDescription());
                if (view != null)
                    invoker.controller.getView().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                invoker.method.invoke(invoker.controller, message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                else
                    try {
                        invoker.method.invoke(invoker.controller, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                if (invoker.annotation.consumed())
                    return;
            }
        }
    }
}
