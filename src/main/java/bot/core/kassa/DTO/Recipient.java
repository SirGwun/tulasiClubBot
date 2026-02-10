package bot.core.kassa.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Recipient {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("gateway_id")
    private String gatewayId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }
}
