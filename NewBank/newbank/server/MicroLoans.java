package newbank.server;

public class MicroLoans {
    int accountDuration;
    int numbersOfLoans;


    public MicroLoans(int accountDuration, int numbersOfLoans) {
        this.accountDuration = accountDuration;
        this.numbersOfLoans = numbersOfLoans;

        public Boolean isLoanAllowed(int accountDuration, int numbersOfLoans){

            if (accountDuration >= 90) {
                if (numbersOfLoans < 3) {
                    return true;
                }
            } else {
                return false;
            }

        }
    }
}