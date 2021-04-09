package newbank.server;

import java.util.ArrayList;
import java.lang.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class NewBank {

    private static final NewBank bank = new NewBank();

    NewBankDatabaseHandler database = new NewBankDatabaseHandler();

    private ArrayList<Microloan> microloans = new ArrayList<>();

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
                case "SHOWTRANSACTIONHISTORY":
                    String history = database.LoadCustomerReadOnly(customerId.getKey()).PrintTransactionHistory();
                    return new RequestResult(history, true);
                case "SUBSCRIBETOMICROLOAN":
                    return new RequestResult(optinLoan(customerId), true);
                case "UNSUBSCRIBETOMICROLOAN":
                    return new RequestResult(optoutLoan(customerId), true);
                case "REQUESTLOAN":
                    return new RequestResult(loanRequest(customerId, splitRequest), true);
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
        database.SaveExistingCustomer(customer);
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
            database.SaveExistingCustomer(fromCustomer);
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
            database.SaveExistingCustomer(fromCustomer);
            database.SaveExistingCustomer(toCustomer);
            return "FAIL";
        }

        if (amount <= 0) {
            database.SaveExistingCustomer(fromCustomer);
            database.SaveExistingCustomer(toCustomer);
            return "FAIL";
        }

        // 6. If all OK, try to remove money from first account
        if (!fromAccount.removeMoney(amount)) {
            database.SaveExistingCustomer(fromCustomer);
            database.SaveExistingCustomer(toCustomer);
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
        database.SaveExistingCustomer(customer);
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
        database.SaveExistingCustomer(customer);
        return "FAIL";
    }

    private String loanRequest(CustomerID customerId, String[] splitRequest) {
        // check there are 2 things in the request
        if (splitRequest.length != 2) {
            return "FAIL";
        }

        // Convert second argument to double, return fail if not a number
        double amount;
        try {
            amount = Double.parseDouble(splitRequest[1]);
        } catch (NumberFormatException e) {
            // amount in invalid format - not a number
            return "FAIL";
        }

        // Check that amount requested is no more than the maximum, and is not a negative value
        if (amount <= 0 || amount > 1000) {
            return "FAIL";
        }

        this.microloans = database.LoadMicroloans();

        Customer customer = database.LoadCustomerReadWrite(customerId.getKey());
        Account toAccount = customer.getAccount("Main");

        // Eligibility check - Main account must have existed for at least 3 months
        LocalDate mainOpeningDate = toAccount.getOpeningDate();
        long diff = ChronoUnit.MONTHS.between(mainOpeningDate,LocalDate.now());
        if (diff < 3) {
            System.out.println("Main account must have existed for at least 3 months.");
            database.SaveExistingCustomer(customer);
            return "FAIL";
        }

        // Eligibility check - Cannot have more than 3 loans at a time
        ArrayList<Microloan> customerMicroloans = database.GetCustomerMicroloans(customerId.getKey());
        if (customerMicroloans.size() >= 3) {
            System.out.println("Cannot have more than 3 active microloans at a time.");
            database.SaveExistingCustomer(customer);
            return "FAIL";
        }

        // Find active loaner with enough money in their Main account
        String loanerId = this.database.findEligibleLoaner(amount);
        if (loanerId == null) {
            database.SaveExistingCustomer(customer);
            return "FAIL";
        }

        Customer loaner = database.LoadCustomerReadWrite(loanerId);
        Account fromAccount = loaner.getAccount("Main");

        // Transfer money from loaner to requester
        if (!fromAccount.removeMoney(amount)) {
            database.SaveExistingCustomer(customer);
            database.SaveExistingCustomer(loaner);
            return "FAIL";
        }
        toAccount.addMoney(amount);

        // Add microloan data to microloans file
        int loanPeriod = 10;
        double interestRate = 5.0;
        microloans.add(new Microloan(loanerId,customerId.getKey(),LocalDateTime.now(),amount,loanPeriod,interestRate));

        database.SaveMicroloans(microloans);

        database.SaveExistingCustomer(customer);
        database.SaveExistingCustomer(loaner);
        return "SUCCESS";
    }
}
