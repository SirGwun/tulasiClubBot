package bot.core;

import bot.core.util.DataUtils;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
import quizbot.core.QuizBot;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot paymentBot;
    public static DataUtils dataUtils = new DataUtils();;
    public static boolean test;

    public static void main(String[] args) {

        for (String arg : args) {
            if (arg.equals("--test")) {
                test = true;
                break;
            }
        }

        paymentBot = new PaymentBot();
        init();
        if (!test) {
            QuizBot quizBot = new QuizBot();
        }
    }

    public static void init() {
        dataUtils.checkAdminRights();
        dataUtils.loadTimers();
    }
}
