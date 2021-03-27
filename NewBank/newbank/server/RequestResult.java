package newbank.server;


public class RequestResult {
    public String response;
    public Boolean continueProcessing = true;

    RequestResult(String response, Boolean logout) {
        this.response = response;
        this.continueProcessing = logout;
    }

    RequestResult() {
    }
}
