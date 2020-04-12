/* https://hyperskill.org/projects/44?goal=7
 * #Project: Flashcards
 * ##About
 * For foreign language learners, it’s hard to remember new words, which is exactly what flashcards are for.
 * Typically, flashcards show a hint (a task or a picture) on one side and the right answer on the other.
 * Flashcards can be used to remember any sort of data, so if you want to create something useful
 * and enhance your programming skills, this project is just right.
 */
package flashcards;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static class UtilIO {
        static File processFilename(String filename) throws IOException {
            String[] words = filename.split("[/\\\\]");
            File file = new File(words[0]);
            for (int i = 1; i < words.length; i++) {
                if (!file.exists()) {
                    if (!file.mkdir()) {
                        throw new IOException("Cannot create a directory");
                    }
                }
                file = new File(file, words[i]);
            }
            return file;
        }

        static void writeCards(String filename, Main main) throws IOException {
            try (var writer = new PrintWriter(processFilename(filename))) {
                for (var entry : main.term2definition.entrySet()) {
                    String term = entry.getKey();
                    writer.println(term);
                    writer.println(entry.getValue());
                    writer.println(main.errors.get(term));
                }
            }
        }

        static void readCards(String filename, Map<String, String> cards, Map<String, Integer> errors)
                throws IOException {
            List<String> lines = Files.readAllLines(processFilename(filename).toPath());
            for (int i = 0; i < lines.size(); i += 3) {
                String term = lines.get(i);
                String definition = lines.get(i + 1);
                Integer nErr = Integer.valueOf(lines.get(i + 2));
                cards.put(term, definition);
                errors.put(term, nErr);
            }
        }

        static void writeLog(String filename, List<String> logLines) throws IOException {
            try (var writer = new PrintWriter(processFilename(filename))) {
                logLines.forEach(writer::println);
            }
        }
    }

    private static class Logger {
        static final Scanner SCANNER = new Scanner(System.in);
        List<String> logLines = new ArrayList<>();

        void println() {
            println("");
        }

        void println(String out) {
            logLines.add(out);
            System.out.println(out);
        }

        String input() {
            String in = SCANNER.nextLine();
            logLines.add("> " + in);
            return in;
        }

        int inputInt() {
            int in = SCANNER.nextInt(); SCANNER.nextLine();
            logLines.add("> " + in);
            return in;
        }

        void save(String filename) throws IOException {
            UtilIO.writeLog(filename, logLines);
        }
    }

    static final Random RANDOM = new Random();
    List<String> terms = new ArrayList<>();
    Map<String, String> term2definition = new HashMap<>();
    Map<String, String> definition2term = new HashMap<>();
    Map<String, Integer> errors = new HashMap<>();
    Logger log = new Logger();
    String outFilename;

    public Main(String... args) {
        List<String> argsList = List.of(args);
        if (!argsList.isEmpty()) {
            int ixImport = argsList.indexOf("-import");
            if (ixImport > -1) {
                String inFilename = argsList.get(ixImport + 1);
                importCards(inFilename);
            }
            int ixExport = argsList.indexOf("-export");
            if (ixExport > -1) {
                outFilename = argsList.get(ixExport + 1);
            }
        }
    }

    void start() throws IOException {
        do {
            log.println("Input the action " +
                    "(add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String response = log.input();
            switch (response.toLowerCase()) {
                case "add":
                    add();
                    break;
                case "remove":
                    remove();
                    break;
                case "import":
                    importCards(askFilename());
                    break;
                case "export":
                    export(askFilename());
                    break;
                case "ask":
                    ask();
                    break;
                case "log":
                    saveLog();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    resetStats();
                    break;
                default:
                    log.println("Unknown option: " + response);
                    break;
                case "exit":
                    exit();
                    return;
            }
            log.println();
        } while (true);
    }

    private String askFilename() {
        log.println("File name:");
        return log.input();
    }

    private void resetStats() {
        errors.replaceAll((term, err) -> 0);
        log.println("Card statistics has been reset.");
    }

    private void hardestCard() {
        int maxErrors = errors.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maxErrors == 0) {
            log.println("There are no cards with errors.");
        } else {
            List<String> hardest = errors.entrySet().stream()
                    .filter(e -> e.getValue() == maxErrors)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (hardest.size() == 1) {
                log.println(String.format("The hardest card is \"%s\". You have %d errors answering it.",
                        hardest.get(0), maxErrors));
            } else {
                String hardestTerms = hardest.stream().map(w -> '\"' + w + '\"')
                        .collect(Collectors.joining(", "));
                log.println(String.format("The hardest cards are %s. You have %d errors answering them.",
                        hardestTerms, maxErrors));
            }
        }
    }

    private void saveLog() throws IOException {
        log.println("File name:");
        String filename = log.input();
        log.println("The log has been saved.");
        log.save(filename);
    }

    private void export(String filename) throws IOException {
        UtilIO.writeCards(filename, this);
        log.println(String.format("%d cards have been saved.", term2definition.size()));
    }

    private void importCards(String filename) {
        try {
            var newCards = new HashMap<String, String>();
            var newErrors = new HashMap<String, Integer>();
            UtilIO.readCards(filename, newCards, newErrors);
            for (var entry : newCards.entrySet()) {
                String newTerm = entry.getKey();
                String newDefinition = entry.getValue();
                Integer newError = newErrors.get(newTerm);
                if (term2definition.containsKey(newTerm)) {
                    String oldDefinition = term2definition.put(newTerm, newDefinition);
                    definition2term.remove(oldDefinition);
                } else {
                    terms.add(newTerm);
                    term2definition.put(newTerm, newDefinition);
                }
                definition2term.put(newDefinition, newTerm);
                errors.put(newTerm, newError);
            }
            log.println(String.format("%d cards have been loaded.", newCards.size()));
        } catch (IOException e) {
            log.println("File not found.");
        }
    }

    private void remove() {
        log.println("The card:");
        String term;
        if (term2definition.containsKey(term = log.input())) {
            terms.remove(term);
            errors.remove(term);
            String definition = term2definition.remove(term);
            definition2term.remove(definition);
            log.println("The card has been removed.");
        } else {
            log.println(String.format("Can't remove \"%s\": there is no such card.", term));
        }
    }

    private void add() {
        log.println("The card:");
        String term, definition;
        if (term2definition.containsKey(term = log.input())) {
            log.println(String.format("The card \"%s\" already exists.", term));
        } else {
            log.println("The definition of the card:");
            if (definition2term.containsKey(definition = log.input())) {
                log.println(String.format("The definition \"%s\" already exists.", definition));
            } else {
                terms.add(term);
                errors.put(term, 0);
                term2definition.put(term, definition);
                definition2term.put(definition, term);
                log.println(String.format("The pair (\"%s\":\"%s\") has been added.", term, definition));
            }
        }
    }

    private void exit() throws IOException {
        log.println("Bye bye!");
        if (outFilename != null) {
            export(outFilename);
        }
    }

    void ask() {
        log.println("How many times to ask?");
        int nToAsk = log.inputInt();
        while (nToAsk-- > 0) {
            int ix = RANDOM.nextInt(terms.size());
            String term = terms.get(ix);
            String definition = term2definition.get(term);
            log.println(String.format("Print the definition of \"%s\":", term));
            String answer = log.input();
            if (answer.equals(definition)) {
                log.println("Correct answer.");
            } else if (definition2term.containsKey(answer)) {
                log.println(String.format(
                        "Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".",
                        definition, definition2term.get(answer)));
                errors.merge(term, 1, Integer::sum);
            } else {
                log.println(String.format("Wrong answer. The correct one is \"%s\".",
                        definition));
                errors.merge(term, 1, Integer::sum);
            }
        }
    }

    public static void main(String[] args) {
        Main main = new Main(args);
        try {
            main.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
