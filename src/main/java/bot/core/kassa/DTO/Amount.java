package bot.core.kassa.DTO;

import java.math.BigDecimal;

public class Amount {

    private BigDecimal value;
    private String currency;

    public Amount() {
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
