/* https://hyperskill.org/projects/66?goal=7
 * Inverse index for searching in text
 * Strategy pattern implemented with enum
 */
package search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    private enum Strategy {
        ALL {
            @Override
            Set<Integer> find(Main searchEngine, String[] queries) {
                if (queries.length == 0) return Set.of();
                List<Integer> first = searchEngine.invIndex.get(queries[0]);
                if (first == null) return Set.of();
                Set<Integer> res = new HashSet<>(first);
                for (String query : queries) {
                    res.retainAll(searchEngine.invIndex.get(query));
                }
                return res;
            }
        },
        ANY {
            @Override
            Set<Integer> find(Main searchEngine, String[] queries) {
                Set<Integer> res = new HashSet<>();
                for (String query : queries) {
                    res.addAll(searchEngine.invIndex.get(query));
                }
                return res;
            }
        },
        NONE {
            @Override
            Set<Integer> find(Main searchEngine, String[] queries) {
                Set<Integer> res = IntStream.range(0, searchEngine.lines.size())
                        .boxed().collect(Collectors.toCollection(HashSet::new));
                for (String query : queries) {
                    res.removeAll(searchEngine.invIndex.get(query));
                }
                return res;
            }
        };

        abstract Set<Integer> find(Main searchEngine, String[] queries);

        static String listAvailable() {
            return Arrays.stream(values()).map(Strategy::toString)
                    .collect(Collectors.joining(", "));
        }
    }

    private static final Scanner SCANNER = new Scanner(System.in);

    private List<String> lines;
    private Map<String, List<Integer>> invIndex;

    private void readData(String filename) {
        try {
            lines = Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildIndex() {
        invIndex = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            for (String word : lines.get(i).split(" ")) {
                word = word.toLowerCase();
                if (!invIndex.containsKey(word)) {
                    invIndex.put(word, new ArrayList<>());
                }
                invIndex.get(word).add(i);
            }
        }
    }

    private int menu() {
        while (true) {
            System.out.println("\n=== Menu ===\n" +
                    "1. Find a person\n" +
                    "2. Print all people\n" +
                    "0. Exit");
            int res = SCANNER.nextInt();
            SCANNER.nextLine();
            if (0 <= res && res <= 2) {
                return res;
            } else {
                System.out.println("\nIncorrect option! Try again.");
            }
        }
    }

    private void find() {
        Strategy strategy = askStrategy();
        System.out.println("Enter a name or email to search all suitable people.");
        String[] queries = SCANNER.nextLine().toLowerCase().strip().split("\\s+");
        Set<Integer> res = strategy.find(this, queries);
        if (!res.isEmpty()) {
            for (int i : res) {
                System.out.println(lines.get(i));
            }
        } else {
            System.out.println("No matching people found.");
        }
    }

    private Strategy askStrategy() {
        while (true) {
            System.out.println("\nSelect a matching strategy: " + Strategy.listAvailable());
            String answer = SCANNER.nextLine();
            try {
                return Strategy.valueOf(answer);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown strategy: \"" + answer + "\". Try again.");
            }
        }
    }

    private void print() {
        System.out.println("=== List of people ===");
        for (var line : lines) System.out.println(line);
    }

    public void start(String[] args) {
        readData(args[1]);
        buildIndex();
        int response;
        while ((response = menu()) != 0) {
            switch (response) {
                case 1:
                    find();
                    break;
                case 2:
                    print();
                    break;
            }
        }
        System.out.println("\nBye!");
    }

    public static void main(String[] args) {
        new Main().start(args);
    }
}
