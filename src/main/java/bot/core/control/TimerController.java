package bot.core.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TimerController {
    private static final Logger log = LoggerFactory.getLogger(TimerController.class);
    private static final List<Timer> timers = new ArrayList<>();
    public static final long STANDARD_TIME = 360000;

    public static void addTimer(long userId, long groupId, long ms) {
        Timer timer = new Timer(userId, groupId, ms);
        if (!timers.contains(timer)) {
            log.info("Новый таймер добавлен");
            timers.add(timer);
            timer.start();
            return;
        }
        log.info("Попытка добавления уже существующего таймера");
    }

    public static void stopTimer(long userId, long groupId) {
        for (int i = 0; i < timers.size(); i++) {
            Timer timer = timers.get(i);
            if (timer.matches(userId, groupId)) {
                log.info("timer stop");
                timer.stop();
                timers.remove(timer);
                return;
            }
        }
    }

    public static boolean hasTimer(long userId, long groupId) {
        for (Timer timer : timers) {
            if (timer.matches(userId, groupId)) {
                return true;
            }
        }
        return false;
    }
}
