package bot.core.model;

public enum EditingActions {
    NONE,
    EDIT_HELP,
    EDIT_INFO;

    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "ничего";
            case EDIT_HELP -> "редактирование помощи";
            case EDIT_INFO -> "редактирование информации";
        };
    }
}