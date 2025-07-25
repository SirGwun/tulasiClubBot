package quizbot.core;

import bot.core.Main;
import bot.core.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import quizbot.model.Session;
import quizbot.test.TestLoader;
import quizbot.test.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class QuizBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(QuizBot.class);
    private final SessionManager sessions = new SessionManager();
    private static final String token;
    private static final String name;
    public static final String data;

    static {
        if (System.getenv("AMVERA") != null && System.getenv("AMVERA").equals("1")) {
            data = "/data";
            token = System.getenv( "BOTTOCKEN");
            name = System.getenv("BOTNAME");
        } else {
            data = "data";
            try (InputStream secretInput = DataUtils.class.getClassLoader().getResourceAsStream("secret.properties")) {
                if (secretInput == null) {
                    throw new FileNotFoundException("secret.properties not found");
                }
                Properties secretProperties = new Properties();
                secretProperties.load(secretInput);
                token = secretProperties.getProperty("tulasiTestBotToken");
                name = secretProperties.getProperty("tulasiTestBotName");
            } catch (IOException e) {
                log.error("Не удалось прочитать токен и имя тестового бота");
                throw new RuntimeException("Не удалось загрузить секреты", e);
            }
            log.info("QuizBot инициализирован");
        }
    }

    public QuizBot() {
        super(token);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            log.info("{} запущен", name);
        } catch (TelegramApiException e) {
            log.error("Ошибка при инициализации бота {}", e.getMessage());
        }
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
            sendMainMenu("Выберете тест", userId);
        } else {
            sendMainMenu("Выберете тест", userId);
        }
    }

    private void handleCallback(CallbackQuery cb) throws Exception {
        long userId = cb.getFrom().getId();
        String data = cb.getData();
        if (data.equals("HELP")) {
            SendMessage sm = new SendMessage();
            sm.setChatId(cb.getMessage().getChatId().toString());
            sm.setText("Выберете тест из главного меню, отвечайте на вопросы и в конце увидите результат теста");
            execute(sm);
        }
        else if (data.startsWith("startTest_")) {
            startTest(cb);
        }
        else if (data.startsWith("ANSWER_")) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(cb.getFrom().getId());
            deleteMessage.setMessageId(cb.getMessage().getMessageId());
            execute(deleteMessage);

            int score = Integer.parseInt(data.substring("ANSWER_".length()));
            Session session = sessions.get(userId);
            if (session != null) {
                Test test = session.getTest();
                if (test != null) {
                    test.registerAnswer(score);
                    sendNextQuestion(session, cb.getMessage().getChatId());
                } else {
                    SendMessage sm = SendMessage.builder()
                            .chatId(cb.getMessage().getChatId().toString())
                            .text("Выберете тест")
                            .replyMarkup(new KeyboardFactory().mainMenu())
                            .build();
                    execute(sm);
                }
            }
        }

        try {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(cb.getId());
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа на CallbackQuery", e);
        }
    }

    private void startTest(CallbackQuery cb) throws IOException, TelegramApiException {
        long userId = cb.getFrom().getId();
        String testName = cb.getData().substring("startTest_".length());
        Path testPath = Path.of(data, testName + ".txt");
        Test test;

        if (testPath.toFile().exists()) {
            test = TestLoader.load(testPath);
        }
        else {
            log.warn("Попытка вызвать несуществующий тест {} ", testPath);
            test = TestLoader.loadDefault();
        }
        Session session = new Session(userId, test);
        sessions.getOrCreate(userId, session);
        sendNextQuestion(session, cb.getMessage().getChatId());
    }

    private void sendNextQuestion(Session session, long chatId) throws TelegramApiException {
        Test test = session.getTest();
        Optional<String> q = test.nextQuestion();
        if (q.isPresent()) {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(chatId));
            sm.setText(q.get());
            sm.setReplyMarkup(KeyboardFactory.answerButtons());
            execute(sm);
        } else if (test.isFinished()) {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(chatId));
            sm.setText(test.result().toString());
            execute(sm);
            sessions.remove(session.getUserId());

            sendMainMenu("Если хотите - выберете следующий тест", chatId);
        }
    }

    private void sendMainMenu(String text, Long chatId) throws TelegramApiException {
        SendMessage sm = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(new KeyboardFactory().mainMenu())
                .build();
        execute(sm);
    }

    @Override
    public String getBotUsername() {
        return name;
    }
}

