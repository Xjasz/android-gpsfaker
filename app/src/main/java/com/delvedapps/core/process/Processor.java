package com.delvedapps.core.process;

import android.util.Log;

import com.delvedapps.core.util.contstant.Constants;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Processor {
    private static final String TAG = Processor.class.getSimpleName();

    public static String runCommand(String... statements) {
        for (String s : statements)
            Log.d(TAG, "statement: " + s);
        String result = sudoForResult(statements);
        Log.d(TAG, "result: " + result);
        return result;
    }

    private static String sudoForResult(String... strings) {
        String res = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try {
            Process su = Runtime.getRuntime().exec(Constants.SUDO);
            outputStream = new DataOutputStream(su.getOutputStream());
            response = su.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s + Constants.NEW_LINE);
                outputStream.flush();
            }
            outputStream.writeBytes(Constants.SUDO_EXIT + Constants.NEW_LINE);
            outputStream.flush();

            su.waitFor();
            res = readFully(response);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            Closer.closeSilently(outputStream, response);
        }
        return res;
    }

    private static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1)
            baos.write(buffer, 0, length);
        String data = baos.toString(Constants.UTF_8);
        baos.flush();
        Closer.closeSilently(baos);
        return (data.length() > 1) ? data.substring(0, data.length() - 1) : data;
    }

}
