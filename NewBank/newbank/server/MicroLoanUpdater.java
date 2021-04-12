package newbank.server;

import java.util.TimerTask;

class MicroLoanUpdater extends TimerTask {
    private NewBank bank;

    public MicroLoanUpdater(){
        bank = NewBank.getBank();
        System.out.println("Creating MicroLoanUpdater!");

    }

    public void run() {
        // This will be executed periodically
        bank.updateMicroloans();
    }

}


