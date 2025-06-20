package bot.core.control;

import bot.core.Main;
import bot.core.model.MessageContext;
import bot.core.util.ChatUtils;
import com.sun.jdi.request.StepRequest;
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
    Document document;
    String documentText;
    public boolean isValidPayment(Message message) {
        if (message.hasDocument()) {
            document = message.getDocument();
            String fileName = document.getFileName();
            if (fileName.endsWith(".pdf")) {
                try {
                    log.info("Получен файл {}", fileName);
                    String fileId = document.getFileId();
                    GetFile getFileMethod = new GetFile(fileId);
                    String fileUrl = Main.bot.execute(getFileMethod).getFileUrl(Main.dataUtils.getBotToken());
                    documentText = extractTextFromPDF(new URL(fileUrl).openStream());
                    return validatePDFText();
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

    public boolean validatePDFText() {
        if (documentText.isEmpty())
            return false;
        return checkTitle(documentText) &&
                checkName(documentText) &&
                checkSum(documentText);
    }

    private boolean checkTitle(String documentText) {
        return documentText.contains("Чек") ||
                documentText.contains("чек") ||
                documentText.contains("Transfer") ||
                documentText.contains("transfer");
    }

    private boolean checkName(String documentText) {
        return  documentText.contains("Елена Алексеевна") ||
                documentText.contains("Елена C.") ||
                documentText.contains("Милана С.") ||
                documentText.contains("Милана Дмитриевна") ||
                documentText.contains("DEBA PRASAD DASH");

    }

    private boolean checkSum(String documentText) {
        return documentText.contains("Сумма") ||
                documentText.contains("Amount");
    }
}
