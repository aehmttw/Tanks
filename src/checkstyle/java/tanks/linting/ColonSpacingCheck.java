package tanks.linting;

import com.puppycrawl.tools.checkstyle.api.*;

/**
 * Custom Checkstyle check for colon spacing:
 *
 * <ul>
 *   <li>Ternary {@code :} (parent node is QUESTION) MUST have a space before it,
 *       i.e. "expr ? a : b" — the colon must be preceded by a non-whitespace char
 *       then a space.</li>
 *   <li>All other {@code :} tokens (for-each loops, switch cases, labels) must
 *       NOT have a space before them in that sense.</li>
 *   <li>A colon that is the first non-whitespace character on its line is skipped
 *       (handles multi-line ternary continuations).</li>
 * </ul>
 */
public class ColonSpacingCheck extends AbstractCheck
{
    /**
     * Message key emitted when a ternary ':' is missing its preceding space.
     */
    static final String MSG_TERNARY_MISSING_SPACE = "Missing space before ternary ':'";

    /**
     * Message key emitted when a non-ternary ':' has an unwanted preceding space.
     */
    static final String MSG_NON_TERNARY_SPACE = "Space before ':' in non-ternary context";

    @Override
    public int[] getDefaultTokens()
    {
        return new int[]{TokenTypes.COLON};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens()
    {
        return new int[]{};
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        final int lineNo = ast.getLineNo();
        final int colNo = ast.getColumnNo(); // 0-based
        final String[] lines = getLines();

        if (lineNo < 1 || lineNo > lines.length)
        {
            return;
        }

        final String line = lines[lineNo - 1];

        // Skip colons that are the first non-whitespace on their line.
        // This handles multi-line ternary continuations like:
        //     : alternativeValue
        if (line.substring(0, colNo).trim().isEmpty())
        {
            return;
        }

        // Determine whether there is a space immediately before ':' that is itself
        // preceded by non-whitespace content (i.e. "word :").
        final boolean spaceBefore = colNo > 0 && line.charAt(colNo - 1) == ' ';
        boolean hasContentBeforeSpace = false;
        if (spaceBefore)
        {
            for (int i = colNo - 2; i >= 0; i--)
            {
                final char c = line.charAt(i);
                if (c != ' ' && c != '\t')
                {
                    hasContentBeforeSpace = true;
                    break;
                }
            }
        }

        // A ternary ':' has QUESTION as its direct parent in the AST.
        final DetailAST parent = ast.getParent();
        final boolean isTernary = parent != null && parent.getType() == TokenTypes.QUESTION;

        if (isTernary)
        {
            if (!spaceBefore || !hasContentBeforeSpace)
            {
                log(ast, MSG_TERNARY_MISSING_SPACE);
            }
        }
        else
        {
            if (spaceBefore && hasContentBeforeSpace)
            {
                log(ast, MSG_NON_TERNARY_SPACE);
            }
        }
    }
}
