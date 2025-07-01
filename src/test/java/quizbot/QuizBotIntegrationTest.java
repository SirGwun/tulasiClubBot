package quizbot;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import quizbot.core.QuizBot;
import quizbot.core.SessionManager;
import quizbot.model.Session;
import quizbot.test.DoshaTestLoader;
import quizbot.test.Test;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.nio.file.Path;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuizBotIntegrationTest {
    @Test
    public void testStartCommand() throws Exception {
        QuizBot bot = spy(new QuizBot("token", "name"));
        Update upd = new Update();
        Message m = new Message();
        m.setText("/start");
        User u = new User();
        u.setId(1L);
        m.setFrom(u);
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setType("private");
        m.setChat(chat);
        upd.setMessage(m);

        doNothing().when(bot).execute(any(SendMessage.class));
        bot.onUpdateReceived(upd);
        verify(bot, atLeastOnce()).execute(any(SendMessage.class));
    }
}
