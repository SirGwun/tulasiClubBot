package bot.core;

import bot.core.repos.GroupRepository;
import bot.core.repos.SessionRepository;
import bot.core.repos.UserRepository;
import bot.core.util.DataUtils;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
import quizbot.core.QuizBot;

public class Main {
    public static final Logger log = LoggerFactory.getLogger(Main.class);
    public static PaymentBot paymentBot;
    public static DataUtils dataUtils = new DataUtils(new GroupRepository(), new SessionRepository(), new UserRepository());
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
        dataUtils.checkAndFixAdminRights();
        dataUtils.loadTimers();
    }
}
