package newbank.server;

import java.util.ArrayList;

public class Customer {

	private ArrayList<Account> accounts;

	private String password;

	public Customer(String password) {
		this.password = password;
		accounts = new ArrayList<>();
	}

	public String accountsToString() {
		String s = "";
		for (Account a : accounts) {
			s += a.toString();
		}
		return s;
	}

	public boolean addAccount(Account account) {
		for (Account a : accounts) {
			if (a.getName().equals(account.getName())) {
				return false;
			}
		}
		accounts.add(account);
		return true;
	}

	public boolean checkPassword(String password) {
		return this.password.equals(password);
	}

	public ArrayList<Account> getAccountList() {
		return accounts;
	}

	public Boolean move(Account fromAccount, Account toAccount, double amount) {
		//check the <From> account has enough money
		if (fromAccount.getBalance() >= amount) {
			//update balance
			fromAccount.removeMoney(amount);
			toAccount.addMoney(amount);
			return true;
		}
		return false;
	}
}
