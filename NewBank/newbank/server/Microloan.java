package newbank.server;

import java.time.LocalDate;

public class Microloan {

    private String source;
    private String target;
    private LocalDate date;
    private double amount;
    private int loanPeriod;
    private double interestRate;

    public Microloan(String source, String target, LocalDate date, double amount, int loanPeriod, double interestRate) {
        this.source = source;
        this.target = target;
        this.date = date;
        this.amount = amount;
        this.loanPeriod = loanPeriod;
        this.interestRate = interestRate;
    }


}
