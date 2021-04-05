package newbank.server;

import java.time.LocalDate;

public class Account {

    private int accountNumber;
    private String accountName;
    private double currentBalance;
    private double possibleDebt = 0;
    private LocalDate openingDate;

    public Account(String accountName, double openingBalance) {
        this.accountName = accountName;
        this.currentBalance = openingBalance;
        this.openingDate = LocalDate.now();
    }
    public Account(String accountName, double openingBalance, String openingDate) {
        this.accountName = accountName;
        this.currentBalance = openingBalance;
        this.openingDate = LocalDate.parse(openingDate);
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String toString() {
        return (accountName + ": " + currentBalance);
    }

    public double getBalance() {
        return (currentBalance);
    }

    public String getName() {
        return this.accountName;
    }

    public void addMoney(double amount) {
        this.currentBalance += amount;
    }

    /**
     * Remove money from the account. Check if the account has enough money
     *
     * @param amount
     * @return true if money was removed, fail if there was not enough money
     */
    public boolean removeMoney(double amount) {
        if (amount <= 0) {
            return false;
        }
        if (currentBalance + possibleDebt >= amount) {
            this.currentBalance -= amount;
            return true;
        }
        return false;
    }
}
