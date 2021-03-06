package newbank.server;

import java.time.LocalDate;

public class Account {

    private int accountNumber;
    private String accountName;
    private Double currentBalance;
    private Double possibleDebt = 0.0;
    private LocalDate openingDate;

    /**
     * Constructor for new account creation
     *
     * @param accountName
     */
    public Account(String accountName, int accountNumber) {
        this.accountName = accountName;
        this.currentBalance = 0.0;
        this.openingDate = LocalDate.now();
        this.accountNumber = accountNumber;
    }

    /**
     * Constructor for account loaded from the database
     *
     * @param accountName
     * @param openingBalance
     * @param openingDate
     */
    public Account(String accountName, double openingBalance, String openingDate, int accountNumber) {
        this.accountName = accountName;
        this.currentBalance = openingBalance;
        this.openingDate = LocalDate.parse(openingDate);
        this.accountNumber = accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String toString() {
        return (accountName + ": " + currentBalance);
    }

    public Double getBalance() {
        return currentBalance;
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

    /**
     * This function allows the account to go into negative when removing the loan
     * @param amount
     * @return
     */
    public boolean removeLoan(double amount) {
        if (amount <= 0) {
            return false;
        }
        this.currentBalance -= amount;
        return true;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }
}
