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

}
