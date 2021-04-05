package newbank.server;

import java.util.HashMap;
import java.util.ArrayList;
import java.lang.*;
import java.time.LocalDate;

public class NewBank {

    private static final NewBank bank = new NewBank();
    private HashMap<String, Customer> customers;
    private int numberOfCustomers = 0;

    private NewBank() {
        customers = new HashMap<>();
        addTestData();
    }

    private void addTestData() {
        numberOfCustomers++;
        Customer bhagy = new Customer("P@SsWorD1", numberOfCustomers);
        bhagy.addAccount(new Account("Main", 1000.0, "2021-01-01"));
        bhagy.addAccount(new Account("Savings", 100.0, "2021-01-02"));
        customers.put("Bhagy", bhagy);

        numberOfCustomers++;
        Customer christina = new Customer("adminperson1", numberOfCustomers);
        christina.addAccount(new Account("Main", 1500.0, "2021-02-01"));
        christina.addAccount(new Account("Savings", 1500.0, "2021-02-02"));
        customers.put("Christina", christina);

        // Christina duplicate test - this overwrites the existing Christina in customers
        numberOfCustomers++;
        Customer christina2 = new Customer("adminperson2", numberOfCustomers);
        christina2.addAccount(new Account("Main", 2500.0, "2021-01-03"));
        christina2.addAccount(new Account("Savings", 2500.0, "2021-01-04"));
        customers.put("Christina", christina2);

        numberOfCustomers++;
        Customer john = new Customer("abcde12", numberOfCustomers);
        john.addAccount(new Account("Main", 250.0));
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
    public synchronized RequestResult processRequest(CustomerID customer, String request) {
        if (customers.containsKey(customer.getKey())) {
            String[] splitRequest = request.split(" ");
            switch (splitRequest[0]) {
                case "SHOWMYACCOUNTS":
                    return new RequestResult(showMyAccounts(customer), true);
                case "MOVE":
                    return new RequestResult(moveRequest(customer, splitRequest), true);
                case "NEWACCOUNT":
                    return new RequestResult(newAccount(customer, splitRequest), true);
                case "PAY":
                    return new RequestResult(payRequest(customer, splitRequest), true);
                case "LOGOUT":
                    return new RequestResult("Success", false);
                default:
                    return new RequestResult("FAIL", true);
            }
        }
        return new RequestResult("FAIL", true);
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

        // convert second argument to double, return fail if not a number
        double amount;
        try {
            amount = Double.parseDouble(splitRequest[1]);
        } catch (NumberFormatException e) {
            // amount in invalid format - not a number
            return "FAIL";
        }

        // find the correct customer object
        Customer customer = customers.get(customerId.getKey());

        String fromAccountString = splitRequest[2];
        String toAccountString = splitRequest[3];

        // call move function in customer object
        if (customer.move(fromAccountString, toAccountString, amount)) {
            return "SUCCESS";
        }
        return "FAIL";
    }

    private String payRequest(CustomerID customerId, String[] splitRequest) {
        // check there are 3 things in the request
        if (splitRequest.length != 3) {
            return "FAIL";
        }

        // Current implementation only transfer money from Main to Main account
        // In the future we need to allow user to specify from and to account name
        String fromAccountName = "Main";
        String toAccountName = "Main";

        // 1. Get first customer
        Customer fromCustomer = customers.get(customerId.getKey());
        // 2. Find account of first customer
        Account fromAccount = fromCustomer.getAccount(fromAccountName);

        // 3. Find second customer
        String toCustomerString = splitRequest[1];
        if (!customers.containsKey(toCustomerString)) {
            return "FAIL";
        }
        Customer toCustomer = customers.get(toCustomerString);

        // 4. Find account of second customer
        Account toAccount = toCustomer.getAccount(toAccountName);

        // 5. Convert third argument to double, return fail if not a number
        double amount;
        try {
            amount = Double.parseDouble(splitRequest[2]);
        } catch (NumberFormatException e) {
            // amount in invalid format - not a number
            return "FAIL";
        }

        if (amount <= 0) {
            return "FAIL";
        }

        // 6. If all OK, try to remove money from first account
        if (!fromAccount.removeMoney(amount)) {
            return "FAIL";
        }

        // 7. If true returned - add money to the second account
        toAccount.addMoney(amount);

        // Add transaction to both customers' transaction history
        fromCustomer.addTransaction(new Transaction(LocalDate.now(),amount,"outgoing"));
        toCustomer.addTransaction(new Transaction(LocalDate.now(),amount,"incoming"));

        // 8. return "SUCCESS"
        return "SUCCESS";
    }
}
