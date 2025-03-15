package org.duynguyen.utils;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Random;

public class Utils {
    private static final Random rand = new Random();

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

    public static byte[] generateDESKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[8];
        random.nextBytes(key);
        for (int i = 0; i < key.length; i++) {
            key[i] = setParityBit(key[i]);
        }
        return key;
    }

    private static byte setParityBit(byte b) {
        int bitCount = Integer.bitCount(b & 0xFE);
        return (byte) (b & 0xFE | (bitCount % 2 == 0 ? 1 : 0));
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
