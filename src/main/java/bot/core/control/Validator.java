package bot.core.control;

import bot.core.Legacy;
import bot.core.model.input.MessageContext;
import bot.core.model.TimerController;
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
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;


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
                    File file = Legacy.paymentBot.execute(new GetFile(fileId));
                    InputStream is = Legacy.paymentBot.downloadFileAsStream(file);
                    documentText = extractTextFromPDF(is);

                    return validatePDFText();
                } catch (TelegramApiException e) {
                    Legacy.log.error("Ошибка при получении файла {}", fileName);
                }
            }
        }
        return false;
    }

    public void sendOuHumanValidation(MessageContext ctx) {
        long userId = ctx.getFromId();
        Long groupId = SessionController.getInstance().getUserSession(ctx.getFromId()).getGroupId();

        if (Legacy.dataUtils.getTimerMinutes() != -1) {
            TimerController.addTimer(userId, groupId, Legacy.dataUtils.getTimerMinutes());
        } else {
            log.debug("Таймер не добавлен тк отключен");
        }

        try {
            ForwardMessage forwardMessage = new ForwardMessage();
            forwardMessage.setChatId(Legacy.dataUtils.getAdminId());
            forwardMessage.setFromChatId(ctx.getChatId());
            forwardMessage.setMessageId(ctx.message().getMessageId());
            Message forwardedMessage = Legacy.paymentBot.execute(forwardMessage);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(Legacy.dataUtils.getAdminId());

            sendMessage.setText("Заявка в чат " + "<a href=\"" + createInviteLink(groupId) + "\">" + Legacy.dataUtils.getGroupName(groupId) + "</a>");
            sendMessage.setParseMode("HTML");
            sendMessage.setReplyMarkup(ChatUtils.getValidationKeyboard(forwardedMessage.getMessageId(), userId));
            sendMessage.setDisableWebPagePreview(true);

            Legacy.paymentBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            Legacy.log.error("Ошибка при отправке сообщения администратору", e);
        }
    }
    private static String createInviteLink(Long groupId) throws TelegramApiException {
        CreateChatInviteLink link = new CreateChatInviteLink();
        link.setChatId(groupId);
        link.setCreatesJoinRequest(true);
        return Legacy.paymentBot.execute(link).getInviteLink();
    }

    public String extractTextFromPDF(PDDocument document) {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        try {
            return pdfStripper.getText(document);
        } catch (IOException e) {
            Legacy.log.error("Ошибка при получении текста из PDF", e);
        }
        return "";
    }

    public String extractTextFromPDF(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            return extractTextFromPDF(document);
        } catch (IOException e) {
            Legacy.log.error("Ошибка при получении текста из PDF", e);
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
