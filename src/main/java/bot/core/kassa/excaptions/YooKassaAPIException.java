package bot.core.kassa.excaptions;

public class YooKassaAPIException extends RuntimeException {



    int status;

    public YooKassaAPIException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}