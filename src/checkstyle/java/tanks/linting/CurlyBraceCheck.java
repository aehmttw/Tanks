package tanks.linting;

import com.puppycrawl.tools.checkstyle.api.*;

/**
 * Custom Checkstyle check for curly brace placement:
 *
 * <ul>
 *   <li>If '{' and '}' are on the same line, any surrounding code is allowed (single-line block).</li>
 *   <li>If '{' and '}' are on different lines, each must be the only non-whitespace character
 *       on its respective line (Allman style).</li>
 * </ul>
 *
 * <p>Curly braces are optional for single-statement {@code if} and {@code for} bodies;
 * this check only applies when braces are present.</p>
 */
public class CurlyBraceCheck extends AbstractCheck
{
    /**
     * Message emitted when the left curly brace is not alone on its line in a multi-line block.
     */
    static final String MSG_LEFT_CURLY =
        "Left curly brace must be alone on its line when block spans multiple lines";

    /**
     * Message emitted when the right curly brace is not alone on its line in a multi-line block.
     */
    static final String MSG_RIGHT_CURLY =
        "Right curly brace must be alone on its line when block spans multiple lines";

    /**
     * Message emitted when 'catch' or 'finally' follows '}' on the same line.
     */
    static final String MSG_CATCH_SAME_LINE =
        "''catch''/''finally'' must be on a new line after the closing brace";

    @Override
    public int[] getDefaultTokens()
    {
        return new int[]
            {
                TokenTypes.SLIST,
                TokenTypes.OBJBLOCK,
                TokenTypes.ARRAY_INIT,
                TokenTypes.LITERAL_SWITCH,
            };
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
        final DetailAST lcurlyToken;
        final DetailAST rcurlyToken;

        final int type = ast.getType();
        if (type == TokenTypes.OBJBLOCK)
        {
            // OBJBLOCK: first child is LCURLY token, last child is RCURLY token
            lcurlyToken = ast.getFirstChild();
            if (lcurlyToken == null || lcurlyToken.getType() != TokenTypes.LCURLY)
            {
                return;
            }
            rcurlyToken = ast.getLastChild();
        }
        else if (type == TokenTypes.LITERAL_SWITCH)
        {
            // Switch block: LCURLY and RCURLY are direct children of LITERAL_SWITCH
            lcurlyToken = ast.findFirstToken(TokenTypes.LCURLY);
            if (lcurlyToken == null)
            {
                return;
            }
            rcurlyToken = ast.getLastChild();
        }
        else
        {
            // SLIST and ARRAY_INIT: the node's own position is the opening '{'
            lcurlyToken = ast;
            rcurlyToken = ast.getLastChild();
        }

        if (rcurlyToken == null || rcurlyToken.getType() != TokenTypes.RCURLY)
        {
            return;
        }

        final int lcurlyLine = lcurlyToken.getLineNo();
        final int rcurlyLine = rcurlyToken.getLineNo();

        // Same-line block (e.g. "{ stmt; }" or "{}") — always allowed
        if (lcurlyLine == rcurlyLine)
        {
            return;
        }

        // Multi-line block: each brace must be alone on its line
        final String[] lines = getLines();
        checkLeftCurly(lcurlyToken, lines);
        checkRightCurly(rcurlyToken, lines);
    }

    /**
     * Checks that the left curly brace is the only non-whitespace character on its line:
     * nothing before it and nothing after it.
     */
    private void checkLeftCurly(DetailAST lcurly, String[] lines)
    {
        final int lineNo = lcurly.getLineNo();
        final int colNo = lcurly.getColumnNo();
        if (lineNo < 1 || lineNo > lines.length)
        {
            return;
        }
        final String line = lines[lineNo - 1];
        final boolean beforeClean = line.substring(0, colNo).trim().isEmpty();
        final boolean afterClean = line.substring(colNo + 1).trim().isEmpty();
        if (!beforeClean || !afterClean)
        {
            log(lcurly, MSG_LEFT_CURLY);
        }
    }

    /**
     * Checks that the right curly brace has only whitespace before it on its line.
     * Trailing content (e.g. "while" in do-while, or a comment) is permitted,
     * but "catch" and "finally" are not — they must appear on the next line.
     */
    private void checkRightCurly(DetailAST rcurly, String[] lines)
    {
        final int lineNo = rcurly.getLineNo();
        final int colNo = rcurly.getColumnNo();
        if (lineNo < 1 || lineNo > lines.length)
        {
            return;
        }
        final String line = lines[lineNo - 1];
        final boolean beforeClean = line.substring(0, colNo).trim().isEmpty();
        if (!beforeClean)
        {
            log(rcurly, MSG_RIGHT_CURLY);
        }
        final String after = line.substring(colNo + 1).trim();
        if (after.matches("(catch|finally)\\b.*"))
        {
            log(rcurly, MSG_CATCH_SAME_LINE);
        }
    }
}