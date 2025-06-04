package bot.core.model;

public enum EditingActions {
    NONE,
    ADD_GROUP,
    EDIT_HELP,
    EDIT_INFO;

    @Override
    public String toString() {
        return switch (this) {
            case NONE -> "ничего";
            case ADD_GROUP -> "добавление группы";
            case EDIT_HELP -> "редактирование помощи";
            case EDIT_INFO -> "редактирование информации";
        };
    }
}