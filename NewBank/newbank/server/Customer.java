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

	public boolean addAccount(Account account) {
		for (Account a: accounts){
			if (a.getName().equals(account.getName())){
				return false;
			}
		}
		accounts.add(account);
		return true;
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
