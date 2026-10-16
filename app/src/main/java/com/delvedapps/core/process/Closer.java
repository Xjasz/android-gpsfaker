package com.delvedapps.core.process;

import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.Socket;

public class Closer {
    private static final String TAG = Closer.class.getSimpleName();

    public static void closeSilently(Object... xs) {
        for (Object x : xs) {
            if (x != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && x instanceof Closeable) {
                        ((Closeable) x).close();
                    } else if (x instanceof Socket) {
                        ((Socket) x).close();
                    } else if (x instanceof ByteArrayOutputStream) {
                        ((ByteArrayOutputStream) x).close();
                    } else if (x instanceof DatagramSocket) {
                        ((DatagramSocket) x).close();
                    } else {
                        Log.d(TAG, "cannot close: " + x);
                        throw new RuntimeException("cannot close " + x);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}