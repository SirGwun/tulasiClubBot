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


    //стрелочки в списке
    rightArrow,
    leftArrow,

    none,

    specialGroupTagChoose,
    buyWholeCourse
}
