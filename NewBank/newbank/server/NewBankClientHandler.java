package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NewBankClientHandler extends Thread {

    private NewBank bank;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;


    public NewBankClientHandler(Socket s) throws IOException {
        bank = NewBank.getBank();
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new PrintWriter(s.getOutputStream(), true);
        socket = s;
    }

    public void run() {
        // keep getting requests from the client and processing them
        try {
            // repeat checkLoginDetails until valid login entered
            int failedLoginAttempts = 0;
            while (failedLoginAttempts < 3) {
                // ask for username
                out.println("Enter Username");
                String userName = in.readLine();
                // ask for password
                out.println("Enter Password");
                String password = in.readLine();
                out.println("Checking Details...");
                // authenticate user and get customer ID token from bank for use in subsequent requests
                CustomerID customer = bank.checkLogInDetails(userName, password);
                // if the user is authenticated then get requests from the user and process them
                if (customer != null) {
                    failedLoginAttempts = 0;
                    out.println("Log In Successful."
                            + "\nYou are able to complete the following actions:"
                            + "\n\tSHOWMYACCOUNTS"
                            + "\n\tNEWACCOUNT <Name>"
                            + "\n\tMOVE <Amount> <From> <To>"
                            + "\n\tPAY <Name> <Amount>"
                            + "\n\tLOGOUT"
                            + "\nWhat do you want to do?");
                    RequestResult result;
                    do {
                        String request = in.readLine();
                        System.out.println("Request from " + customer.getKey());
                        result = bank.processRequest(customer, request);
                        out.println(result.response);

                    } while (result.continueProcessing);


                } else {
                    out.println("Log In Failed; Please Retry");
                    failedLoginAttempts++;
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                in.close();
                out.close();
                System.out.println("Connection closed, ip: " +
                        ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress());
            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

}
