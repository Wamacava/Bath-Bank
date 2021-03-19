package newbank.server;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.*;

public class NewBank {

    private static final NewBank bank = new NewBank();
    private HashMap<String, Customer> customers;

    private NewBank() {
        customers = new HashMap<>();
        addTestData();
    }

    private void addTestData() {
        Customer bhagy = new Customer("P@SsWorD");
        bhagy.addAccount(new Account("Main", 1000.0));
        bhagy.addAccount(new Account("Savings", 100.0));
        customers.put("Bhagy", bhagy);

        Customer christina = new Customer("admin");
        christina.addAccount(new Account("Savings", 1500.0));
        customers.put("Christina", christina);

        Customer john = new Customer("abcd");
        john.addAccount(new Account("Checking", 250.0));
        customers.put("John", john);
    }

    public static NewBank getBank() {
        return bank;
    }

    public synchronized CustomerID checkLogInDetails(String userName, String password) {
        if (customers.containsKey(userName)) {
            Customer customer = customers.get(userName);
            if (customer.checkPassword(password)) {
                return new CustomerID(userName);
            }
        }
        return null;
    }

    // commands from the NewBank customer are processed in this method
    public synchronized String processRequest(CustomerID customer, String request) {
        if (customers.containsKey(customer.getKey())) {
            String[] splitRequest = request.split(" ");
            switch (splitRequest[0]) {
                case "SHOWMYACCOUNTS":
                    return showMyAccounts(customer);
                case "MOVE":
                    return moveRequest(customer, splitRequest);
                case "NEWACCOUNT":
                    return newAccount(customer, splitRequest);
                case "PAY":
                    return payRequest(customer, splitRequest);
                default:
                    return "FAIL";
            }
        }
        return "FAIL";
    }

    private String newAccount(CustomerID customerId, String[] splitRequest) {
        // check there are 2 things in the request
        if (splitRequest.length != 2) {
            return "FAIL";
        }
        Account account = new Account(splitRequest[1], 0.0);
        Customer customer = customers.get(customerId.getKey());
        if (customer.addAccount(account)) {
            return "SUCCESS";
        }
        return "FAIL";
    }

    private String showMyAccounts(CustomerID customer) {
        return (customers.get(customer.getKey())).accountsToString();
    }

    private String moveRequest(CustomerID customerId, String[] splitRequest) {
        // check there are 4 things in the request
        if (splitRequest.length != 4) {
            return "FAIL";
        }

        //check the second input in MOVE request is a number
        double amount;
        try {
            amount = Double.parseDouble(splitRequest[1]);
        } catch (NumberFormatException e) {
            // amount in invalid format - not a number
            return "FAIL";
        }

        // check both bank accounts exist for the customer
        Customer customer = customers.get(customerId.getKey());
        ArrayList<Account> customerAccounts = customer.getAccountList();

        //get the account objects (instead of the accountName)
        Account fromAccount = null;
        Account toAccount = null;

        for (int i = 0; i < customerAccounts.size(); i++) {
            if (customerAccounts.get(i).getName().equals(splitRequest[2])) {
                fromAccount = customerAccounts.get(i);
            }
        }

        for (int i = 0; i < customerAccounts.size(); i++) {
            if (customerAccounts.get(i).getName().equals(splitRequest[3])) {
                toAccount = customerAccounts.get(i);
            }
        }
        if (fromAccount == null || toAccount == null) { //check length of moveAccounts
            return "FAIL";
        }

        if (customer.move(fromAccount, toAccount, amount)) {
            return "SUCCESS";
        }

        return "FAIL";
    }

    private String payRequest(CustomerID customerId, String[] splitRequest) {
        // check there are 2 things in the request
        if (splitRequest.length != 3) {
            return "FAIL";
        }

        return "FAIL";
    }
}
