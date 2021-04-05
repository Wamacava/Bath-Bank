package newbank.server;

import java.time.LocalDate;

public class Transaction {

    private String customerInvolved;
    private LocalDate date;
    private double amount;
    private boolean isIncomingTransaction;

    public Transaction(LocalDate date, double amount, boolean isIncomingTransaction, String customerInvolved) {
        this.customerInvolved = customerInvolved;
        this.date = date;
        this.amount = amount;
        this.isIncomingTransaction = isIncomingTransaction;
    }

    public String toString() {
        String string = "";
        string += this.date.toString()+"\t";
        string += Double.toString(this.amount)+"\t";
        string += "Incoming: " + this.isIncomingTransaction;
        return string;
    }

    public LocalDate GetDate(){
        return date;
    }

    public Double GetAmount(){
        return amount;
    }

    public String GetCustomerInvolved(){
        return customerInvolved;
    }

    public boolean IsIncoming(){
        return isIncomingTransaction;
    }

}
