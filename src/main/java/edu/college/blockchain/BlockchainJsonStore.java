package edu.college.blockchain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads and writes the chain as plain JSON without third-party libraries. */
public final class BlockchainJsonStore {
    private BlockchainJsonStore() {
    }

    public static void save(Path file, List<Block> blocks) throws IOException {
        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            json.append("  {\n")
                    .append("    \"index\": ").append(block.getIndex()).append(",\n")
                    .append("    \"timestamp\": ").append(block.getTimestamp()).append(",\n")
                    .append("    \"data\": \"").append(escape(block.getData())).append("\",\n")
                    .append("    \"previousHash\": \"").append(escape(block.getPreviousHash())).append("\",\n")
                    .append("    \"nonce\": ").append(block.getNonce()).append(",\n")
                    .append("    \"hash\": \"").append(escape(block.getHash())).append("\"\n")
                    .append("  }");
            if (i < blocks.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("]\n");
        Files.writeString(absoluteFile, json, StandardCharsets.UTF_8);
    }

    public static List<Block> load(Path file) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        Object parsed = new JsonParser(json).parse();
        if (!(parsed instanceof List<?> values)) {
            throw new IOException("The saved blockchain must be a JSON array.");
        }

        List<Block> blocks = new ArrayList<>();
        try {
            for (Object value : values) {
                if (!(value instanceof Map<?, ?> fields)) {
                    throw new IOException("Each item in the saved blockchain must be a JSON object.");
                }
                blocks.add(new Block(
                        number(fields, "index"),
                        number(fields, "timestamp"),
                        string(fields, "data"),
                        string(fields, "previousHash"),
                        number(fields, "nonce"),
                        string(fields, "hash")));
            }
        } catch (IllegalArgumentException exception) {
            throw new IOException("The saved blockchain has missing or invalid fields: " + exception.getMessage(), exception);
        }
        return blocks;
    }

    private static long number(Map<?, ?> fields, String key) {
        Object value = fields.get(key);
        if (value instanceof Long number) {
            return number;
        }
        throw new IllegalArgumentException("'" + key + "' must be an integer.");
    }

    private static String string(Map<?, ?> fields, String key) {
        Object value = fields.get(key);
        if (value instanceof String text) {
            return text;
        }
        throw new IllegalArgumentException("'" + key + "' must be text.");
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }

    /** A tiny parser for the JSON arrays, objects, strings, and integers used by this project. */
    private static final class JsonParser {
        private final String input;
        private int position;

        private JsonParser(String input) {
            this.input = input;
        }

        private Object parse() throws IOException {
            Object value = parseValue();
            skipWhitespace();
            if (position != input.length()) {
                fail("Unexpected characters after the JSON value.");
            }
            return value;
        }

        private Object parseValue() throws IOException {
            skipWhitespace();
            if (position >= input.length()) {
                fail("Unexpected end of JSON.");
            }
            return switch (input.charAt(position)) {
                case '[' -> parseArray();
                case '{' -> parseObject();
                case '"' -> parseString();
                default -> parseInteger();
            };
        }

        private List<Object> parseArray() throws IOException {
            expect('[');
            List<Object> values = new ArrayList<>();
            skipWhitespace();
            if (take(']')) {
                return values;
            }
            while (true) {
                values.add(parseValue());
                skipWhitespace();
                if (take(']')) {
                    return values;
                }
                expect(',');
            }
        }

        private Map<String, Object> parseObject() throws IOException {
            expect('{');
            Map<String, Object> fields = new LinkedHashMap<>();
            skipWhitespace();
            if (take('}')) {
                return fields;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                fields.put(key, parseValue());
                skipWhitespace();
                if (take('}')) {
                    return fields;
                }
                expect(',');
            }
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (position < input.length()) {
                char character = input.charAt(position++);
                if (character == '"') {
                    return result.toString();
                }
                if (character < 0x20) {
                    fail("Unescaped control character in a JSON string.");
                }
                if (character != '\\') {
                    result.append(character);
                    continue;
                }
                if (position >= input.length()) {
                    fail("Incomplete escape sequence.");
                }
                char escape = input.charAt(position++);
                switch (escape) {
                    case '"', '\\', '/' -> result.append(escape);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> result.append(parseUnicodeEscape());
                    default -> fail("Invalid JSON escape sequence.");
                }
            }
            fail("Unterminated JSON string.");
            return ""; // Unreachable; keeps the compiler's flow analysis satisfied.
        }

        private char parseUnicodeEscape() throws IOException {
            if (position + 4 > input.length()) {
                fail("Incomplete Unicode escape.");
            }
            try {
                char decoded = (char) Integer.parseInt(input.substring(position, position + 4), 16);
                position += 4;
                return decoded;
            } catch (NumberFormatException exception) {
                throw new IOException("Invalid Unicode escape in JSON.", exception);
            }
        }

        private Long parseInteger() throws IOException {
            int start = position;
            if (position < input.length() && input.charAt(position) == '-') {
                position++;
            }
            int digitStart = position;
            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                position++;
            }
            if (digitStart == position) {
                fail("Expected a JSON integer.");
            }
            try {
                return Long.parseLong(input.substring(start, position));
            } catch (NumberFormatException exception) {
                throw new IOException("JSON integer is outside the supported range.", exception);
            }
        }

        private void skipWhitespace() {
            while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
                position++;
            }
        }

        private void expect(char expected) throws IOException {
            skipWhitespace();
            if (!take(expected)) {
                fail("Expected '" + expected + "'.");
            }
        }

        private boolean take(char expected) {
            if (position < input.length() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void fail(String message) throws IOException {
            throw new IOException(message + " At character " + position + ".");
        }
    }
}
