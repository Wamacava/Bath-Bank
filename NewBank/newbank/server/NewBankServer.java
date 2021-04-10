package newbank.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;

public class NewBankServer extends Thread {

    private ServerSocket server;

    public NewBankServer(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public void run() {
        // starts up a new client handler thread to receive incoming connections and process requests
        System.out.println("New Bank Server listening on " + server.getLocalPort());
        try {
            while (true) {
                System.out.println("Before accepting the socket");
                Socket s = server.accept();
                        NewBankClientHandler clientHandler = new NewBankClientHandler(s);
                clientHandler.start();
                System.out.println("New connection open, ip: "
                        + ((InetSocketAddress) s.getRemoteSocketAddress()).getAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void startMicroLoanUpdater() {
        // And From your main() method or any other method
        Timer timer = new Timer();
        int updatePeriodUs = 5000;
        MicroLoanUpdater microLoanUpdater = new MicroLoanUpdater();
        timer.schedule(microLoanUpdater, 0, updatePeriodUs);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // starts a new NewBankServer thread on a specified port number
        NewBankServer nbs = new NewBankServer(14002);
        nbs.start();
        nbs.startMicroLoanUpdater();
    }


}

