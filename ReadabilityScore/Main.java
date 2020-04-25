/* https://hyperskill.org/projects/39?goal=7
 * # Project: Readability Score
 * ## About
 * Everyone has their own personal reading history, and as we grow up, we are able
 *  to comprehend more and more complicated texts. But how do you estimate the level
 *  of difficulty of a given text, and how do you teach a computer to do that?
 *  In this project, you will find it out: write a program that determines how difficult
 *  the text is and for which age it is most suitable.
* */

package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    String text;
    int words;
    int sentences;
    int characters;
    int syllables;
    int polysyllables;
    Map<Strategy, Double> scores = new HashMap<>();
    double averageAge;

    Main(String filename) throws IOException {
        Path file = Paths.get(filename);
        text = new String(Files.readAllBytes(file));
        countStats();
    }

    private void countStats() {
        words = text.split("[-.,;!?]?\\s+").length;
        sentences = text.split("[.!?]+\\s*").length;
        characters = text.replaceAll("\\s", "").length();
        syllables = Arrays.stream(text.split("[-.,;!?]?\\s+"))
                .mapToInt(Main::countVowelsWord)
                .sum();
        polysyllables = (int) Arrays.stream(text.split("\\s+"))
                .mapToInt(Main::countVowelsWord)
                .filter(i -> i > 2)
                .count();
        for (Strategy s : Strategy.values()) {
            scores.put(s, calcScore(s));
        }
        averageAge = scores.values().stream()
                .collect(Collectors.averagingDouble(Main::getAge));
    }

    enum Strategy {
        ARI("Automated Readability Index"),
        FK("Flesch–Kincaid readability tests"),
        SMOG("Simple Measure of Gobbledygook"),
        CL("Coleman–Liau index");

        String fullName;

        Strategy(String fullName) {
            this.fullName = fullName;
        }
    }

    private double calcScore(Strategy strategy) {
        switch (strategy) {
            case ARI:
                return 4.71 * characters / words + 0.5 * words / sentences - 21.43;
            case FK:
                return 0.39 * words / sentences + 11.8 * syllables / words - 15.59;
            case SMOG:
                return 1.043 * Math.sqrt(polysyllables * 30.0 / sentences) + 3.1291;
            case CL:
                double L = characters * 100.0 / words;
                double S = sentences * 100.0 / words;
                return 0.0588 * L - 0.296 * S - 15.8;
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }

    private void showText() {
        System.out.println("The text is:");
        System.out.println(text);
        System.out.println();
    }

    private void showStats() {
        System.out.format("Words: %d%n" +
                        "Sentences: %d%n" +
                        "Characters: %d%n" +
                        "Syllables: %d%n" +
                        "Polysyllables: %d%n",
                words, sentences, characters, syllables, polysyllables);
    }

    private void showScore(Strategy s) {
        double score = scores.get(s);
        int age = getAge(score);
        System.out.format("%s: %.2f (about %d year olds).%n",
                s.fullName, score, age);
    }

    private void showAllScores() {
        for (Strategy s : Strategy.values()) {
            showScore(s);
        }
    }

    private static int getAge(double score) {
        int iScore = (int) Math.round(score);
        return iScore <= 2 ? iScore + 5
                : iScore <= 12 ? iScore + 6
                : iScore + 11;
    }

    private static int countVowelsWord(String word) {
        word = word.toLowerCase();
        if (word.endsWith("e")) {
            word = word.substring(0, word.length() - 1);
        }
        word = word.replaceAll("[aeiouy]+", "=");
        int syllables = word.length() - word.replaceAll("=", "").length();
        return Math.max(1, syllables);
    }

    private void showMenuAndScore() {
        while (true) {
            System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
            String answer = SCANNER.next();
            System.out.println();
            try {
                if ("all".equals(answer)) {
                    showAllScores();
                } else {
                    Strategy s = Strategy.valueOf(answer);
                    showScore(s);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Can't parse the option: " + answer);
                continue;
            }
            break;
        }
        System.out.format("%nThis text should be understood in average by %.2f year olds.%n",
                averageAge);
    }

    public static void main(String[] args) throws IOException {
        Main readabilityScore = new Main(args[0]);
        readabilityScore.showText();
        readabilityScore.showStats();
        readabilityScore.showMenuAndScore();
    }
}
