/**
 * CS 380.01 - Computer Networks
 * Professor: NDavarpanah
 *
 * Project 2
 * PhysLayerClient
 *
 * Justin Galloway
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class PhysLayerClient {
    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("18.221.102.182", 38002)) {
            // Initializations
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            System.out.println("Connected to server.");
            double base = getBaseline(is);
            System.out.println("Baseline established from preamble: " + base);
            os.write(decodeNRZI(is, base));

            // Check response claim
            if (is.read() == 1) {
                System.out.println("Response good.");
            }
        }
        System.out.println("Disconnected from server.");
    }

    public static double getBaseline(InputStream is) throws IOException {
        // Initialize baseline
        double base = 0.0;

        // Iterate through signals
        for(int i = 0; i < 64; i++) {
            int signal = is.read();
            base += signal;
        }
        // Take the average...
        base = base / 64;
        return base;
    }

    public static byte[] decodeNRZI(InputStream is, double bl) throws IOException {
        HashMap<String, String> decoder = new HashMap<>();
        // 4B/5B Table inputs
        decoder.put("11110", "0000");
        decoder.put("01001", "0001");
        decoder.put("10100", "0010");
        decoder.put("10101", "0011");
        decoder.put("01010", "0100");
        decoder.put("01011", "0101");
        decoder.put("01110", "0110");
        decoder.put("01111", "0111");
        decoder.put("10010", "1000");
        decoder.put("10011", "1001");
        decoder.put("10110", "1010");
        decoder.put("10111", "1011");
        decoder.put("11010", "1100");
        decoder.put("11011", "1101");
        decoder.put("11100", "1110");
        decoder.put("11101", "1111");

        String[] holdBytes = new String[64];
        boolean check = false;
        // Iterate through bytes...
        for(int i = 0; i < 64; i++) {
            String toBin = "";
            // Iterate through binary...
            for(int j = 0; j < 5; j++) {
                boolean flag;
                if (is.read() > bl) {
                    flag = true;
                } else {
                    flag = false;
                }
                if (flag == check) {
                    toBin += "0";
                } else {
                    toBin += "1";
                }
                check = flag;
            }
            holdBytes[i] = decoder.get(toBin);
        }

        System.out.print("Recieved 32 bytes: ");
        byte[] returnToServer = new byte[32];
        for(int i = 0; i < 32; i++) {
            String firstHalf = holdBytes[2 * i];
            String secondalf = holdBytes[2 * i + 1];
            System.out.printf("%X", Integer.parseInt(firstHalf, 2));
            System.out.printf("%X", Integer.parseInt(secondalf, 2));
            String combine = firstHalf + secondalf;
            returnToServer[i] = (byte)Integer.parseInt(combine, 2);
        }

        System.out.println();
        return returnToServer;
    }
}
