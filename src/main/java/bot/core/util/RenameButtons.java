package bot.core.util;

import bot.core.Main;
import bot.core.model.Group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameButtons {

    public void doIt() {
        Map<String, String> exceptions = new HashMap<>();
        exceptions.put("1.ЙОГА и ПРАНАЯМА с ЕЛЕНОЙ МОСКОВКИНОЙ", "1. Йога и пранаяма");
        exceptions.put("7.ОЧИЩЕНИЕ ПЕЧЕНИ", "7. Очищение печени");
        exceptions.put("6.УХОД ЗА КОЖЕЙ и волосами", "6. Уход за кожей и волосами");
        exceptions.put("5.КУЛИНАРНЫЙ ПРАКТИКУМ", "5. Кулинарный практикум");
        exceptions.put("4.ЗДОРОВЬЕ КАЖДОГО ДНЯ", "4. Здоровье каждого дня");
        exceptions.put("2. ОПРЕДЕЛЕНИЕ КОНСТИТУЦИИ", "2. Определение конституции");
        exceptions.put("3.БАЛАНС ВАТА-ДОШИ", "3. Баланс Вата-доши");
        exceptions.put("6. Шестая лекция профессора Дева Прасад Даша. \"Менопауза. Рекомендации аюрведы\"", "6. Менопауза. Рекомендации аюрведы");
        exceptions.put("29. Двадцать девятая лекция профессора Дева Прасад Даша. Панча - карма", "29. Панча-карма");
        exceptions.put("8. Восьмая лекция. Расаяна, продолжение темы", "8. Расаяна (продолжение)");

        System.out.println("=== РЕЗУЛЬТАТ ПЕРЕИМЕНОВАНИЯ ===\n");
        List<Group> lectures = Main.dataUtils.getGroupList();

        for (Group lecture : lectures) {
            String newName = renameLecture(lecture.getName(), exceptions);
            lecture.setName(newName);
        }
        Main.dataUtils.saveGroupList();
    }

    public static String renameLecture(String lecture, Map<String, String> exceptions) {
        // Проверяем, есть ли точное совпадение в исключениях
        if (exceptions.containsKey(lecture)) {
            return exceptions.get(lecture);
        }

        // Извлекаем номер лекции
        String number = extractNumber(lecture);

        // Проверяем, содержит ли название аюрведу или лекцию
        if (containsAyurvedaOrLecture(lecture)) {
            return number + ". Лекция по аюрведе";
        }

        // Если ничего не подошло, возвращаем оригинал (можно добавить обработку)
        return lecture;
    }

    private static String extractNumber(String lecture) {
        // Ищем номер в начале строки или после "Второй поток"
        Pattern pattern = Pattern.compile("(?:Второй поток\\s*)?(\\d+)\\.");
        Matcher matcher = pattern.matcher(lecture);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Если номер не найден, возвращаем пустую строку
        return "";
    }

    private static boolean containsAyurvedaOrLecture(String lecture) {
        String lower = lecture.toLowerCase();

        // Ключевые слова, указывающие на лекцию по аюрведе
        String[] keywords = {
                "лекция", "профессор", "аюрвед", "дева прасад", "дебапрасад",
                "омоложение", "курс", "обучени", "расаяна", "панча", "карма"
        };

        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
