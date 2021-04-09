package newbank.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.LocalDate;
import java.util.*;

public class Customer {

    private ArrayList<Account> accounts;
    private LinkedList<Transaction> transactionHistory = new LinkedList<>();
    private ArrayList<Microloan> microloans = new ArrayList<>();


    private String name = "";
    private String surname = "";
    private String password = "";
    private String UID = "";
    private boolean isAdmin = false;
    private boolean isVerified = false;
    private boolean isActiveLoaner = false;

    private String AccountNameJson = "Name";
    private String AccountNumberJson = "AccountNumber";
    private String AccountBalanceJson = "Balance";
    private String AccountOpeningDateJson = "OpeningDate";

    private String TransactionDateJson = "Date";
    private String TransactionCustomerInvolvedJson = "CustomerInvolved";
    private String TransactionAmountJson = "Amount";
    private String TransactionIsIncomingJson = "IsIncoming";

    public static Customer CreateCustomer(String UID, String name, String surname, String password, boolean isAdmin, boolean isVerified, boolean isActiveLoaner) {
        if (validPassword(password)) {
            return new Customer(UID, name, surname, password, isAdmin, isVerified, isActiveLoaner);
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

    public Customer(String UID, String name, String surname, String password, boolean isAdmin, boolean isVerified, boolean isActiveLoaner) {
        this.password = password;
        this.UID = UID;
        this.name = name;
        this.surname = surname;
        this.isAdmin = isAdmin;
        this.isVerified = isVerified;
        this.isActiveLoaner = isActiveLoaner;
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

    public boolean addNewAccount(String newAccountName, int accountNumber) {
        for (Account a : accounts) {
            if (a.getName().equals(newAccountName)) {
                return false;
            }
        }
        Account account = new Account(newAccountName, accountNumber);
        accounts.add(account);
        return true;
    }


    public boolean addAccountFromDatabase(String newAccountName, Double balance, String openingDate, int accountNumber) {
        Account account = new Account(newAccountName, balance, openingDate, accountNumber);
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

    public String PrintTransactionHistory() {
        String history = "";
        for (Transaction transaction : this.transactionHistory) {
            history += transaction.toString();
        }
        return history;
    }

    public JSONArray AccountsToJson() {
        JSONArray accountList = new JSONArray();
        for (Account account : accounts) {
            JSONObject accountJson = new JSONObject();
            accountJson.put(AccountOpeningDateJson, account.getOpeningDate().toString());
            accountJson.put(AccountBalanceJson, account.getBalance());
            accountJson.put(AccountNameJson, account.getName());
            accountJson.put(AccountNumberJson, account.getAccountNumber());

            accountList.add(accountJson);
        }
        return accountList;
    }

    public void AccountsFromJson(JSONArray accountsJsonArray) {
        // An iterator over a collection. Iterator takes the place of Enumeration in the Java Collections Framework.
        // Iterators differ from enumerations in two ways:
        // 1. Iterators allow the caller to remove elements from the underlying collection during the iteration with well-defined semantics.
        // 2. Method names have been improved.
        Iterator<JSONObject> iterator = accountsJsonArray.iterator();
        // Adding accounts
        while (iterator.hasNext()) {
            JSONObject accountData = iterator.next();
            String accountName = (String) accountData.get(AccountNameJson);
            String openingDate = (String) accountData.get(AccountOpeningDateJson);
            Long accountNumber = (Long) accountData.get(AccountNumberJson);
            Double balance = 0.0;
            try {
                balance = (Double) accountData.get(AccountBalanceJson);
            } catch (NumberFormatException e) {
                // TODO we should log an error somehow
                // we reset customer's account in this case
            }
            this.addAccountFromDatabase(accountName, balance, openingDate, accountNumber.intValue());
        }
    }

    public JSONArray TransactionsToJson() {
        JSONArray transactionList = new JSONArray();
        for (Transaction transaction : this.transactionHistory) {
            JSONObject transactionJson = new JSONObject();
            transactionJson.put(TransactionDateJson, transaction.GetDate().toString());
            transactionJson.put(TransactionAmountJson, transaction.GetAmount());
            transactionJson.put(TransactionIsIncomingJson, transaction.IsIncoming());
            transactionJson.put(TransactionCustomerInvolvedJson, transaction.GetCustomerInvolved());

            transactionList.add(transactionJson);
        }
        return transactionList;
    }

    public void TransactionsFromJson(JSONArray transactionJsonArray) {
        // An iterator over a collection. Iterator takes the place of Enumeration in the Java Collections Framework.
        // Iterators differ from enumerations in two ways:
        // 1. Iterators allow the caller to remove elements from the underlying collection during the iteration with well-defined semantics.
        // 2. Method names have been improved.
        Iterator<JSONObject> iterator = transactionJsonArray.iterator();
        // Adding accounts
        while (iterator.hasNext()) {
            JSONObject accountData = iterator.next();
            String date = (String) accountData.get(TransactionDateJson);
            String customerInvolved = (String) accountData.get(TransactionCustomerInvolvedJson);
            boolean isIncoming = (boolean) accountData.get(TransactionIsIncomingJson);
            Double amount = 0.0;
            try {
                amount = (Double) accountData.get(TransactionAmountJson);
            } catch (NumberFormatException e) {
            }
            this.addTransaction(new Transaction(LocalDate.parse(date), amount, isIncoming, customerInvolved));
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

    public boolean getIsActiveLoaner() {
        return isActiveLoaner;
    }

    public void setIsActiveLoaner(boolean isActiveLoaner){
        this.isActiveLoaner = isActiveLoaner;
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
