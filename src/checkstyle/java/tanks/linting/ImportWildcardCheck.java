package tanks.linting;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom Checkstyle check for import wildcard rules:
 *
 * <ul>
 *   <li>If a package already has a wildcard import (e.g. {@code java.util.*}), any
 *       specific import from that same package (e.g. {@code java.util.ArrayList}) is
 *       flagged as redundant.</li>
 *   <li>If a package accumulates more than {@value #MAX_BEFORE_WILDCARD} specific
 *       imports and no wildcard import exists, the first such import is flagged and
 *       a wildcard is suggested.</li>
 * </ul>
 *
 * <p>The threshold (default 3) can be configured via the {@code maxImportsBeforeWildcard}
 * property in the Checkstyle XML.</p>
 */
public class ImportWildcardCheck extends AbstractCheck
{
    /** Maximum specific imports allowed per package before a wildcard is required. */
    private int maxImportsBeforeWildcard = 3;

    // --- per-file state ---
    private final Map<String, List<DetailAST>> specificImports = new LinkedHashMap<>();
    private final Set<String>                  wildcardPackages = new HashSet<>();

    // -----------------------------------------------------------------
    // Configuration
    // -----------------------------------------------------------------

    /**
     * Sets the maximum number of specific imports from a single package before a
     * wildcard import is required.
     *
     * @param max the threshold (default 3)
     */
    public void setMaxImportsBeforeWildcard(int max)
    {
        this.maxImportsBeforeWildcard = max;
    }

    // -----------------------------------------------------------------
    // AbstractCheck overrides
    // -----------------------------------------------------------------

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.IMPORT, TokenTypes.STATIC_IMPORT};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens()
    {
        return new int[] {};
    }

    @Override
    public void beginTree(DetailAST rootAST)
    {
        specificImports.clear();
        wildcardPackages.clear();
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        final boolean isStatic = ast.getType() == TokenTypes.STATIC_IMPORT;

        // For STATIC_IMPORT the AST is: STATIC_IMPORT -> LITERAL_STATIC -> DOT ...
        // For IMPORT          the AST is: IMPORT -> DOT ...
        // createFullIdentBelow walks all children; for static imports that includes
        // the word "static", so we skip past LITERAL_STATIC to get only the path.
        final String importPath;
        if (isStatic)
        {
            final DetailAST afterStatic = ast.getFirstChild().getNextSibling();
            importPath = afterStatic == null ? "" : FullIdent.createFullIdent(afterStatic).getText();
        }
        else
        {
            importPath = FullIdent.createFullIdentBelow(ast).getText();
        }

        if (importPath.isEmpty())
        {
            return;
        }

        final String prefix = isStatic ? "static:" : "";

        if (importPath.endsWith(".*"))
        {
            // Wildcard import — record the package name (strip ".*")
            final String pkg = importPath.substring(0, importPath.length() - 2);
            wildcardPackages.add(prefix + pkg);
        }
        else
        {
            // Specific import — extract the package (everything before the last '.')
            final int lastDot = importPath.lastIndexOf('.');
            if (lastDot > 0)
            {
                final String pkg = prefix + importPath.substring(0, lastDot);
                specificImports.computeIfAbsent(pkg, k -> new ArrayList<>()).add(ast);
            }
        }
    }

    @Override
    public void finishTree(DetailAST rootAST)
    {
        for (Map.Entry<String, List<DetailAST>> entry : specificImports.entrySet())
        {
            final String pkgKey = entry.getKey();          // may start with "static:"
            final List<DetailAST> imports = entry.getValue();
            final String pkgDisplay = pkgKey.replace("static:", "");

            if (wildcardPackages.contains(pkgKey))
            {
                // Specific imports that duplicate a wildcard
                for (DetailAST imp : imports)
                {
                    log(imp, "Import is redundant; ''{0}.*'' is already imported.", pkgDisplay);
                }
            }
            else if (imports.size() > maxImportsBeforeWildcard)
            {
                log(imports.get(0),
                    "{0} specific import(s) from ''{1}''; use ''{1}.*'' (max before wildcard: {2}).",
                    imports.size(), pkgDisplay, maxImportsBeforeWildcard);
            }
        }
    }
}
