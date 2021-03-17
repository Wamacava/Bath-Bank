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

    private String moveRequest(CustomerID customer, String[] splitRequest) {
        // check there are 4 things in the request
        if (splitRequest.length != 4) {
            return "FAIL";
        }
        // check both bank accounts exist for the customer
        ArrayList<Account> customerAccounts = customers.get(customer.getKey()).getAccountList();
        //check the second input in MOVE request is a number
        try {
            Double.parseDouble(splitRequest[1]);
        } catch (NumberFormatException e) {
            return "FAIL";
        }
        double amount = Double.parseDouble(splitRequest[1]);
        //get the account objects (instead of the accountName)
        ArrayList<Account> moveAccounts = new ArrayList<>();
        for (int i = 0; i < customerAccounts.size(); i++) {
            if (customerAccounts.get(i).getName().equals(splitRequest[2])) {
                moveAccounts.add(customerAccounts.get(i));
            }
        }
        for (int i = 0; i < customerAccounts.size(); i++) {
            if (customerAccounts.get(i).getName().equals(splitRequest[3])) {
                moveAccounts.add(customerAccounts.get(i));
            }
        }
        if (moveAccounts.size() != 2) { //check length of moveAccounts
            return "FAIL";
        }
        Account fromAccount = moveAccounts.get(0);
        Account toAccount = moveAccounts.get(1);
        if (customerAccounts.contains(fromAccount) && customerAccounts.contains(toAccount)) {
            if (customers.get(customer.getKey()).move(fromAccount, toAccount, amount)) {
                return "SUCCESS";
            }
            return "FAIL";
        }
        return "FAIL";
    }
}
