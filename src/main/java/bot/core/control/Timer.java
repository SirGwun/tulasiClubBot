package bot.core.control;

import bot.core.Main;
import bot.core.util.ChatUtils;

public class Timer {
    private final long userId;
    private final long groupId;
    private long time;
    private long messageOnCHeckId;
    Thread thread;

    public Timer(long userId, long groupId, long ms, long messageOnCHeckId) {
        this.userId = userId;
        this.groupId = groupId;
        time = ms;
        this.messageOnCHeckId = messageOnCHeckId;
    }

    void start() {
        thread = new Thread(() -> {
            try {
                Thread.sleep(time);
                ChatUtils.addInGroup(userId, groupId, "Добавлен по таймеру");
            } catch (InterruptedException e) {
                return;
            }
        });
        thread.start();
    }

    void stop() {
        thread.interrupt();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Timer))
            return false;
        return ((Timer) object).userId == userId && ((Timer) object).groupId == groupId;
    }

    public boolean matches(long userId, long groupId) {
        return this.userId == userId && this.groupId == groupId;
    }
}
