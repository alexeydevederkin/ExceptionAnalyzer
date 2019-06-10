import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;


public class ExceptionAnalyzer {

    static class FastReader {
        BufferedReader br;
        StringTokenizer st;
        String line;
        int linePosition;

        FastReader() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        char nextChar() {
            if (line == null || linePosition > line.length()) {
                line = nextLine();
                linePosition = 0;
            }

            // EOF
            if (line == null) {
                return '\0';
            }

            // Return '\n' in case of empty line or when line ended
            if (linePosition == line.length()) {
                linePosition++;
                return '\n';
            }

            return line.charAt(linePosition++);
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }

        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }


    private static Map<String, Function> functions = new HashMap<>();


    static class Exception {
        String name;

        Exception(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Exception exception = (Exception) o;
            return name.equals(exception.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static class TryBlock {
        Set<Exception> thrown;
        Set<String> functionCalls;
        Set<TryBlock> tryBlocks;
        Set<Exception> suppressed;

        TryBlock() {
            this.thrown = new HashSet<>();
            this.functionCalls = new HashSet<>();
            this.tryBlocks = new HashSet<>();
            this.suppressed = new HashSet<>();
        }
    }

    static class Function {
        String name;
        Set<Exception> exceptions;
        Set<TryBlock> tryBlocks;
        Set<String> functionCalls;
        Set<Exception> cachedThrownExceptions;
        boolean cached;

        Function(String name) {
            this.name = name;
            this.exceptions = new HashSet<>();
            this.tryBlocks = new HashSet<>();
            this.functionCalls = new HashSet<>();
            this.cachedThrownExceptions = new HashSet<>();
            this.cached = false;
        }
    }


    enum Token {
        FUNC_KEYWORD,
        NAME,
        PAREN_OPEN,
        PAREN_CLOSE,
        BRACKET_OPEN,
        BRACKET_CLOSE,
        MAYBETHROW_KEYWORD,
        TRY_KEYWORD,
        SUPPRESS_KEYWORD,
        COMMA,
        EOF;

        String value;
    }


    static class Lexer {
        private static final Map<String, Token> keywordToToken;

        static {
            keywordToToken = new HashMap<>();
            keywordToToken.put("func", Token.FUNC_KEYWORD);
            keywordToToken.put("try", Token.TRY_KEYWORD);
            keywordToToken.put("suppress", Token.SUPPRESS_KEYWORD);
            keywordToToken.put("maybethrow", Token.MAYBETHROW_KEYWORD);
        }

        private FastReader reader;
        private char currentChar;
        private char[] buffer = new char[1024];
        private int bufPos = 0;

        Lexer(FastReader reader) {
            this.reader = reader;
            this.currentChar = reader.nextChar();
        }

        void advance() {
            currentChar = reader.nextChar();
        }

        Token bufferedToken() {
            String tokenString = String.copyValueOf(buffer, 0, bufPos);
            Token token;

            if (keywordToToken.containsKey(tokenString)) {
                token = keywordToToken.get(tokenString);
            } else {
                token = Token.NAME;
                token.value = tokenString;
            }

            bufPos = 0;

            return token;
        }


        Token getNextToken() {

            while (currentChar != 0) {

                switch (currentChar) {
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        if (bufPos > 0) {
                            return bufferedToken();
                        } else {
                            advance();
                            break;
                        }
                    case '(':
                        if (bufPos > 0) {
                            return bufferedToken();
                        } else {
                            advance();
                            return Token.PAREN_OPEN;
                        }
                    case ')':
                        if (bufPos > 0) {
                            return bufferedToken();
                        } else {
                            advance();
                            return Token.PAREN_CLOSE;
                        }
                    case '{':
                        if (bufPos > 0) {
                            return bufferedToken();
                        } else {
                            advance();
                            return Token.BRACKET_OPEN;
                        }
                    case '}':
                        if (bufPos > 0) {
                            return bufferedToken();
                        } else {
                            advance();
                            return Token.BRACKET_CLOSE;
                        }
                    case ',':
                        if (bufPos > 0) {
                            return bufferedToken();
                        } else {
                            advance();
                            return Token.COMMA;
                        }
                    default:
                        buffer[bufPos++] = currentChar;
                        advance();
                }
            }

            if (bufPos > 0) {
                return bufferedToken();
            }

            return Token.EOF;
        }
    }



    static class Parser {
        Lexer lexer;
        Token currentToken;
        int numberOfFunctions;

        Parser(Lexer lexer, int numberOfFunctions) {
            this.lexer = lexer;
            this.numberOfFunctions = numberOfFunctions;
        }

        void error(String message) {
            System.out.println(message);
            System.exit(1);
        }

        void eat(Token expectedToken) {
            if (currentToken != expectedToken) {
                error("PARSING ERROR: UNEXPECTED TOKEN: " + currentToken + " instead of " + expectedToken);
            }
            currentToken = lexer.getNextToken();
        }

        void parseTryBlock(TryBlock tryBlock) {
            eat(Token.BRACKET_OPEN);

            String tokenValue;
            boolean tryBlockParsed = false;
            while (!tryBlockParsed) {
                switch (currentToken) {
                    case MAYBETHROW_KEYWORD:
                        eat(Token.MAYBETHROW_KEYWORD);
                        tokenValue = currentToken.value;
                        eat(Token.NAME);
                        tryBlock.thrown.add(new Exception(tokenValue));
                        break;
                    case TRY_KEYWORD:
                        eat(Token.TRY_KEYWORD);
                        TryBlock tryBlockInternal = new TryBlock();
                        tryBlock.tryBlocks.add(tryBlockInternal);
                        parseTryBlock(tryBlockInternal);
                        break;
                    case NAME:
                        tokenValue = currentToken.value;
                        eat(Token.NAME);
                        tryBlock.functionCalls.add(tokenValue);
                        eat(Token.PAREN_OPEN);
                        eat(Token.PAREN_CLOSE);
                        break;
                    case BRACKET_CLOSE:
                        eat(Token.BRACKET_CLOSE);
                        tryBlockParsed = true;
                        break;
                    default:
                        error("PARSING ERROR: UNEXPECTED TOKEN: " + currentToken);
                }
            }

            eat(Token.SUPPRESS_KEYWORD);

            // First exception: "suppress exc1"
            tokenValue = currentToken.value;
            eat(Token.NAME);
            tryBlock.suppressed.add(new Exception(tokenValue));

            // Other exceptions: "suppress exc1 , exc2 , exc3"
            while (currentToken == Token.COMMA) {
                eat(Token.COMMA);
                tokenValue = currentToken.value;
                eat(Token.NAME);
                tryBlock.suppressed.add(new Exception(tokenValue));
            }
        }

        void parseFunction() {
            // eating any token at first = getting the first token of program OR eating "}" of prev func
            eat(currentToken);

            eat(Token.FUNC_KEYWORD);

            String tokenValue = currentToken.value;
            eat(Token.NAME);

            Function func = new Function(tokenValue);
            functions.put(tokenValue, func);

            eat(Token.PAREN_OPEN);
            eat(Token.PAREN_CLOSE);
            eat(Token.BRACKET_OPEN);

            boolean functionParsed = false;
            while (!functionParsed) {
                switch (currentToken) {
                    case MAYBETHROW_KEYWORD:
                        eat(Token.MAYBETHROW_KEYWORD);
                        tokenValue = currentToken.value;
                        eat(Token.NAME);
                        func.exceptions.add(new Exception(tokenValue));
                        break;
                    case TRY_KEYWORD:
                        eat(Token.TRY_KEYWORD);
                        TryBlock tryBlock = new TryBlock();
                        func.tryBlocks.add(tryBlock);
                        parseTryBlock(tryBlock);
                        break;
                    case NAME:
                        tokenValue = currentToken.value;
                        eat(Token.NAME);
                        func.functionCalls.add(tokenValue);
                        eat(Token.PAREN_OPEN);
                        eat(Token.PAREN_CLOSE);
                        break;
                    case BRACKET_CLOSE:
                        // Not eating "}" - or will freeze on the last "}" of program (waiting for input)
                        //eat(Token.BRACKET_CLOSE);
                        functionParsed = true;
                        break;
                    default:
                        error("PARSING ERROR: UNEXPECTED TOKEN: " + currentToken);
                }
            }
        }

        void parseProgram() {
            for (int i = 0; i < numberOfFunctions; i++) {
                parseFunction();
            }
        }
    }


    private static Set<Exception> throwTryBlock(TryBlock tryBlock, Set<String> callStack) {

        // get exceptions from maybethrows
        Set<Exception> thrownExceptions = new HashSet<>(tryBlock.thrown);

        // add exceptions from func calls
        for (String funcName : tryBlock.functionCalls) {
            thrownExceptions.addAll(throwFunc(funcName, callStack));
        }

        // add exceptions from internal try blocks
        for (TryBlock tryBlockInternal : tryBlock.tryBlocks) {
            thrownExceptions.addAll(throwTryBlock(tryBlockInternal, callStack));
        }

        // exclude exceptions from suppress
        thrownExceptions.removeAll(tryBlock.suppressed);

        return thrownExceptions;
    }

    private static Set<Exception> throwFunc(String funcName, Set<String> callStack) {

        Function func = functions.get(funcName);

        Set<Exception> thrownExceptions = new HashSet<>();

        if (func == null || callStack.contains(funcName)) {
            return thrownExceptions;
        }

        if (func.cached) {
            return func.cachedThrownExceptions;
        }

        callStack.add(funcName);

        // add exceptions from maybethrows
        thrownExceptions.addAll(func.exceptions);

        // add exceptions from tryblocks
        for (TryBlock tryBlock : func.tryBlocks) {
            thrownExceptions.addAll(throwTryBlock(tryBlock, callStack));
        }

        // add exceptions from internal func calls
        for (String fName : func.functionCalls) {
            thrownExceptions.addAll(throwFunc(fName, callStack));
        }

        func.cachedThrownExceptions = thrownExceptions;
        func.cached = true;

        callStack.remove(funcName);

        return thrownExceptions;
    }


    public static void main(String[] args) {
        FastReader reader = new FastReader();
        PrintWriter out = new PrintWriter(System.out);

        int n = reader.nextInt();

        if (n < 1) {
            System.out.println("");
            return;
        }

        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer, n);

        parser.parseProgram();

        Set<Exception> exceptions = throwFunc("main", new HashSet<>());

        out.println(exceptions.size());
        exceptions.forEach(out::println);

        out.flush();
    }
}