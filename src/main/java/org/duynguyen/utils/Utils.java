package org.duynguyen.utils;

import java.io.IOException;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class Utils {
    private static final Random rand = new Random();
    private static final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi"));

    public static boolean availablePort(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    public static int nextInt(int max) {
        return rand.nextInt(max);
    }

    public static void setTimeout(Runnable runnable, int delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception e) {
                Log.error("Error in setTimeout: " + e.getMessage());
            }
        }).start();
    }

}
