/**
 * Copyright (C) 2015 Michael Schnell. All rights reserved.
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.srcgen4j.commons;

import static java.util.Objects.requireNonNull;
import static org.fuin.utils4j.Utils4J.replaceVars;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.jspecify.annotations.Nullable;
import org.fuin.objects4j.common.Contract;

/**
 * Replaces text found by a regular expression with a replacement pattern.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "replacer")
public final class Replacer {

    /** Name of the replacer. */
    @NotEmpty
    @XmlAttribute
    private String name;

    /** Optional extension to allow further customization. */
    @Nullable
    @XmlAttribute
    private String extension;

    /** Pattern used to find the text to replace. */
    @NotEmpty
    @XmlAttribute
    private String expression;

    /** Replacement pattern. */
    @NotEmpty
    @XmlAttribute
    private String replacement;

    @Nullable
    private transient Pattern expressionPattern;

    /**
     * Package visible default constructor for deserialization.
     */
    @SuppressWarnings("NullAway.Init") // Fields are populated by JAXB after construction
    Replacer() {
        super();
    }

    /**
     * Constructor with name, expression and replacement.
     *
     * @param name
     *            Name of the replacer.
     * @param expression
     *            Pattern used to find the text to replace. Compiled and validated as a regular expression when
     *            {@link #init(Map)} is called (or lazily on the first {@link #replace(String)}).
     * @param replacement
     *            Replacement pattern. Validated as a valid replacement string for the expression when {@link #init(Map)} is called.
     */
    public Replacer(@NotEmpty final String name, @NotEmpty final String expression, @NotEmpty final String replacement) {
        this(name, null, expression, replacement);
    }

    /**
     * Constructor with all data.
     *
     * @param name
     *            Name of the replacer.
     * @param extension
     *            Optional extension to allow further customization.
     * @param expression
     *            Pattern used to find the text to replace. Compiled and validated as a regular expression when
     *            {@link #init(Map)} is called (or lazily on the first {@link #replace(String)}).
     * @param replacement
     *            Replacement pattern. Validated as a valid replacement string for the expression when {@link #init(Map)} is called.
     */
    public Replacer(@NotEmpty final String name, @Nullable final String extension, @NotEmpty final String expression,
            @NotEmpty final String replacement) {
        super();
        Contract.requireArgNotEmpty("name", name);
        Contract.requireArgNotEmpty("expression", expression);
        Contract.requireArgNotEmpty("replacement", replacement);
        this.name = name;
        this.extension = extension;
        this.expression = expression;
        this.replacement = replacement;
    }

    /**
     * Returns the name of the replacer.
     *
     * @return Current name.
     */
    @NotEmpty
    public final String getName() {
        return name;
    }

    /**
     * Returns the optional extension that allows further customization.
     *
     * @return Extension or <code>null</code>.
     */
    @Nullable
    public final String getExtension() {
        return extension;
    }

    /**
     * Returns the pattern used to find the text to replace.
     *
     * @return Regular expression to search for.
     */
    @NotEmpty
    public final String getExpression() {
        return expression;
    }

    /**
     * Returns the replacement pattern.
     *
     * @return Replacement string applied to every match.
     */
    @NotEmpty
    public final String getReplacement() {
        return replacement;
    }

    /**
     * Replaces every subsequence of the input that matches the {@link #getExpression() expression} with the
     * {@link #getReplacement() replacement} pattern.
     *
     * @param input
     *            Text to apply the replacement on.
     *
     * @return Result after applying <code>replaceAll(expression, replacement)</code> on the input.
     */
    public final String replace(@NotNull final String input) {
        Contract.requireArgNotNull("input", input);
        return compiledExpression().matcher(input).replaceAll(replacement);
    }

    /**
     * Replaces variables (if defined) in the extension, expression and replacement, then compiles the expression and validates the
     * replacement against it. The name is left unchanged as it identifies the replacer.
     *
     * @param vars
     *            Variables to use.
     *
     * @return This instance.
     */
    public final Replacer init(@Nullable final Map<String, String> vars) {
        extension = replaceVars(extension, vars);
        expression = requireNonNull(replaceVars(expression, vars));
        replacement = requireNonNull(replaceVars(replacement, vars));
        expressionPattern = compile("expression", expression);
        requireValidReplacement("replacement", replacement, expressionPattern);
        return this;
    }

    private Pattern compiledExpression() {
        if (expressionPattern == null) {
            expressionPattern = compile("expression", expression);
        }
        return expressionPattern;
    }

    private static Pattern compile(final String argName, final String regEx) {
        try {
            return Pattern.compile(regEx);
        } catch (final PatternSyntaxException ex) {
            throw new IllegalArgumentException(
                    "The argument '" + argName + "' is not a valid regular expression: '" + regEx + "'", ex);
        }
    }

    /**
     * Verifies that the given string is a valid replacement for the given expression. This applies the same rules as
     * {@link java.util.regex.Matcher#replaceAll(String)}: a backslash escapes the following character (which must exist) and a dollar sign
     * introduces a numbered (<code>$1</code>) or named (<code>${name}</code>) group reference that must exist in the expression.
     *
     * @param argName
     *            Name of the argument used in the error message.
     * @param replacement
     *            Replacement string to check.
     * @param pattern
     *            Compiled expression the replacement refers to.
     */
    private static void requireValidReplacement(final String argName, final String replacement, final Pattern pattern) {
        final int groupCount = pattern.matcher("").groupCount();
        final Map<String, Integer> namedGroups = pattern.namedGroups();
        int cursor = 0;
        while (cursor < replacement.length()) {
            final char ch = replacement.charAt(cursor);
            if (ch == '\\') {
                cursor++;
                if (cursor == replacement.length()) {
                    throw invalidReplacement(argName, replacement, "character to be escaped is missing");
                }
                cursor++;
            } else if (ch == '$') {
                cursor++;
                if (cursor == replacement.length()) {
                    throw invalidReplacement(argName, replacement, "group reference is missing after '$'");
                }
                if (replacement.charAt(cursor) == '{') {
                    cursor = checkNamedGroup(argName, replacement, cursor + 1, namedGroups);
                } else {
                    cursor = checkNumberedGroup(argName, replacement, cursor, groupCount);
                }
            } else {
                cursor++;
            }
        }
    }

    private static int checkNamedGroup(final String argName, final String replacement, final int start,
            final Map<String, Integer> namedGroups) {
        int cursor = start;
        while (cursor < replacement.length() && isLatinLetterOrDigit(replacement.charAt(cursor))) {
            cursor++;
        }
        final String groupName = replacement.substring(start, cursor);
        if (groupName.isEmpty()) {
            throw invalidReplacement(argName, replacement, "named group reference has a zero length name");
        }
        if (cursor == replacement.length() || replacement.charAt(cursor) != '}') {
            throw invalidReplacement(argName, replacement, "named group reference is missing a trailing '}'");
        }
        if (Character.isDigit(groupName.charAt(0))) {
            throw invalidReplacement(argName, replacement, "named group reference '" + groupName + "' starts with a digit");
        }
        if (!namedGroups.containsKey(groupName)) {
            throw invalidReplacement(argName, replacement, "there is no group with name '" + groupName + "' in the expression");
        }
        return cursor + 1; // Consume the trailing '}'
    }

    private static int checkNumberedGroup(final String argName, final String replacement, final int start, final int groupCount) {
        int refNum = replacement.charAt(start) - '0';
        if (refNum < 0 || refNum > 9) {
            throw invalidReplacement(argName, replacement, "illegal group reference");
        }
        int cursor = start + 1;
        // Capture the largest legal group number (same greedy logic as Matcher)
        while (cursor < replacement.length()) {
            final int nextDigit = replacement.charAt(cursor) - '0';
            if (nextDigit < 0 || nextDigit > 9) {
                break;
            }
            final int newRefNum = refNum * 10 + nextDigit;
            if (newRefNum > groupCount) {
                break;
            }
            refNum = newRefNum;
            cursor++;
        }
        if (refNum > groupCount) {
            throw invalidReplacement(argName, replacement, "there is no group " + refNum + " in the expression");
        }
        return cursor;
    }

    private static boolean isLatinLetterOrDigit(final char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9');
    }

    private static IllegalArgumentException invalidReplacement(final String argName, final String replacement, final String detail) {
        return new IllegalArgumentException(
                "The argument '" + argName + "' is not a valid replacement string (" + detail + "): '" + replacement + "'");
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Replacer other = (Replacer) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public final String toString() {
        return "Replacer [name=" + name + ", extension=" + extension + ", expression=" + expression + ", replacement=" + replacement + "]";
    }

}
