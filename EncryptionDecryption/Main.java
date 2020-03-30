package encryptdecrypt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

interface Coder {
    String encrypt(String message, int key);

    String decrypt(String message, int key);
}

class CoderStore {
    static Coder getCoder(String alg) {
        switch (alg.toLowerCase()) {
            case "unicode":
                return new UnicodeCoder();
            case "shift":
                return new ShiftCoder();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + alg);
        }
    }
}

class UnicodeCoder implements Coder {
    @Override
    public String encrypt(String message, int key) {
        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] += key;
        }
        return new String(chars);
    }

    @Override
    public String decrypt(String message, int key) {
        return encrypt(message, -key);
    }
}

class ShiftCoder implements Coder {
    @Override
    public String encrypt(String message, int key) {
        key %= 26;
        char[] data = message.toCharArray();
        for (int i = 0; i < data.length; i++) {
            char c = data[i];
            if ('a' <= c && c <= 'z') {
                data[i] = (char) ((c - 'a' + key + 26) % 26 + 'a');
            } else if ('A' <= c && c <= 'Z') {
                data[i] = (char) ((c - 'A' + key + 26) % 26 + 'A');
            }
        }
        return new String(data);
    }

    @Override
    public String decrypt(String message, int key) {
        return encrypt(message, -key);
    }
}

class IoUtil {
    static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    static void writeToFile(String fileName, String data) throws IOException {
        try (var fw = new FileWriter(fileName)) {
            fw.write(data);
        }
    }
}

abstract class Template {
    String message;
    int key;
    String mode;
    Coder coder;
    String convertedMessage;

    void execute(String[] args) throws Exception {
        parseArgs(args);
        readMessage();
        convertMessage();
        writeMessage();
    }

    abstract void parseArgs(String[] args) throws Exception;

    abstract void readMessage() throws Exception;

    abstract void writeMessage() throws Exception;

    void convertMessage() {
        switch (mode) {
            case "enc":
                convertedMessage = coder.encrypt(message, key);
                break;
            case "dec":
                convertedMessage = coder.decrypt(message, key);
                break;
            default:
                throw new IllegalArgumentException("Unknown mode : " + mode);
        }
    }
}

class Worker extends Template {
    String inFilename;
    String outFilename;

    private static int findPos(String[] args, String option) {
        for (int i = 0; i < args.length; i++) {
            if (option.equals(args[i])) return i;
        }
        return -1;
    }

    @Override
    void parseArgs(String[] args) throws IllegalArgumentException {
        int ixMode = findPos(args, "-mode");
        mode = ixMode > -1 ? args[ixMode + 1] : "enc";

        int ixKey = findPos(args, "-key");
        key = ixKey > -1 ? Integer.parseInt(args[ixKey + 1]) : 0;

        int ixData = findPos(args, "-data");
        message = ixData > -1 ? args[ixData + 1] : null;

        int ixIn = findPos(args, "-in");
        inFilename = ixIn > -1 ? args[ixIn + 1] : null;

        int ixOut = findPos(args, "-out");
        outFilename = ixOut > -1 ? args[ixOut + 1] : null;

        int ixAlg = findPos(args, "-alg");
        String alg = ixAlg > -1 ? args[ixAlg + 1] : "shift";
        coder = CoderStore.getCoder(alg);
    }

    @Override
    void readMessage() throws Exception {
        if (message == null) {
            if (inFilename != null) {
                message = IoUtil.readFileAsString(inFilename);
            } else {
                message = "";
            }
        }
    }

    @Override
    void writeMessage() throws Exception {
        if (outFilename == null) {
            System.out.println(convertedMessage);
        } else {
            IoUtil.writeToFile(outFilename, convertedMessage);
        }
    }
}

public class Main {

    public static void main(String[] args) {
        try {
            Worker worker = new Worker();
            worker.execute(args);
        } catch (Exception e) {
            System.out.println("Error : " +
                    e.getClass().getName() + " - " + e.getMessage());
        }
    }
}
