# QuizBot

This project provides a simple Telegram bot that runs a questionnaire based on Ayurvedic dosha principles. All questions are loaded from `data/dosha_test.txt` and sessions are stored only in memory.

## Running

1. Configure your Telegram bot token and name in `DataUtils` or start the bot with your own runner.
2. Build with Maven `mvn package` (Java 21 is required).
3. Launch `quizbot.core.QuizBot` providing the token and bot name.

## Tests

Unit and integration tests are located under `src/test/java/quizbot`. They cover file parsing, question flow and basic bot interaction.

