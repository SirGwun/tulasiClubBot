package bot.core.util.config.infra;

public class ProductionConfig implements ConfigProvider {
    private static final ConfigProvider INSTANCE = new ProductionConfig();

    public static ConfigProvider getInstance() {
        return INSTANCE;
    }

    private ProductionConfig() {

    }

    @Override
    public String get(String key) {
        return switch (key) {
            case "bot.token" -> System.getenv("BOTTOCKEN");
            case "bot.name" -> System.getenv("BOTNAME");
            case "quiz.token" -> System.getenv("TESTBOTTOCKEN");
            case "quiz.name" -> System.getenv("TESTBOTNAME");
            case "shop.key" -> System.getenv("SHOPKEY");
            case "shop.id" -> System.getenv("SHOPID");
            default -> throw new IllegalArgumentException();
        };
    }
}
