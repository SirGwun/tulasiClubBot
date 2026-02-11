package bot.core.control.callbackHandlers;

public enum Action {
    //выбор курса
    chooseArchiveOrActual,
    archive,
    chooseTag,
    chooseGroup,

    //действия админа
    confirm,
    decline,
    delGroup,
    setTag,

    //дать ссылку на группу в крайнем случае
    getJoinRequestedLink,

    //инструкции
    getInstruction,
    getPaymentInstruction,

    //ссылка на описание крусов
    getCourseDescription,

    //стрелочки в списке
    rightArrow,
    leftArrow,

    none, //ничего не делать

    payWithRussianCard,
    payWithForeignCard,
    alreadyPaid,
    choosePaymentAmount,
}
