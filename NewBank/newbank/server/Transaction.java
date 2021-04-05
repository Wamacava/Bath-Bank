package newbank.server;

import java.time.LocalDate;

public class Transaction {

    private LocalDate date;
    private double amount;
    private String direction;

    public Transaction(LocalDate date, double amount, String direction) {
        this.date = date;
        this.amount = amount;
        this.direction = direction;
    }

    public String toString() {
        String string = "";
        string += this.date.toString()+"\t";
        string += Double.toString(this.amount)+"\t";
        string += this.direction;
        return string;
    }

}
