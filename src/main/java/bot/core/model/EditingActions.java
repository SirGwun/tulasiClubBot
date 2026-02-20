package bot.core.model;

public enum EditingActions {
    NONE,
    EDIT_HELP,
    EDIT_PAYMENT_INFO,
    SENDING_SPREAD,
    SENDING_SPREAD_WITHOUT_BUTTON,
    WAIT_FOR_SPECIAL_GROUP;

    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "ничего";
            case EDIT_HELP -> "редактирование помощи";
            case EDIT_PAYMENT_INFO -> "установка методов оплаты";
            case SENDING_SPREAD -> "рассылка";
            case SENDING_SPREAD_WITHOUT_BUTTON -> "рассылка без кнопки";
            case WAIT_FOR_SPECIAL_GROUP -> "ожидание особой группы";
        };
    }
}