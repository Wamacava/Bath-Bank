package newbank.server;

import java.util.ArrayList;

public class Customer {
	
	private ArrayList<Account> accounts;
	
	public Customer() {
		accounts = new ArrayList<>();
	}
	
	public String accountsToString() {
		String s = "";
		for(Account a : accounts) {
			s += a.toString();
		}
		return s;
	}

	public void addAccount(Account account) {
		accounts.add(account);		
	}
	public ArrayList<Account> getAccountList() { return accounts; }
	public Boolean move(Account fromAccount, Account toAccount, double amount){
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
