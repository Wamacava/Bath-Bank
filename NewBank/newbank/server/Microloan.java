package newbank.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Microloan {

    private String source;
    private String target;
    private LocalDateTime loanStartDate;
    private double amount;
    private int loanPeriod;
    private double interestRate;

    public Microloan(String source, String target, LocalDateTime loanStartDate, double amount, int loanPeriod, double interestRate) {
        this.source = source;
        this.target = target;
        this.loanStartDate = loanStartDate;
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
    public LocalDateTime getLoanStartDate() {
        return this.loanStartDate;
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

    /**
     * This function creates the amount that should be returned by the customer to the loaner
     * @return
     */
    public double getAmountDue() {
        return amount*(1.0+interestRate);
    }

    public boolean isExpired(){
        long loanDuration = ChronoUnit.MINUTES.between(loanStartDate, LocalDateTime.now());

        if(loanDuration > getLoanPeriod()){
            return true;
        }
        return false;
    }

    public String toString() {
        String string = "";
        string += "Source: " + this.source + "\t";
        string += "Target: " + this.target + "\t";
        string += "Date/Time: " + this.loanStartDate.toString() + "\t";
        string += "Amount: " + Double.toString(this.amount) + "\t";
        string += "Loan Period: " + Integer.toString(this.loanPeriod) + "\t";
        string += "Interest Rate: " + Double.toString(this.interestRate) + "\n";
        return string;
    }

}
