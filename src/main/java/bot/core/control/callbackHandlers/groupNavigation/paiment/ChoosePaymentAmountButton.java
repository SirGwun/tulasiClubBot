package bot.core.control.callbackHandlers.groupNavigation.paiment;

import bot.core.control.callbackHandlers.Action;
import bot.core.control.callbackHandlers.CallbackHandler;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ChoosePaymentAmountButton implements CallbackHandler {

    @Override
    public Action getAction() {
        return Action.choosePaymentAmount;
    }

    @Override
    public String getFormat() {
        return "choosePaymentAmount_<amount/custom>";
    }

    @Override
    public boolean match(Update update) {
        if (!update.hasCallbackQuery()) return false;
        return update.getCallbackQuery().getData().startsWith(getAction().toString());
    }

    @Override
    public boolean isFormatCorrect(String callback) {
        String[] data = callback.split("_");
        if (data.length != 3) return false;
        if (!data[0].equalsIgnoreCase(getAction().toString())) return false;
        try {
            Long.parseLong(data[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void handle(Update update) {
        System.out.println("Запрошен платеж " + update.getCallbackQuery().getData());
    }
}
