package newbank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExampleClient extends Thread {

    private Socket server;
    private PrintWriter bankServerOut;
    private BufferedReader userInput;
    private Thread bankServerResponceThread;
    private AtomicBoolean isServerConnected;

    public ExampleClient(String ip, int port) throws UnknownHostException, IOException {
        server = new Socket(ip, port);
        userInput = new BufferedReader(new InputStreamReader(System.in));
        bankServerOut = new PrintWriter(server.getOutputStream(), true);
        isServerConnected = new AtomicBoolean(true);

        bankServerResponceThread = new Thread() {
            private BufferedReader bankServerIn = new BufferedReader(new InputStreamReader(server.getInputStream()));

            public void run() {
                try {
                    while (true) {
                        String response = bankServerIn.readLine();
                        if (response == null) {
                            break;
                        }
                        System.out.println(response);
                    }
                    System.out.println("Connection with bank closed. Please try again later.");
                    isServerConnected.set(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        bankServerResponceThread.start();
    }


    public void run() {
        while (isServerConnected.get()) {
            try {
                while (isServerConnected.get()) {
                    String command = userInput.readLine();
                    bankServerOut.println(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        Boolean retryConnecting = true;
        while (retryConnecting) {
            try {
                new ExampleClient("localhost", 14002).start();
                retryConnecting = false; // already connected
            } catch (IOException e) {
                System.out.println("Failed to connect to the bank, do you want to retry? [y/n]");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String response = reader.readLine();
                if (!response.equals("y")) {
                    retryConnecting = false;
                }
            }

        }

    }
}
