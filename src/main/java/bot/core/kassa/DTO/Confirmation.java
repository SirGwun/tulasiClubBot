package bot.core.kassa.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Confirmation {
    String type;

    @JsonProperty("return_url")
    String returnUrl;

    public Confirmation() {

    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
