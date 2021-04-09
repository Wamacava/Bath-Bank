package newbank.server;

import java.time.LocalDateTime;

public class Microloan {

    private String source;
    private String target;
    private LocalDateTime datetime;
    private double amount;
    private int loanPeriod;
    private double interestRate;

    public Microloan(String source, String target, LocalDateTime datetime, double amount, int loanPeriod, double interestRate) {
        this.source = source;
        this.target = target;
        this.datetime = datetime;
        this.amount = amount;
        this.loanPeriod = loanPeriod;
        this.interestRate = interestRate;
    }

    public String getSource() {
        return this.source;
    }
    public String getTarget() {
        return this.target;
    }
    public LocalDateTime getDateTime() {
        return this.datetime;
    }
    public double getAmount() {
        return this.amount;
    }
    public int getLoanPeriod() {
        return this.loanPeriod;
    }
    public double getInterestRate() {
        return this.interestRate;
    }

    public String toString() {
        String string = "";
        string += "Source: " + this.source + "\t";
        string += "Target: " + this.target + "\t";
        string += "Date/Time: " + this.datetime.toString() + "\t";
        string += "Amount: " + Double.toString(this.amount) + "\t";
        string += "Loan Period: " + Integer.toString(this.loanPeriod) + "\t";
        string += "Interest Rate: " + Double.toString(this.interestRate) + "\n";
        return string;
    }

}
