package newbank.server;

import java.util.*;

public class Customer {

    private ArrayList<Account> accounts;

    private String password;

    public Customer(String password) {
        if (validPassword(password)) {
            this.password = password;
            accounts = new ArrayList<>();
        }
    }

    private Boolean validPassword(String password){
        boolean numberIncluded = false;
        int length = 0;

        for(String letter : password.split("")){
            try {
                Double.parseDouble(letter);
                numberIncluded = true;
            } catch (NumberFormatException e){}
            length = length + 1;
        }
        if (length >= 6 && numberIncluded){
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

    public boolean addAccount(Account account) {
        for (Account a : accounts) {
            if (a.getName().equals(account.getName())) {
                return false;
            }
        }
        accounts.add(account);
        return true;
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

    // No one should have access to account outside this class
//    public ArrayList<Account> getAccountList() {
//        return accounts;
//    }

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

}
