package bot.core.validator;

import bot.core.Main;
import bot.core.control.TimerController;
import bot.core.model.MessageContext;
import bot.core.model.SessionController;
import bot.core.util.ChatUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class Validator {
    private static final Logger log = LoggerFactory.getLogger(Validator.class);
    public boolean isValidPayment(Message message) {
        if (message.hasDocument()) {
            Document document = message.getDocument();
            String fileName = document.getFileName();
            if (fileName.endsWith(".pdf")) {
                try {
                    log.info("Получен файл {}", fileName);
                    String fileId = document.getFileId();
                    GetFile getFileMethod = new GetFile(fileId);
                    String fileUrl = Main.bot.execute(getFileMethod).getFileUrl(Main.dataUtils.getBotToken());

                    return validatePDFText(extractTextFromPDF(new URL(fileUrl).openStream()));
                } catch (TelegramApiException e) {
                    Main.log.error("Ошибка при получении файла {}", fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }

    public void sendOuHumanValidation(MessageContext ctx) {
        long userId = ctx.getFromId();
        Long groupId = SessionController.getInstance().getUserSession(ctx.getFromId()).getGroupId();

        TimerController.addTimer(userId, groupId, TimerController.STANDARD_TIME);

        try {
            ForwardMessage forwardMessage = new ForwardMessage();
            forwardMessage.setChatId(Main.dataUtils.getAdminId());
            forwardMessage.setFromChatId(ctx.getChatId());
            forwardMessage.setMessageId(ctx.getMessage().getMessageId());
            Message forwardedMessage = Main.bot.execute(forwardMessage);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(Main.dataUtils.getAdminId());

            sendMessage.setText("Заявка в чат " + "<a href=\"" + createInviteLink(groupId) + "\">" + Main.dataUtils.getGroupName(groupId) + "</a>");
            sendMessage.setParseMode("HTML");
            sendMessage.setReplyMarkup(ChatUtils.getValidationKeyboard(forwardedMessage.getMessageId(), userId));
            Main.bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            Main.log.error("Ошибка при отправке сообщения администратору", e);
        }
    }
    private static String createInviteLink(Long groupId) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setCreatesJoinRequest(true);
        return Main.bot.execute(link).getInviteLink();
    }

    public String extractTextFromPDF(PDDocument document) {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        try {
            return pdfStripper.getText(document);
        } catch (IOException e) {
            Main.log.error("Ошибка при получении текста из PDF", e);
        }
        return "";
    }

    public String extractTextFromPDF(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            return extractTextFromPDF(document);
        } catch (IOException e) {
            Main.log.error("Ошибка при получении текста из PDF", e);
        }
        return "";
    }

    public String extractTextFromPDF(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            return extractTextFromPDF(document);
        } catch (IOException e) {
            Main.log.error("Ошибка при получении текста из PDF", e);
        }
        return "";
    }

    public boolean validatePDFText(String textFromDocument) {
        if (textFromDocument.isEmpty()) {
            return false;
        } else {
            boolean hasCheck = textFromDocument.contains("Чек");
            boolean hasName = (textFromDocument.contains("Елена Алексеевна") ||
                                textFromDocument.contains("Елена Алексеевна") ||
                                    textFromDocument.contains("Елена C."));
            boolean hasSum = textFromDocument.contains("Сумма");
            return hasCheck && hasName && hasSum;
        }
    }
}
