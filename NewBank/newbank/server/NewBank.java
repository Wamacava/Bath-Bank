package newbank.server;

import java.util.HashMap;
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
        christina.addAccount(new Account("Main", 1500.0));
        christina.addAccount(new Account("Savings", 1500.0));
        customers.put("Christina", christina);

        Customer john = new Customer("abcd");
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
        // check there are 2 things in the request
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

        // 6. If all OK, try to remove money from first account
        if (!fromAccount.removeMoney(amount)) {
            return "FAIL";
        }

        // 7. If true returned - add money to the second account
        toAccount.addMoney(amount);

        // 8. return "SUCCESS"
        return "SUCCESS";
    }
}
