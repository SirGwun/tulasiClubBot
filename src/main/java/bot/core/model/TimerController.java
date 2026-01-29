package bot.core.model;

import bot.core.Legacy;
import bot.core.util.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TimerController {
    private static final Logger log = LoggerFactory.getLogger(TimerController.class);
    private static final Map<Timer, Thread> timers = new ConcurrentHashMap<>();

    public static void addTimer(long userId, long groupId, long min) {
        Timer timer = new Timer(userId, groupId, min * 60);
        if (!timers.containsKey(timer)) {
            log.info("Таймер добавлен");
            timers.put(timer, new Thread(timer));
            Legacy.dataUtils.storeTimer(timer);
            timers.get(timer).start();
            return;
        }
        log.info("Попытка добавления уже существующего таймера");
    }

    public static void restoreTimer(long userId, long groupId, long sec) {
        Timer timer = new Timer(userId, groupId, sec);
        if (!timers.containsKey(timer)) {
            timers.put(timer, new Thread(timer));
            timers.get(timer).start();
        }
    }

    public static void stopTimer(long userId, long groupId) {
        Timer timer = getTimer(userId, groupId);
        if (timer == null) {
            log.info("Timer for {} {} not find", userId, groupId);
            return;
        }
        log.info("timer stop");
        timers.get(timer).interrupt();
    }

    public static boolean hasTimer(long userId, long groupId) {
        return getTimer(userId, groupId) != null;
    }

    private static Timer getTimer(long userId, long groupId) {
        for (Timer timer : timers.keySet()) {
            if (timer.matches(userId, groupId)) {
                return timer;
            }
        }
        return null;
    }

    public static class Timer implements Serializable, Runnable {
        private final long userId;
        private final long groupId;
        private final long time_sec;
        private final long startTime;
        @Serial
        private static final long serialVersionUID = 1L;

        public Timer(long userId, long groupId, long sec) {
            this.userId = userId;
            this.groupId = groupId;
            time_sec = sec;
            startTime = Instant.now().getEpochSecond();
        }

        @Override
        public void run() {
            try {
                Thread.sleep(time_sec * 1000);
                if (!Thread.interrupted()) {
                    log.info("user {} added in group {} by timer", userId, Legacy.dataUtils.getGroupName(groupId));
                    ChatUtils.addInGroup(userId, groupId, "Добавлен по таймеру");
                }
            } catch (InterruptedException e) {
                log.info("timer interrupted");
            } finally {
                Legacy.dataUtils.unstoreTimer(userId, groupId);
                timers.remove(this);
            }
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Timer))
                return false;
            return ((Timer) object).userId == userId && ((Timer) object).groupId == groupId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, groupId);
        }

        public boolean matches(long userId, long groupId) {
            return this.userId == userId && this.groupId == groupId;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getTime_sec() {
            return time_sec;
        }

        public long getUserId() {
            return userId;
        }

        public long getGroupId() {
            return groupId;
        }
    }
}
