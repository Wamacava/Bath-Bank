package newbank.server;

import java.io.File;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.Iterator;

import java.io.FileWriter;
import java.io.IOException;


public class NewBankDatabaseAccessor {
    private String databaseDirectory = "newbank/database/";
    private String dbFileExtension = ".json";
    // Fields used in JSON file (to avoid mismatches in loaders and savers):
    private String NameJson = "Name";
    private String SurnameJson = "Surname";
    private String PasswordJson = "Password";
    private String AccountListJson = "Accounts";
    private String IsAdminJson = "IsAdmin";
    private String IsVerifiedJson = "IsVerified";
    private String AccountNameJson = "Name";
    private String AccountBalanceJson = "Balance";
    private String AccountOpeningDateJson = "OpeningDate";

    public NewBankDatabaseAccessor() {

    }

    /**
     * Checks if there is an entry for a given customer in the bank database
     *
     * @param id of the customer to check
     * @return true if entry found
     */
    public boolean CustomerExists(String id) {
        String filename = id + dbFileExtension;
        return new File(databaseDirectory, filename).exists();
    }

    /**
     * Loads Customer object from the database
     *
     * @param id of the customer to load
     * @return null if customer with given ID not found, populated Customer object otherwise
     */
    public Customer LoadCustomer(String id) {
        if (!CustomerExists(id)) {
            System.out.println("Trying to load customer " + id + ". Does not exist");
            return null;
        }

        String customerFileName = databaseDirectory + id + dbFileExtension;
        JSONParser parser = new JSONParser();
        Customer customer = null;

        try (FileReader fileReader = new FileReader(customerFileName)) {

            Object obj = parser.parse(fileReader);

            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            JSONObject jsonObject = (JSONObject) obj;

            String name = (String) jsonObject.get(NameJson);
            String surname = (String) jsonObject.get(SurnameJson);
            String password = (String) jsonObject.get(PasswordJson);
            boolean isAdmin = (boolean) jsonObject.get(IsAdminJson);
            boolean isVerified = (boolean) jsonObject.get(IsVerifiedJson);

            customer = Customer.CreateCustomer(id, name, surname, password, isAdmin, isVerified);

            // Customer creation can fail due to the password check
            if (customer != null) {

                // A JSON array. JSONObject supports java.util.List interface.
                JSONArray accountsJsonArray = (JSONArray) jsonObject.get(AccountListJson);

                customer = ParseCustomerAccounts(customer, accountsJsonArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (customer != null) {
            System.out.println("Customer " + id + " loaded");
        }
        return customer;
    }

    /**
     * Function to load customer by surname
     *
     * @return List of customers that match the search criteria
     */
    public ArrayList<Customer> LoadCustomerBySurname() {
        // TODO implement
        return new ArrayList<>();
    }

    /**
     * Saves changes to previously loaded customer
     * NOTE do not use this to add new customer to the database. There is another function for this
     *
     * @param customer Customer to save
     * @return true is save was successful
     */
    public boolean SaveExistingCustomer(Customer customer) {
        if (customer == null) {
            return false;
        }
        // JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
        JSONObject obj = new JSONObject();
        obj.put(NameJson, customer.getName());
        obj.put(SurnameJson, customer.getSurname());
        obj.put(PasswordJson, customer.getPassword());
        obj.put(IsAdminJson, customer.getIsAdmin());
        obj.put(IsVerifiedJson, customer.getIsVerified());


        JSONArray accountList = new JSONArray();
        ArrayList<Account> accounts = customer.getAccountList();
        for (Account account : accounts) {
            JSONObject accountJson = new JSONObject();
            accountJson.put(AccountOpeningDateJson, account.getOpeningDate().toString());
            accountJson.put(AccountBalanceJson, account.getBalance());
            accountJson.put(AccountNameJson, account.getName());

            accountList.add(accountJson);
        }
        obj.put(AccountListJson, accountList);

        // Constructs a FileWriter given a file name, using the platform's default charset
        String fileName = databaseDirectory + customer.getUID() + dbFileExtension;
        try (FileWriter fileWriter = new FileWriter(fileName)) {

            fileWriter.write(prettyPrintJSON(obj.toJSONString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Saves new customer to the database
     *
     * @param customer Customer to save
     * @return true is save is successful, false if customer with given ID is
     */
    public boolean SaveNewCustomer(Customer customer) {
        // TODO implement for new customer functionality
        return true;
    }


    private Customer ParseCustomerAccounts(Customer customer, JSONArray accountsJsonArray) {
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
            Double balance = 0.0;
            try {
                balance = (Double) accountData.get(AccountBalanceJson);
            } catch (NumberFormatException e) {
                // TODO we should log an error somehow
                // we reset customer's account in this case
            }
            customer.addAccountFromDatabase(accountName, balance, openingDate);
        }
        return customer;
    }


    /**
     * A simple implementation to pretty-print JSON file.
     *
     * @param unformattedJsonString
     * @return
     */
    private static String prettyPrintJSON(String unformattedJsonString) {
        StringBuilder prettyJSONBuilder = new StringBuilder();
        int indentLevel = 0;
        boolean inQuote = false;
        for (char charFromUnformattedJson : unformattedJsonString.toCharArray()) {
            switch (charFromUnformattedJson) {
                case '"':
                    // switch the quoting status
                    inQuote = !inQuote;
                    prettyJSONBuilder.append(charFromUnformattedJson);
                    break;
                case ' ':
                    // For space: ignore the space if it is not being quoted.
                    if (inQuote) {
                        prettyJSONBuilder.append(charFromUnformattedJson);
                    }
                    break;
                case '{':
                case '[':
                    // Starting a new block: increase the indent level
                    prettyJSONBuilder.append(charFromUnformattedJson);
                    indentLevel++;
                    appendIndentedNewLine(indentLevel, prettyJSONBuilder);
                    break;
                case '}':
                case ']':
                    // Ending a new block; decrese the indent level
                    indentLevel--;
                    appendIndentedNewLine(indentLevel, prettyJSONBuilder);
                    prettyJSONBuilder.append(charFromUnformattedJson);
                    break;
                case ',':
                    // Ending a json item; create a new line after
                    prettyJSONBuilder.append(charFromUnformattedJson);
                    if (!inQuote) {
                        appendIndentedNewLine(indentLevel, prettyJSONBuilder);
                    }
                    break;
                default:
                    prettyJSONBuilder.append(charFromUnformattedJson);
            }
        }
        return prettyJSONBuilder.toString();
    }

    /**
     * Print a new line with indention at the beginning of the new line.
     *
     * @param indentLevel
     * @param stringBuilder
     */
    private static void appendIndentedNewLine(int indentLevel, StringBuilder stringBuilder) {
        stringBuilder.append("\n");
        for (int i = 0; i < indentLevel; i++) {
            // Assuming indention using 2 spaces
            stringBuilder.append("  ");
        }
    }
}
