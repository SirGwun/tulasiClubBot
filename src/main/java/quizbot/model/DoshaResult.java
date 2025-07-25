package quizbot.model;

import java.util.Arrays;
import java.util.Comparator;

public class DoshaResult {
    private final int vata;
    private final int pitta;
    private final int kapha;

    public DoshaResult(int vata, int pitta, int kapha) {
        this.vata = vata;
        this.pitta = pitta;
        this.kapha = kapha;
    }

    public int getVata() {
        return vata;
    }

    public int getPitta() {
        return pitta;
    }

    public int getKapha() {
        return kapha;
    }

    public String dominant() {
        int[] scores = {vata, pitta, kapha};
        int max = Arrays.stream(scores).max().orElse(0);
        long countMax = Arrays.stream(scores).filter(s -> s == max).count();
        if (countMax > 1) {
            return "Mixed"; // unusual but handle gracefully
        }
        if (max == vata) return "Вата";
        if (max == pitta) return "Питта";
        return "Капха";
    }

    public boolean isDoubleType() {
        int[] scores = {vata, pitta, kapha};
        int max = Arrays.stream(scores).max().orElse(0);
        int second = Arrays.stream(scores)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst().orElse(0);
        return max > 0 && Math.abs(max - second) <= max * 0.1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Вата: ").append(vata).append("\n")
          .append("Питта: ").append(pitta).append("\n")
          .append("Капха: ").append(kapha).append("\n\n");
        sb.append("Превалирует - ").append(dominant()).append('.');
        return sb.toString();
    }
}
