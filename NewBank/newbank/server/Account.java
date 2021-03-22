package newbank.server;

public class Account {

    private String accountName;
    private double currentBalance;
    private double possibleDebt = 0;

    public Account(String accountName, double openingBalance) {
        this.accountName = accountName;
        this.currentBalance = openingBalance;
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
