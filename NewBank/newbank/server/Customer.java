package newbank.server;

import java.util.*;

public class Customer {

    private ArrayList<Account> accounts;
    private LinkedList<Transaction> transactionHistory = new LinkedList<>();


    private String name = "";
    private String surname = "";
    private String password = "";
    private String UID = "";
    private boolean isAdmin = false;
    private boolean isVerified = false;
    private int numberOfAccounts = 0;

    public static Customer CreateCustomer(String UID, String name, String surname, String password, boolean isAdmin, boolean isVerified) {
        if (validPassword(password)) {
            return new Customer(UID, name, surname, password, isAdmin, isVerified);
        }
        return null;
    }

    public static Customer CreateCustomer(String password, String UID) {
        if (validPassword(password)) {
            return new Customer(password, UID);
        }
        return null;
    }

    public Customer(String password, String UID) {
        this.password = password;
        this.UID = UID;
        accounts = new ArrayList<>();
    }

    public Customer(String UID, String name, String surname, String password, boolean isAdmin, boolean isVerified) {
        this.password = password;
        this.UID = UID;
        this.name = name;
        this.surname = surname;
        this.isAdmin = isAdmin;
        this.isVerified = isVerified;
        accounts = new ArrayList<>();
    }

    private static Boolean validPassword(String password) {
        boolean numberIncluded = false;
        int length = 0;
        for (String letter : password.split("")) {
            try {
                Double.parseDouble(letter);
                numberIncluded = true;
            } catch (NumberFormatException e) {
            }
            length++;
        }
        if (length >= 6 && numberIncluded) {
            return true;
        }
        return false;
    }

    public String accountsToString() {
        String s = "";
        for (Account a : accounts) {
            s += a.toString() + "\n";
        }
        return s;
    }

    public boolean addNewAccount(String newAccountName) {
        for (Account a : accounts) {
            if (a.getName().equals(newAccountName)) {
                return false;
            }
        }
        Account account = new Account(newAccountName);
        account.setAccountNumber(accounts.size() + 1); // TODO this should in database
        accounts.add(account);
        return true;
    }


    public boolean addAccountFromDatabase(String newAccountName, Double balance, String openingDate) {
        Account account = new Account(newAccountName, balance, openingDate);
        account.setAccountNumber(accounts.size() + 1); // TODO this should in database
        accounts.add(account);
        return true;
    }

    public void addTransaction(Transaction transaction) {
        transactionHistory.addFirst(transaction);
    }

    /**
     * Function to get account with a given name
     *
     * @param accountName string
     * @return if account with given name exists, return this account. Return null otherwise
     */
    public Account getAccount(String accountName) {
        for (Account account : accounts) {
            if (account.getName().equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public ArrayList<Account> getAccountList() {
        return accounts;
    }

    public Boolean move(String fromAccountString, String toAccountString, double amount) {
        Account fromAccount = this.getAccount(fromAccountString);
        Account toAccount = this.getAccount(toAccountString);

        if (amount <= 0) {
            return false;
        }

        if (fromAccount == null || toAccount == null) { //check length of moveAccounts
            return false;
        }

        // try to remove money from <From> account has enough money
        // and add to <To> account if successful
        if (fromAccount.removeMoney(amount)) {
            //update balance
            toAccount.addMoney(amount);
            return true;
        }
        return false;
    }

    public void printTransactionHistory() {
        System.out.println("Transaction History");
        for (Transaction transaction : this.transactionHistory) {
            System.out.println(transaction.toString());
        }
    }

    public String getSurname() {
        return surname;
    }


    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public String getPassword() {
        return password;
    }


    public boolean setPassword(String password) {
        if (Customer.validPassword(password)) {
            this.password = password;
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
