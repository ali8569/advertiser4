package ir.markazandroid.advertiser.hardware;

/**
 * Coded by Ali on 5/26/2018.
 */
public class InputParser {

    public interface CommandDetectListener {
        void onCommandDetected(String cmd);
    }

    public interface CharDetector {
        boolean isStartChar(char ch);

        boolean isEndChar(char ch);
    }

    private char startDelimiter;
    private char endDelimiter;
    private CommandDetectListener detectListener;
    private CharDetector charDetector;
    private StringBuilder commandBuilder;
    private boolean started;
    private boolean includeStart = false;
    private boolean includeEnd = false;


    public InputParser(char startDelimiter, char endDelimiter, CommandDetectListener detectListener) {
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.detectListener = detectListener;
    }

    public InputParser(char delimiter, CommandDetectListener detectListener) {
        this.startDelimiter = delimiter;
        this.endDelimiter = delimiter;
        this.detectListener = detectListener;
    }

    public InputParser(CommandDetectListener detectListener, CharDetector charDetector) {
        this.detectListener = detectListener;
        this.charDetector = charDetector;
    }

    public void init() {
        if (charDetector == null) {
            charDetector = new CharDetector() {
                @Override
                public boolean isStartChar(char ch) {
                    return ch == startDelimiter;
                }

                @Override
                public boolean isEndChar(char ch) {
                    return ch == endDelimiter;
                }
            };
        }
        started = false;
        commandBuilder = new StringBuilder();
    }


    public synchronized void addInput(String input) {
        for (char in : input.toCharArray()) {

            if (!started) {
                if (charDetector.isStartChar(in)) {
                    started = true;
                    if (!includeStart) continue;
                }
            } else {
                if (charDetector.isEndChar(in)) {
                    started = false;
                    if (includeEnd) commandBuilder.append(in);
                    detectListener.onCommandDetected(commandBuilder.toString());
                    commandBuilder = new StringBuilder();
                }
            }

            if (started) commandBuilder.append(in);
        }
    }


    public CharDetector getCharDetector() {
        return charDetector;
    }

    public void setCharDetector(CharDetector charDetector) {
        this.charDetector = charDetector;
    }

    public char getStartDelimiter() {
        return startDelimiter;
    }

    public void setStartDelimiter(char startDelimiter) {
        this.startDelimiter = startDelimiter;
    }

    public char getEndDelimiter() {
        return endDelimiter;
    }

    public void setEndDelimiter(char endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    public CommandDetectListener getDetectListener() {
        return detectListener;
    }

    public void setDetectListener(CommandDetectListener detectListener) {
        this.detectListener = detectListener;
    }

    public boolean isIncludeStart() {
        return includeStart;
    }

    public void setIncludeStart(boolean includeStart) {
        this.includeStart = includeStart;
    }

    public boolean isIncludeEnd() {
        return includeEnd;
    }

    public void setIncludeEnd(boolean includeEnd) {
        this.includeEnd = includeEnd;
    }
}
