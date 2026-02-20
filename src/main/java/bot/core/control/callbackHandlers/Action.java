package bot.core.control.callbackHandlers;

public enum Action {
    //выбор курса
    getCourseList,
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

    none,

    specialGroupTagChoose,
    buyWholeCourse
}
