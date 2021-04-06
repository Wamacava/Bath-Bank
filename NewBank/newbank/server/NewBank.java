package newbank.server;

import java.util.ArrayList;
import java.lang.*;
import java.time.LocalDate;

public class NewBank {

    private static final NewBank bank = new NewBank();

    private ArrayList<LocalDate> payments = new ArrayList<>();
    NewBankDatabaseHandler database = new NewBankDatabaseHandler();

    private NewBank() {
    }


    public static NewBank getBank() {
        return bank;
    }

    public synchronized CustomerID checkLogInDetails(String userName, String password) {
        Customer customer = database.LoadCustomerReadOnly(userName);
        if (customer != null) {
            if (customer.checkPassword(password)) {
                return new CustomerID(userName);
            }
        }
        return null;
    }

    // commands from the NewBank customer are processed in this method
    public synchronized RequestResult processRequest(CustomerID customerId, String request) {
        if (database.CustomerExists(customerId.getKey())) {
            String[] splitRequest = request.split(" ");
            switch (splitRequest[0]) {
                case "SHOWMYACCOUNTS":
                    return new RequestResult(showMyAccounts(customerId), true);
                case "MOVE":
                    return new RequestResult(moveRequest(customerId, splitRequest), true);
                case "NEWACCOUNT":
                    return new RequestResult(newAccount(customerId, splitRequest), true);
                case "PAY":
                    return new RequestResult(payRequest(customerId, splitRequest), true);
                case "SUBSCRIBETOMICROLOAN":
                    return new RequestResult(optinLoan(customerId), true);
                case "UNSUBSCRIBETOMICROLOAN":
                    return new RequestResult(optoutLoan(customerId), true);
                case "SHOWTRANSACTIONHISTORY":
                    String history = database.LoadCustomerReadOnly(customerId.getKey()).PrintTransactionHistory();
                    return new RequestResult(history, true);
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
        String accountName = splitRequest[1];

        Customer customer = database.LoadCustomerReadWrite(customerId.getKey());

        int accountNumber = database.GetHighestAccountNumber() + 1;
        database.SetHighestAccountNumber(accountNumber);
        boolean success = customer.addNewAccount(accountName, accountNumber);
        database.SaveExistingCustomer(customer);
        return success ? "SUCCESS" : "FAIL";
    }

    private String showMyAccounts(CustomerID customerId) {
        return (database.LoadCustomerReadOnly(customerId.getKey())).accountsToString();
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
        Customer customer = database.LoadCustomerReadWrite(customerId.getKey());

        String fromAccountString = splitRequest[2];
        String toAccountString = splitRequest[3];

        // call move function in customer object
        if (customer.move(fromAccountString, toAccountString, amount)) {
            database.SaveExistingCustomer(customer);
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
        Customer fromCustomer = database.LoadCustomerReadWrite(customerId.getKey());
        // 2. Find account of first customer
        Account fromAccount = fromCustomer.getAccount(fromAccountName);

        // 3. Find second customer
        String toCustomerString = splitRequest[1];
        if (!database.CustomerExists(toCustomerString)) {
            return "FAIL";
        }
        Customer toCustomer = database.LoadCustomerReadWrite(toCustomerString);

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
        fromCustomer.addTransaction(new Transaction(LocalDate.now(), amount, false, toCustomer.getUID()));
        toCustomer.addTransaction(new Transaction(LocalDate.now(), amount, true, fromCustomer.getUID()));

        // 8. return "SUCCESS"
        database.SaveExistingCustomer(fromCustomer);
        database.SaveExistingCustomer(toCustomer);
        return "SUCCESS";
    }

    private String optinLoan(CustomerID customerId){
        Customer customer = database.LoadCustomerReadWrite(customerId.getKey());
        //if not already an active loaner, change so you are
        if(!customer.getIsActiveLoaner()){
            customer.setIsActiveLoaner(true);
            database.SaveExistingCustomer(customer);
            return "SUCCESS";
        }
        return "FAIL";
    }

    private String optoutLoan(CustomerID customerId){
        Customer customer = database.LoadCustomerReadWrite(customerId.getKey());
        //if already an active loaner, change so you are not
        if(customer.getIsActiveLoaner()){
            customer.setIsActiveLoaner(false);
            database.SaveExistingCustomer(customer);
            return "SUCCESS";
        }
        return "FAIL";
    }
}
