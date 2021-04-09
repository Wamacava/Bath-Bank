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


/**
 * Class responsible for accessing database files
 */
public class NewBankDatabaseAccessor {
    private String customerDatabaseDirectory = "newbank/customer_database/";
    private String dbFileExtension = ".json";
    // Fields used in JSON file (to avoid mismatches in loaders and savers):
    private String NameJson = "Name";
    private String SurnameJson = "Surname";
    private String PasswordJson = "Password";
    private String AccountListJson = "Accounts";
    private String TransactionListJson = "Transactions";
    private String IsAdminJson = "IsAdmin";
    private String IsVerifiedJson = "IsVerified";
    private String IsActiveLoanerJson = "IsActiveLoaner";


    private String bankDatabaseDirectory = "newbank/bank_details_database/";
    private String accountDetailsFilename = "account_details.json";
    private String HighestAccountNumberJson = "HighestAccountNumber";

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
        return new File(customerDatabaseDirectory, filename).exists();
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

        String customerFileName = customerDatabaseDirectory + id + dbFileExtension;
        // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.

        JSONObject jsonObject = ParseJsonFile(customerFileName);

        String name = (String) jsonObject.get(NameJson);
        String surname = (String) jsonObject.get(SurnameJson);
        String password = (String) jsonObject.get(PasswordJson);
        boolean isAdmin = (boolean) jsonObject.get(IsAdminJson);
        boolean isVerified = (boolean) jsonObject.get(IsVerifiedJson);
        boolean isActiveLoaner = (boolean) jsonObject.get(IsActiveLoanerJson);

        Customer customer = Customer.CreateCustomer(id, name, surname, password, isAdmin, isVerified, isActiveLoaner);

        // Customer creation can fail due to the password check
        if (customer != null) {

            // A JSON array. JSONObject supports java.util.List interface.
            JSONArray accountsJsonArray = (JSONArray) jsonObject.get(AccountListJson);
            customer.AccountsFromJson(accountsJsonArray);

            JSONArray transactionsJsonArray = (JSONArray) jsonObject.get(TransactionListJson);
            customer.TransactionsFromJson(transactionsJsonArray);

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
        obj.put(IsActiveLoanerJson, customer.getIsActiveLoaner());

        obj.put(AccountListJson, customer.AccountsToJson());

        obj.put(TransactionListJson, customer.TransactionsToJson());

        // Constructs a FileWriter given a file name, using the platform's default charset
        String filePath = customerDatabaseDirectory + customer.getUID() + dbFileExtension;
        SaveToJsonFile(obj, filePath);

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

    public int GetHighestAccountNumber() {
        JSONObject jsonObject = ParseJsonFile(bankDatabaseDirectory + accountDetailsFilename);
        return ((Long) jsonObject.get(HighestAccountNumberJson)).intValue();
    }

    public void SetHighestAccountNumber(int newHighestAccountNumber) {
        // JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
        JSONObject obj = new JSONObject();
        obj.put(HighestAccountNumberJson, newHighestAccountNumber);
        String filePath = bankDatabaseDirectory + accountDetailsFilename;
        SaveToJsonFile(obj, filePath);
    }

    private JSONObject ParseJsonFile(String filePath) {
        JSONObject jsonObject = null;
        try (FileReader fileReader = new FileReader(filePath)) {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(fileReader);
            // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
            jsonObject = (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void SaveToJsonFile(JSONObject jsonObject, String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {

            fileWriter.write(prettyPrintJSON(jsonObject.toJSONString()));

        } catch (IOException e) {
            e.printStackTrace();
        }
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
