package newbank.server;

import java.util.ArrayList;


/**
 * Class responsible for making sure that two threads do not try to operate on the same file at the same time.
 * This is to avoid case when for example two threads make a pay operation, one saves result first, and the second
 * overwrites result of the previous operation
 */
public class NewBankDatabaseHandler {
    private NewBankDatabaseAccessor databaseAccessor = new NewBankDatabaseAccessor();
    private ArrayList<String> databaseEntriesInUse = new ArrayList<>();
    private boolean isAccountDetailsFileInUse = false;

    public NewBankDatabaseHandler() {

    }

    public boolean CustomerExists(String id) {
        return databaseAccessor.CustomerExists(id);
    }

    /**
     * Load customer data when you expect to write it back. This is a simple synchronisation mechanism to prevent
     * functions from using outdated customer/accounts data
     *
     * @param id of the customer
     * @return Customer or null
     */
    public Customer LoadCustomerReadWrite(String id) {
        // if customer database entry is already used by another thread, wait until its done
        while (databaseEntriesInUse.contains(id)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Customer customer = databaseAccessor.LoadCustomer(id);
        if (customer != null) {
            databaseEntriesInUse.add(id);
        }
        return customer;
    }

    /**
     * Load customer data entry for read only
     *
     * @param id of the customer
     * @return Customer or null
     */
    public Customer LoadCustomerReadOnly(String id) {
        Customer customer = databaseAccessor.LoadCustomer(id);
        return customer;
    }

    public ArrayList<Customer> LoadCustomerBySurname() {
        // TODO implement
        return databaseAccessor.LoadCustomerBySurname();
    }

    public boolean SaveExistingCustomer(Customer customer) {
        databaseEntriesInUse.remove(customer.getUID());
        return databaseAccessor.SaveExistingCustomer(customer);
    }

    public boolean SaveNewCustomer(Customer customer) {
        // TODO implement for new customer functionality
        return databaseAccessor.SaveNewCustomer(customer);
    }

    public void SaveMicroloans(ArrayList<Microloan> microloans) {
        databaseAccessor.SaveMicroloans(microloans);
    }

    public ArrayList<Microloan> LoadMicroloans() {
        ArrayList<Microloan> microloans = databaseAccessor.LoadMicroloans();
        return microloans;
    }

    public int GetHighestAccountNumber() {
        // if account_details file is already used by another thread, wait until its done
        while (isAccountDetailsFileInUse) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isAccountDetailsFileInUse = true;
        return databaseAccessor.GetHighestAccountNumber();
    }

    public void SetHighestAccountNumber(int newHighestAccountNumber) {
        databaseAccessor.SetHighestAccountNumber(newHighestAccountNumber);
        isAccountDetailsFileInUse = false;
    }

    private ArrayList<String> getMicroloanSubscribedUsers() {
        ArrayList<String> microloanSubscribedUsers = new ArrayList<>();
        // Scan all user files for active loaners
        ArrayList<String> allCustomerIds = this.databaseAccessor.getAllCustomerIds();
        for (String id : allCustomerIds) {
            Customer customer = LoadCustomerReadOnly(id);
            if (customer.getIsActiveLoaner()) {
                microloanSubscribedUsers.add(id);
            }
            SaveExistingCustomer(customer);
        }

        return microloanSubscribedUsers;
    }

    public String findEligibleLoaner(double amount) {
        // Get list of eligible loaners
        ArrayList<String> microloanSubscribedUsers = getMicroloanSubscribedUsers();
        for (String id : microloanSubscribedUsers) {
            Customer customer = LoadCustomerReadOnly(id);
            double amountInMain = customer.getAccount("Main").getBalance();
            if (amountInMain >= (4 * amount)) {
                return id;
            }
        }
        // If no eligible loaners, print error message
        System.out.println("There are no eligible loaners for this amount.");
        return null;
    }

    public ArrayList<Microloan> GetAllActiveMicroloans() {
        return databaseAccessor.LoadMicroloans();

    }

    public ArrayList<Microloan> GetCustomerMicroloans(String id) {
        ArrayList<Microloan> customerMicroloans = new ArrayList<>();

        // Search list of all microloans for target = id
        ArrayList<Microloan> allMicroloans = GetAllActiveMicroloans();
        for (Microloan microloan : allMicroloans) {
            if (microloan.getTarget().equals(id)) {
                customerMicroloans.add(microloan);
            }
        }

        return customerMicroloans;
    }

    /**
     * This function wants to get the returned microloans, merge it with  historicalmicroloans from database,
     * and save it into the historical microloans file.
     * @param newReturnedMicroloans
     */
    public void AddHistoricalMicroloans(ArrayList<Microloan> newReturnedMicroloans) {
        ArrayList<Microloan> historicalMicroloans = databaseAccessor.LoadHistoricalMicroloans();
        historicalMicroloans.addAll(newReturnedMicroloans);
        databaseAccessor.SaveHistoricalMicroloans(historicalMicroloans);
    }
}
