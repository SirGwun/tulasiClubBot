package quizbot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import quizbot.model.AnswerScale;
import quizbot.model.Session;
import quizbot.test.DoshaTestLoader;
import quizbot.test.Test;

import java.io.IOException;
import java.util.Optional;

public class QuizBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(QuizBot.class);
    private final SessionManager sessions = new SessionManager();
    private final String token;
    private final String name;

    public QuizBot(String token, String name) {
        super(token);
        this.token = token;
        this.name = name;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            }
        } catch (Exception e) {
            log.error("Update handling failed", e);
        }
    }

    private void handleMessage(Message message) throws Exception {
        long userId = message.getFrom().getId();
        String text = message.getText();
        if ("/start".equals(text)) {
            SendMessage sm = SendMessage.builder()
                    .chatId(String.valueOf(message.getChatId()))
                    .text("Choose option:")
                    .replyMarkup(mainMenu())
                    .build();
            execute(sm);
        }
    }

    private InlineKeyboardMarkup mainMenu() {
        InlineKeyboardButtonFactory factory = new InlineKeyboardButtonFactory();
        return factory.mainMenu();
    }

    private void handleCallback(CallbackQuery cb) throws Exception {
        long userId = cb.getFrom().getId();
        String data = cb.getData();
        if (data.equals("ABOUT")) {
            SendMessage sm = new SendMessage();
            sm.setChatId(cb.getMessage().getChatId().toString());
            sm.setText("Dosha service: https://example.com");
            execute(sm);
            return;
        }
        if (data.equals("START_TEST")) {
            startTest(cb);
            return;
        }
        if (data.startsWith("ANSWER_")) {
            int score = Integer.parseInt(data.substring("ANSWER_".length()));
            Session session = sessions.get(userId);
            if (session != null) {
                session.getTest().registerAnswer(score);
                sendNextQuestion(session, cb.getMessage().getChatId());
            }
            return;
        }
    }

    private void startTest(CallbackQuery cb) throws IOException, Exception {
        long userId = cb.getFrom().getId();
        Test test = DoshaTestLoader.loadDefault();
        Session session = new Session(userId, test);
        sessions.getOrCreate(userId, session);
        sendNextQuestion(session, cb.getMessage().getChatId());
    }

    private void sendNextQuestion(Session session, long chatId) throws Exception {
        Test test = session.getTest();
        Optional<String> q = test.nextQuestion();
        if (q.isPresent()) {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(chatId));
            sm.setText(q.get());
            sm.setReplyMarkup(ButtonsFactory.answerButtons());
            execute(sm);
        } else if (test.isFinished()) {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(chatId));
            sm.setText(test.result().toString());
            execute(sm);
            sessions.remove(session.getUserId());
        }
    }

    @Override
    public String getBotUsername() {
        return name;
    }
}

class InlineKeyboardButtonFactory {
    InlineKeyboardMarkup mainMenu() {
        List<List<org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton>> rows = new ArrayList<>();

        org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton test = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
        test.setText("Take Dosha Test");
        test.setCallbackData("START_TEST");
        org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton about = new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton();
        about.setText("About");
        about.setCallbackData("ABOUT");

        rows.add(List.of(test, about));
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
