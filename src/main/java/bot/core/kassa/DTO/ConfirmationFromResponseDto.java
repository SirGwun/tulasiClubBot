package bot.core.kassa.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfirmationFromResponseDto {
    @JsonProperty("type")
    private String type;

    @JsonProperty("confirmation_url")
    private String confirmationUrl;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    public void setConfirmationUrl(String confirmationUrl) {
        this.confirmationUrl = confirmationUrl;
    }
}
