package tanks.translation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Approximates the set of English strings that may be used as translation keys.
 *
 * <p>This is intentionally heuristic rather than a full Java semantic analyzer. It catches:
 * direct {@code Translation.translate(...)} calls, common GUI setters/constructors that translate
 * their text later, and editor/property annotations whose {@code name}/{@code desc} values are
 * shown through translation elsewhere.</p>
 *
 * <p>Usage: run with an optional source root. If omitted, the extractor prefers {@code java/}
 * under the current working directory, then {@code src/main/java}, then the working directory.</p>
 */
public class TranslationKeyExtractor
{
    private static final List<String> METHOD_SINKS = Arrays.asList("setText", "setSubtext", "setHoverText");
    private static final Set<String> GUI_CONSTRUCTORS = new LinkedHashSet<>(Arrays.asList(
            "Button", "Label", "TextBox", "Selector", "InputSelector"
    ));
    private static final String OUTPUT_LOCATIONS = "locations";
    private static final String OUTPUT_LANG = "lang";

    public static void main(String[] args) throws IOException
    {
        Config config = parseArgs(args);
        Path root = config.root;

        root = root.toAbsolutePath().normalize();

        if (!Files.isDirectory(root))
        {
            System.err.println("Not a directory: " + root);
            System.exit(1);
        }

        TreeMap<String, TreeSet<String>> results = new TreeMap<>();
        int[] fileCount = {0};

        final Path scanRoot = root;

        try (Stream<Path> stream = Files.walk(scanRoot))
        {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .forEach(path ->
                    {
                        fileCount[0]++;
                        try
                        {
                            scanFile(scanRoot, path, results);
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException("Failed to scan " + path, e);
                        }
                    });
        }

        if (OUTPUT_LANG.equals(config.outputMode))
        {
            System.out.println(config.languageName);
            for (String key: results.keySet())
                System.out.println(escapeLangValue(key) + "=");
        }
        else
        {
            for (Map.Entry<String, TreeSet<String>> entry: results.entrySet())
            {
                System.out.println(entry.getKey());
                for (String location: entry.getValue())
                    System.out.println("  " + location);
            }

            System.out.println();
            System.out.println("Scanned " + fileCount[0] + " Java files");
            System.out.println("Found " + results.size() + " unique candidate translation keys");
        }
    }

    private static Config parseArgs(String[] args)
    {
        Config config = new Config();

        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if ("--lang".equals(arg))
            {
                config.outputMode = OUTPUT_LANG;
            }
            else if (arg.startsWith("--format="))
            {
                config.outputMode = arg.substring("--format=".length()).trim().toLowerCase();
            }
            else if ("--format".equals(arg) && i + 1 < args.length)
            {
                config.outputMode = args[++i].trim().toLowerCase();
            }
            else if (arg.startsWith("--language-name="))
            {
                config.languageName = arg.substring("--language-name=".length());
            }
            else if ("--language-name".equals(arg) && i + 1 < args.length)
            {
                config.languageName = args[++i];
            }
            else if (!arg.startsWith("--") && config.root == null)
            {
                config.root = Paths.get(arg);
            }
        }

        if (config.root == null)
        {
            if (Files.isDirectory(Paths.get("java")))
                config.root = Paths.get("java");
            else if (Files.isDirectory(Paths.get("src", "main", "java")))
                config.root = Paths.get("src", "main", "java");
            else
                config.root = Paths.get(".");
        }

        if (!OUTPUT_LOCATIONS.equals(config.outputMode) && !OUTPUT_LANG.equals(config.outputMode))
        {
            System.err.println("Unknown format: " + config.outputMode);
            System.err.println("Supported formats: " + OUTPUT_LOCATIONS + ", " + OUTPUT_LANG);
            System.exit(1);
        }

        return config;
    }

    private static void scanFile(Path root, Path path, TreeMap<String, TreeSet<String>> results) throws IOException
    {
        String text = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        LineMap lines = new LineMap(text);
        String relative = root.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');

        for (int idx: findOccurrences(text, "Translation.translate("))
            extractCallFirstArgument(text, idx + "Translation.translate".length(), relative, lines, results);

        for (String method: METHOD_SINKS)
        {
            String needle = "." + method + "(";
            for (int idx: findOccurrences(text, needle))
                extractCallFirstArgument(text, idx + needle.length() - 1, relative, lines, results);
        }

        for (String type: GUI_CONSTRUCTORS)
        {
            String needle = "new " + type + "(";
            for (int idx: findOccurrences(text, needle))
                extractConstructorStrings(type, text, idx + needle.length() - 1, relative, lines, results);
        }

        for (String annotation: Arrays.asList("@MetadataProperty(", "@Property("))
        {
            for (int idx: findOccurrences(text, annotation))
                extractAnnotationStrings(text, idx + annotation.length() - 1, relative, lines, results);
        }
    }

    private static void extractCallFirstArgument(String text, int openParenIndex, String relative, LineMap lines,
                                                 TreeMap<String, TreeSet<String>> results)
    {
        String argsText = extractBalanced(text, openParenIndex, '(', ')');
        if (argsText == null)
            return;

        List<String> args = splitTopLevel(argsText);
        if (args.isEmpty())
            return;

        addValues(approximateStringValues(args.get(0)), relative, lines.lineNumber(openParenIndex), results);
    }

    private static void extractConstructorStrings(String type, String text, int openParenIndex, String relative,
                                                  LineMap lines, TreeMap<String, TreeSet<String>> results)
    {
        String argsText = extractBalanced(text, openParenIndex, '(', ')');
        if (argsText == null)
            return;

        List<String> args = splitTopLevel(argsText);
        int line = lines.lineNumber(openParenIndex);

        if ("Button".equals(type))
        {
            addArg(args, 4, relative, line, results);

            if (args.size() >= 6 && isProbablyStringExpression(args.get(5)))
                addArg(args, 5, relative, line, results);
            else if (args.size() >= 7)
                addArg(args, 6, relative, line, results);
        }
        else if ("Label".equals(type))
            addArg(args, 3, relative, line, results);
        else if ("TextBox".equals(type))
        {
            addArg(args, 4, relative, line, results);
            addArg(args, 7, relative, line, results);
        }
        else if ("Selector".equals(type))
        {
            addArg(args, 4, relative, line, results);
            addArg(args, 7, relative, line, results);
            addArg(args, 6, relative, line, results);
        }
        else if ("InputSelector".equals(type))
        {
            addArg(args, 4, relative, line, results);
            addArg(args, 6, relative, line, results);
        }
    }

    private static void extractAnnotationStrings(String text, int openParenIndex, String relative, LineMap lines,
                                                 TreeMap<String, TreeSet<String>> results)
    {
        String body = extractBalanced(text, openParenIndex, '(', ')');
        if (body == null)
            return;

        int line = lines.lineNumber(openParenIndex);
        for (String field: Arrays.asList("name", "desc"))
        {
            String expr = findNamedArgumentExpression(body, field);
            if (expr != null)
                addValues(approximateStringValues(expr), relative, line, results);
        }
    }

    private static void addArg(List<String> args, int index, String relative, int line,
                               TreeMap<String, TreeSet<String>> results)
    {
        if (index < 0 || index >= args.size())
            return;

        addValues(approximateStringValues(args.get(index)), relative, line, results);
    }

    private static void addValues(Collection<String> values, String relative, int line,
                                  TreeMap<String, TreeSet<String>> results)
    {
        if (values == null)
            return;

        for (String value: values)
        {
            String key = clean(value);
            if (key == null)
                continue;
            results.computeIfAbsent(key, k -> new TreeSet<>()).add(relative + ":" + line);
        }
    }

    private static String clean(String value)
    {
        if (value == null)
            return null;

        String v = value.trim();
        if (v.isEmpty())
            return null;

        if (v.startsWith("\u00A7"))
        {
            int i = 0;
            while (i + 1 < v.length() && v.charAt(i) == '\u00A7')
            {
                i += 2;
                while (i < v.length() && Character.isDigit(v.charAt(i)))
                    i++;
            }
            v = v.substring(Math.min(i, v.length())).trim();
        }

        if (v.isEmpty())
            return null;

        boolean hasLetter = false;
        for (int i = 0; i < v.length(); i++)
        {
            if (Character.isLetter(v.charAt(i)))
            {
                hasLetter = true;
                break;
            }
        }

        return hasLetter ? v: null;
    }

    private static boolean isProbablyStringExpression(String expr)
    {
        return approximateStringValues(expr) != null;
    }

    private static Set<String> approximateStringValues(String expr)
    {
        if (expr == null)
            return null;

        expr = stripOuterParens(expr.trim());
        if (expr.isEmpty())
            return null;

        if (isStringLiteral(expr))
            return Collections.singleton(unescapeJavaString(expr.substring(1, expr.length() - 1)));

        TernarySplit ternary = splitTopLevelTernary(expr);
        if (ternary != null)
        {
            Set<String> left = approximateStringValues(ternary.trueExpr);
            Set<String> right = approximateStringValues(ternary.falseExpr);
            if (left == null && right == null)
                return null;

            LinkedHashSet<String> out = new LinkedHashSet<>();
            if (left != null)
                out.addAll(left);
            if (right != null)
                out.addAll(right);
            return out;
        }

        List<String> plusParts = splitTopLevelByChar(expr, '+');
        if (plusParts.size() > 1)
        {
            LinkedHashSet<String> built = new LinkedHashSet<>();
            built.add("");

            for (String part: plusParts)
            {
                Set<String> values = approximateStringValues(part);
                if (values == null)
                    return null;

                LinkedHashSet<String> next = new LinkedHashSet<>();
                for (String prefix: built)
                    for (String suffix: values)
                        next.add(prefix + suffix);
                built = next;
            }

            return built;
        }

        return null;
    }

    private static String findNamedArgumentExpression(String body, String fieldName)
    {
        int i = 0;
        while (i < body.length())
        {
            i = skipWhitespace(body, i);
            if (!startsWithIdentifier(body, i, fieldName))
            {
                i++;
                continue;
            }

            int j = skipWhitespace(body, i + fieldName.length());
            if (j >= body.length() || body.charAt(j) != '=')
            {
                i++;
                continue;
            }

            j++;
            int start = skipWhitespace(body, j);
            int end = findTopLevelComma(body, start);
            if (end < 0)
                end = body.length();
            return body.substring(start, end).trim();
        }

        return null;
    }

    private static List<Integer> findOccurrences(String text, String needle)
    {
        ArrayList<Integer> out = new ArrayList<>();
        int i = 0;
        int len = needle.length();

        State state = State.NORMAL;
        while (i < text.length())
        {
            char c = text.charAt(i);

            if (state == State.NORMAL)
            {
                if (c == '"')
                {
                    state = State.STRING;
                    i++;
                    continue;
                }
                if (c == '\'')
                {
                    state = State.CHAR;
                    i++;
                    continue;
                }
                if (c == '/' && i + 1 < text.length())
                {
                    char n = text.charAt(i + 1);
                    if (n == '/')
                    {
                        state = State.LINE_COMMENT;
                        i += 2;
                        continue;
                    }
                    if (n == '*')
                    {
                        state = State.BLOCK_COMMENT;
                        i += 2;
                        continue;
                    }
                }

                if (i + len <= text.length() && text.regionMatches(i, needle, 0, len))
                    out.add(i);

                i++;
            }
            else if (state == State.STRING)
            {
                i = skipQuoted(text, i, '"');
                state = State.NORMAL;
            }
            else if (state == State.CHAR)
            {
                i = skipQuoted(text, i, '\'');
                state = State.NORMAL;
            }
            else if (state == State.LINE_COMMENT)
            {
                if (c == '\n')
                    state = State.NORMAL;
                i++;
            }
            else
            {
                if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '/')
                {
                    state = State.NORMAL;
                    i += 2;
                }
                else
                    i++;
            }
        }

        return out;
    }

    private static String extractBalanced(String text, int openIndex, char open, char close)
    {
        if (openIndex < 0 || openIndex >= text.length() || text.charAt(openIndex) != open)
            return null;

        int depth = 1;
        int i = openIndex + 1;
        while (i < text.length())
        {
            char c = text.charAt(i);
            if (c == '"')
            {
                i = skipQuoted(text, i, '"');
                continue;
            }
            if (c == '\'')
            {
                i = skipQuoted(text, i, '\'');
                continue;
            }
            if (c == '/' && i + 1 < text.length())
            {
                char n = text.charAt(i + 1);
                if (n == '/')
                {
                    i = skipLineComment(text, i + 2);
                    continue;
                }
                if (n == '*')
                {
                    i = skipBlockComment(text, i + 2);
                    continue;
                }
            }

            if (c == open)
                depth++;
            else if (c == close)
            {
                depth--;
                if (depth == 0)
                    return text.substring(openIndex + 1, i);
            }

            i++;
        }

        return null;
    }

    private static List<String> splitTopLevel(String text)
    {
        ArrayList<String> parts = new ArrayList<>();
        int start = 0;
        int paren = 0;
        int bracket = 0;
        int brace = 0;
        int angle = 0;
        int i = 0;

        while (i < text.length())
        {
            char c = text.charAt(i);
            if (c == '"')
            {
                i = skipQuoted(text, i, '"');
                continue;
            }
            if (c == '\'')
            {
                i = skipQuoted(text, i, '\'');
                continue;
            }
            if (c == '/' && i + 1 < text.length())
            {
                char n = text.charAt(i + 1);
                if (n == '/')
                {
                    i = skipLineComment(text, i + 2);
                    continue;
                }
                if (n == '*')
                {
                    i = skipBlockComment(text, i + 2);
                    continue;
                }
            }

            if (c == '(')
                paren++;
            else if (c == ')')
                paren--;
            else if (c == '[')
                bracket++;
            else if (c == ']')
                bracket--;
            else if (c == '{')
                brace++;
            else if (c == '}')
                brace--;
            else if (c == '<')
                angle++;
            else if (c == '>' && angle > 0)
                angle--;
            else if (c == ',' && paren == 0 && bracket == 0 && brace == 0 && angle == 0)
            {
                parts.add(text.substring(start, i).trim());
                start = i + 1;
            }

            i++;
        }

        parts.add(text.substring(start).trim());
        return parts;
    }

    private static List<String> splitTopLevelByChar(String text, char delimiter)
    {
        ArrayList<String> parts = new ArrayList<>();
        int start = 0;
        int paren = 0;
        int bracket = 0;
        int brace = 0;
        int i = 0;

        while (i < text.length())
        {
            char c = text.charAt(i);
            if (c == '"')
            {
                i = skipQuoted(text, i, '"');
                continue;
            }
            if (c == '\'')
            {
                i = skipQuoted(text, i, '\'');
                continue;
            }

            if (c == '(')
                paren++;
            else if (c == ')')
                paren--;
            else if (c == '[')
                bracket++;
            else if (c == ']')
                bracket--;
            else if (c == '{')
                brace++;
            else if (c == '}')
                brace--;
            else if (c == delimiter && paren == 0 && bracket == 0 && brace == 0)
            {
                parts.add(text.substring(start, i).trim());
                start = i + 1;
            }

            i++;
        }

        parts.add(text.substring(start).trim());
        return parts;
    }

    private static TernarySplit splitTopLevelTernary(String expr)
    {
        int paren = 0;
        int bracket = 0;
        int brace = 0;
        int depth = 0;
        int question = -1;

        for (int i = 0; i < expr.length(); i++)
        {
            char c = expr.charAt(i);
            if (c == '"')
            {
                i = skipQuoted(expr, i, '"') - 1;
                continue;
            }
            if (c == '\'')
            {
                i = skipQuoted(expr, i, '\'') - 1;
                continue;
            }

            if (c == '(')
                paren++;
            else if (c == ')')
                paren--;
            else if (c == '[')
                bracket++;
            else if (c == ']')
                bracket--;
            else if (c == '{')
                brace++;
            else if (c == '}')
                brace--;
            else if (paren == 0 && bracket == 0 && brace == 0)
            {
                if (c == '?')
                {
                    if (depth == 0 && question < 0)
                        question = i;
                    depth++;
                }
                else if (c == ':' && depth > 0)
                {
                    depth--;
                    if (depth == 0 && question >= 0)
                    {
                        String left = expr.substring(question + 1, i).trim();
                        String right = expr.substring(i + 1).trim();
                        return new TernarySplit(left, right);
                    }
                }
            }
        }

        return null;
    }

    private static int findTopLevelComma(String text, int start)
    {
        int paren = 0;
        int bracket = 0;
        int brace = 0;

        for (int i = start; i < text.length(); i++)
        {
            char c = text.charAt(i);
            if (c == '"')
            {
                i = skipQuoted(text, i, '"') - 1;
                continue;
            }
            if (c == '\'')
            {
                i = skipQuoted(text, i, '\'') - 1;
                continue;
            }

            if (c == '(')
                paren++;
            else if (c == ')')
                paren--;
            else if (c == '[')
                bracket++;
            else if (c == ']')
                bracket--;
            else if (c == '{')
                brace++;
            else if (c == '}')
                brace--;
            else if (c == ',' && paren == 0 && bracket == 0 && brace == 0)
                return i;
        }

        return -1;
    }

    private static String stripOuterParens(String expr)
    {
        while (expr.startsWith("(") && expr.endsWith(")") && matchingParens(expr))
            expr = expr.substring(1, expr.length() - 1).trim();
        return expr;
    }

    private static boolean matchingParens(String expr)
    {
        int depth = 0;
        for (int i = 0; i < expr.length(); i++)
        {
            char c = expr.charAt(i);
            if (c == '"')
            {
                i = skipQuoted(expr, i, '"') - 1;
                continue;
            }
            if (c == '\'')
            {
                i = skipQuoted(expr, i, '\'') - 1;
                continue;
            }

            if (c == '(')
                depth++;
            else if (c == ')')
            {
                depth--;
                if (depth == 0 && i != expr.length() - 1)
                    return false;
            }
        }
        return depth == 0;
    }

    private static boolean isStringLiteral(String expr)
    {
        return expr.length() >= 2 && expr.charAt(0) == '"' && expr.charAt(expr.length() - 1) == '"';
    }

    private static String unescapeJavaString(String raw)
    {
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++)
        {
            char c = raw.charAt(i);
            if (c != '\\' || i + 1 >= raw.length())
            {
                out.append(c);
                continue;
            }

            char n = raw.charAt(++i);
            switch (n)
            {
                case 'n':
                    out.append('\n');
                    break;
                case 'r':
                    out.append('\r');
                    break;
                case 't':
                    out.append('\t');
                    break;
                case 'b':
                    out.append('\b');
                    break;
                case 'f':
                    out.append('\f');
                    break;
                case '\'':
                    out.append('\'');
                    break;
                case '"':
                    out.append('"');
                    break;
                case '\\':
                    out.append('\\');
                    break;
                case 'u':
                    if (i + 4 < raw.length())
                    {
                        String hex = raw.substring(i + 1, i + 5);
                        out.append((char) Integer.parseInt(hex, 16));
                        i += 4;
                    }
                    else
                        out.append("\\u");
                    break;
                default:
                    if (n >= '0' && n <= '7')
                    {
                        int start = i;
                        while (i + 1 < raw.length() && i - start < 2 && raw.charAt(i + 1) >= '0' && raw.charAt(i + 1) <= '7')
                            i++;
                        out.append((char) Integer.parseInt(raw.substring(start, i + 1), 8));
                    }
                    else
                        out.append(n);
                    break;
            }
        }
        return out.toString();
    }

    private static String escapeLangValue(String value)
    {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            switch (c)
            {
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
        return out.toString();
    }

    private static int skipWhitespace(String text, int index)
    {
        while (index < text.length() && Character.isWhitespace(text.charAt(index)))
            index++;
        return index;
    }

    private static boolean startsWithIdentifier(String text, int index, String identifier)
    {
        if (index < 0 || index + identifier.length() > text.length())
            return false;
        if (!text.regionMatches(index, identifier, 0, identifier.length()))
            return false;

        boolean beforeOk = index == 0 || !Character.isJavaIdentifierPart(text.charAt(index - 1));
        boolean afterOk = index + identifier.length() >= text.length() ||
                !Character.isJavaIdentifierPart(text.charAt(index + identifier.length()));
        return beforeOk && afterOk;
    }

    private static int skipQuoted(String text, int start, char quote)
    {
        int i = start + 1;
        while (i < text.length())
        {
            char c = text.charAt(i);
            if (c == '\\')
                i += 2;
            else if (c == quote)
                return i + 1;
            else
                i++;
        }
        return text.length();
    }

    private static int skipLineComment(String text, int start)
    {
        int i = start;
        while (i < text.length() && text.charAt(i) != '\n')
            i++;
        return i;
    }

    private static int skipBlockComment(String text, int start)
    {
        int i = start;
        while (i + 1 < text.length())
        {
            if (text.charAt(i) == '*' && text.charAt(i + 1) == '/')
                return i + 2;
            i++;
        }
        return text.length();
    }

    private enum State
    {
        NORMAL,
        STRING,
        CHAR,
        LINE_COMMENT,
        BLOCK_COMMENT
    }

    private static class TernarySplit
    {
        public final String trueExpr;
        public final String falseExpr;

        public TernarySplit(String trueExpr, String falseExpr)
        {
            this.trueExpr = trueExpr;
            this.falseExpr = falseExpr;
        }
    }

    private static class LineMap
    {
        private final int[] starts;

        public LineMap(String text)
        {
            ArrayList<Integer> lineStarts = new ArrayList<>();
            lineStarts.add(0);
            for (int i = 0; i < text.length(); i++)
            {
                if (text.charAt(i) == '\n')
                    lineStarts.add(i + 1);
            }

            this.starts = new int[lineStarts.size()];
            for (int i = 0; i < lineStarts.size(); i++)
                this.starts[i] = lineStarts.get(i);
        }

        public int lineNumber(int index)
        {
            int low = 0;
            int high = starts.length - 1;

            while (low <= high)
            {
                int mid = (low + high) >>> 1;
                if (starts[mid] <= index)
                    low = mid + 1;
                else
                    high = mid - 1;
            }

            return Math.max(1, high + 1);
        }
    }

    private static class Config
    {
        public Path root;
        public String outputMode = OUTPUT_LOCATIONS;
        public String languageName = "New language";
    }
}
