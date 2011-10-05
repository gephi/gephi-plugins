/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.streaming.impl.dgs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;

/**
 * Ths parser is based on the DGS parser implementation
 * available in the GraphStream project:
 * http://graphstream.sourceforge.net/
 * <br>
 * <br>
 * Copyright 2006 - 2009<br>
 *  Julien Baudry<br>
 *  Antoine Dutot<br>
 *  Yoann Pigné<br>
 *  Guilhelm Savin<br>
 *
 */
public abstract class BaseParser {
    
    protected final StreamTokenizer st;
    /**
     * The quote character. Can be changed in descendants.
     */
    protected int QUOTE_CHAR = '"';

    /**
     * The comment character. Can be changed in descendants.
     */
    protected int COMMENT_CHAR = '#';

    /**
     * Is EOL significant?.
     */
    protected boolean eol_is_significant = true;
    
    @SuppressWarnings("deprecation")
    public BaseParser(InputStream inputStream) {
        // the InputStream constructor is better, as it does not block until the end of stream
//        StreamTokenizer st = new StreamTokenizer( new InputStreamReader(inputStream) );
        StreamTokenizer st = new StreamTokenizer( inputStream );
        
        st.commentChar(COMMENT_CHAR);
        st.eolIsSignificant(eol_is_significant);
        st.parseNumbers();
        st.wordChars( '_', '_' );
        
        this.st = st;
    }

    /**
     * Push back the last read thing, so that it can be read anew. This allows
     * to explore one token ahead, and if not corresponding to what is expected,
     * go back.
     */
    protected void pushBack()
    {
        st.pushBack();
    }

    /**
     * Read EOF or report garbage at end of file.
     */
    protected void eatEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_EOF )
            parseError( "garbage at end of file, expecting EOF, "
                    + gotWhat( tok ) );
    }

    /**
     * Read EOL.
     */
    protected void eatEol() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_EOL )
            parseError( "expecting EOL, " + gotWhat( tok ) );
    }

    /**
     * Read EOL or EOF.
     * @return The token read StreamTokenizer.TT_EOL or StreamTokenizer.TT_EOF.
     */
    protected int eatEolOrEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_EOL && tok != StreamTokenizer.TT_EOF )
            parseError( "expecting EOL or EOF, " + gotWhat( tok ) );

        return tok;
    }

    /**
     * Read an expected <code>word</code> token or generate a parse error.
     */
    protected void eatWord( String word ) throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_WORD )
            parseError( "expecting `" + word + "', " + gotWhat( tok ) );

        if( !st.sval.equals( word ) )
            parseError( "expecting `" + word + "' got `" + st.sval + "'" );
    }
    
    /**
     * Read an expected word among the given word list or generate a parse error.
     * @param words The expected words.
     */
    protected void eatWords( String...words )
        throws IOException
    {
        int tok = st.nextToken();
        
        if( tok != StreamTokenizer.TT_WORD )
            parseError( "expecting one of `" + words + "', " + gotWhat( tok ) );
        
        boolean found = false;
        
        for( String word: words )
        {
            if( st.sval.equals( word ) )
            {
                found = true;
                break;
            }
        }
        
        if( ! found )
            parseError( "expecting one of `" + words + ", got `" + st.sval + "'" );
    }

    /**
     * Eat either a word or another, and return the eated one.
     * 
     * @param word1 The first word to eat.
     * @param word2 The alternative word to eat.
     * @return The word eaten.
     */
    protected String eatOneOfTwoWords( String word1, String word2 )
            throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_WORD )
            parseError( "expecting `" + word1 + "' or  `" + word2 + "', "
                    + gotWhat( tok ) );

        if( st.sval.equals( word1 ) )
            return word1;

        if( st.sval.equals( word2 ) )
            return word2;

        parseError( "expecting `" + word1 + "' or `" + word2 + "' got `"
                + st.sval + "'" );
        return null;
    }

    /**
     * Eat the expected symbol or generate a parse error.
     */
    protected void eatSymbol( char symbol ) throws IOException
    {
        int tok = st.nextToken();

        if( tok != symbol )
            parseError( "expecting symbol `" + symbol + "', " + gotWhat( tok ) );
    }

    /**
     * Eat one of the list of expected <code>symbols</code> or generate a
     * parse error none of <code>symbols</code> can be found.
     */
    protected void eatSymbols( String symbols ) throws IOException
    {
        int tok = st.nextToken();
        int n = symbols.length();
        boolean f = false;

        for( int i = 0; i < n; ++i )
        {
            if( tok == symbols.charAt( i ) )
            {
                f = true;
                i = n;
            }
        }

        if( !f )
            parseError( "expecting one of symbols `" + symbols + "', "
                    + gotWhat( tok ) );
    }

    /**
     * Eat the expected <code>word</code> or push back what was read so that
     * it can be read anew.
     */
    protected void eatWordOrPushbak( String word ) throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_WORD )
            pushBack();

        if( !st.sval.equals( word ) )
            pushBack();
    }

    /**
     * Eat the expected <code>symbol</code> or push back what was read so that
     * it can be read anew.
     */
    protected void eatSymbolOrPushback( char symbol ) throws IOException
    {
        int tok = st.nextToken();

        if( tok != symbol )
            pushBack();
    }

    /**
     * Eat all until an EOL is found. The EOL is also eaten. This works only if
     * EOL is significant (else it does nothing).
     */
    protected void eatAllUntilEol() throws IOException
    {
        if( !eol_is_significant )
            return;

        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_EOF )
            return;

        while( ( tok != StreamTokenizer.TT_EOL )
                && ( tok != StreamTokenizer.TT_EOF ) )
        {
            tok = st.nextToken();
        }
    }

    /**
     * Eat all availables EOLs.
     */
    protected void eatAllEols() throws IOException
    {
        if( !eol_is_significant )
            return;

        int tok = st.nextToken();

        while( tok == StreamTokenizer.TT_EOL )
            tok = st.nextToken();

        pushBack();
    }

    /**
     * Read a word or generate a parse error.
     */
    protected String getWord() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_WORD )
            parseError( "expecting a word, " + gotWhat( tok ) );

        return st.sval;
    }

    /**
     * Get a symbol.
     */
    protected char getSymbol() throws IOException
    {
        int tok = st.nextToken();

        if( tok > 0 && tok != StreamTokenizer.TT_WORD
                && tok != StreamTokenizer.TT_NUMBER
                && tok != StreamTokenizer.TT_EOL
                && tok != StreamTokenizer.TT_EOF && tok != QUOTE_CHAR
                && tok != COMMENT_CHAR )
        {
            return (char) tok;
        }

        parseError( "expecting a symbol, " + gotWhat( tok ) );
        return (char) 0;    // Never reached.
    }
    
    /**
     * Get a symbol or push back what was read so that it can be read anew. If
     * no symbol is found, 0 is returned.
     */
    protected char getSymbolOrPushback() throws IOException
    {
        int tok = st.nextToken();

        if( tok > 0 && tok != StreamTokenizer.TT_WORD
                && tok != StreamTokenizer.TT_NUMBER
                && tok != StreamTokenizer.TT_EOL
                && tok != StreamTokenizer.TT_EOF && tok != QUOTE_CHAR
                && tok != COMMENT_CHAR )
        {
            return (char) tok;
        }

        pushBack();

        return (char) 0;
    }

    /**
     * Read a string constant (between quotes) or generate a parse error. Return
     * the content of the string without the quotes.
     */
    protected String getString() throws IOException
    {
        int tok = st.nextToken();

        if( tok != QUOTE_CHAR )
            parseError( "expecting a string constant, " + gotWhat( tok ) );

        return st.sval;
    }

    /**
     * Read a word or number or generate a parse error. If it is a number it is
     * converted to a string before being returned.
     */
    protected String getWordOrNumber() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_WORD && tok != StreamTokenizer.TT_NUMBER )
            parseError( "expecting a word or number, " + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_NUMBER )
        {
            // If st.nval is an integer, as it is stored into a double,
            // toString() will transform it by automatically adding ".0", we
            // prevent this. The tokenizer does not allow to read integers.

            if( ( st.nval - ( (int) st.nval ) ) == 0 )
                return Integer.toString( (int) st.nval );
            else
                return Double.toString( st.nval );
        }
        else
        {
            return st.sval;
        }
    }

    /**
     * Read a string or number or generate a parse error. If it is a number it
     * is converted to a string before being returned.
     */
    protected String getStringOrNumber() throws IOException
    {
        int tok = st.nextToken();

        if( tok != QUOTE_CHAR && tok != StreamTokenizer.TT_NUMBER )
            parseError( "expecting a string constant or a number, "
                    + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_NUMBER )
        {
            if( ( st.nval - ( (int) st.nval ) ) == 0 )
                return Integer.toString( (int) st.nval );
            else
                return Double.toString( st.nval );
        }
        else
        {
            return st.sval;
        }
    }

    /**
     * Read a string or number or generate a parse error. If it is a number it
     * is converted to a string before being returned.
     */
    protected String getStringOrWordOrNumber() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_EOL || tok == StreamTokenizer.TT_EOF )
            parseError( "expecting word, string or number, " + gotWhat( tok ) );
        
        if( tok == StreamTokenizer.TT_NUMBER )
        {
            if( ( st.nval - ( (int) st.nval ) ) == 0 )
                return Integer.toString( (int) st.nval );
            else
                return Double.toString( st.nval );
        }
        else
        {
            return st.sval;
        }
    }
    
    /**
     * Read a string or number or generate a parse error. The returned value
     * is converted to a Number of a String depending on its type.
     */
    protected Object getStringOrWordOrNumberO() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_EOL || tok == StreamTokenizer.TT_EOF )
            parseError( "expecting word, string or number, " + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_NUMBER )
        {
            return st.nval;
        }
        else
        {
            return st.sval;
        }       
    }
    
    /**
     * Read a string or number or generate a parse error. The returned value
     * is converted to a Number of a String depending on its type.
     */
    protected Object getStringOrWordOrSymbolOrNumberO() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_EOL || tok == StreamTokenizer.TT_EOF )
            parseError( "expecting word, string or number, " + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_NUMBER )
        {
            return st.nval;
        }
        else if( tok == StreamTokenizer.TT_WORD )
        {
            return st.sval;
        }       
        else return Character.toString( (char) tok );
    }

    /**
     * Read a word or string or generate a parse error.
     */
    protected String getWordOrString() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_WORD || tok == QUOTE_CHAR )
            return st.sval;

        parseError( "expecting a word or string, " + gotWhat( tok ) );
        return null;
    }

    /**
     * Read a word or symbol or generate a parse error.
     */
    protected String getWordOrSymbol() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER || tok == QUOTE_CHAR
                || tok == StreamTokenizer.TT_EOF )
            parseError( "expecting a word or symbol, " + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;
        else
            return Character.toString( (char) tok );
    }

    /**
     * Read a word or symbol or push back the read thing so that it is readable
     * anew. In the second case, null is returned.
     */
    protected String getWordOrSymbolOrPushback() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER || tok == QUOTE_CHAR
                || tok == StreamTokenizer.TT_EOF )
        {
            pushBack();
            return null;
        }

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;
        else
            return Character.toString( (char) tok );
    }

    /**
     * Read a word or symbol or string or generate a parse error.
     */
    protected String getWordOrSymbolOrString() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER || tok == StreamTokenizer.TT_EOF )
            parseError( "expecting a word, symbol or string, " + gotWhat( tok ) );

        if( tok == QUOTE_CHAR )
            return st.sval;

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;
        else
            return Character.toString( (char) tok );
    }

    /**
     * Read a word or symbol or string or number or generate a parse error.
     */
    protected String getAllExceptedEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_EOF )
            parseError( "expecting all excepted EOF, " + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_NUMBER || tok == StreamTokenizer.TT_EOF )
        {
            if( ( st.nval - ( (int) st.nval ) ) == 0 )
                return Integer.toString( (int) st.nval );
            else
                return Double.toString( st.nval );
        }

        if( tok == QUOTE_CHAR )
            return st.sval;

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;
        else
            return Character.toString( (char) tok );
    }

    /**
     * Read a word, a symbol or EOF, or generate a parse error. If this is EOF,
     * the string "EOF" is returned.
     */
    protected String getWordOrSymbolOrEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER || tok == QUOTE_CHAR )
            parseError( "expecting a word or symbol, " + gotWhat( tok ) );

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;
        else if( tok == StreamTokenizer.TT_EOF )
            return "EOF";
        else
            return Character.toString( (char) tok );
    }

    /**
     * Read a word or symbol or string or EOL/EOF or generate a parse error.
     * If EOL is read the "EOL" string is returned. If EOF is read the "EOF"
     * string is returned.
     * @return A string. 
     */
    protected String getWordOrSymbolOrStringOrEolOrEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER )
            parseError( "expecting a word, symbol or string, " + gotWhat( tok ) );

        if( tok == QUOTE_CHAR )
            return st.sval;

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;

        if( tok == StreamTokenizer.TT_EOF )
            return "EOF";

        if( tok == StreamTokenizer.TT_EOL )
            return "EOL";

        return Character.toString( (char) tok );
    }

    /**
     * Read a word or number or string or EOL/EOF or generate a parse error.
     * If EOL is read the "EOL" string is returned. If EOF is read the "EOF"
     * string is returned. If a number is returned, it is converted to a string
     * as follows: if it is an integer, only the integer part is converted to a
     * string without dot or comma and no leading zeros. If it is a float the
     * fractional part is also converted and the dot is used as separator.
     * @return A string. 
     */
    protected String getWordOrNumberOrStringOrEolOrEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER )
        {
            if( st.nval - ((int)st.nval) != 0 )
                return Double.toString( st.nval );
            
            return Integer.toString( (int) st.nval );
        }

        if( tok == QUOTE_CHAR )
            return st.sval;

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;

        if( tok == StreamTokenizer.TT_EOF )
            return "EOF";

        if( tok == StreamTokenizer.TT_EOL )
            return "EOL";

        parseError( "expecting a word, a number, a string, EOL or EOF, " + gotWhat( tok ) );
        return null; // Never happen, parseError throws unconditionally an exception.
    }
    
    /**
     * Read a word or string or EOL/EOF or generate a parse error.
     * If EOL is read the "EOL" string is returned. If EOF is read the "EOF"
     * string is returned. 
     * @return A string. 
     */
    protected String getWordOrStringOrEolOrEof() throws IOException
    {
        int tok = st.nextToken();
        
        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;
        
        if( tok == QUOTE_CHAR )
            return st.sval;
        
        if( tok == StreamTokenizer.TT_EOL )
            return "EOL";
        
        if( tok == StreamTokenizer.TT_EOF )
            return "EOF";
        
        parseError( "expecting a word, a string, EOL or EOF, " + gotWhat( tok ) );
        return null; // Never happen, parseError throws unconditionally an exception.       
    }

    // Ordre: Word | String | Symbol | Number | Eol | Eof
    
    /**
     * Read a word or number or string or EOL/EOF or generate a parse error.
     * If EOL is read the "EOL" string is returned. If EOF is read the "EOF"
     * string is returned. If a number is returned, it is converted to a string
     * as follows: if it is an integer, only the integer part is converted to a
     * string without dot or comma and no leading zeros. If it is a float the
     * fractional part is also converted and the dot is used as separator.
     * @return A string. 
     */
    protected String getWordOrSymbolOrNumberOrStringOrEolOrEof() throws IOException
    {
        int tok = st.nextToken();

        if( tok == StreamTokenizer.TT_NUMBER )
        {
            if( st.nval - ((int)st.nval) != 0 )
                return Double.toString( st.nval );
            
            return Integer.toString( (int) st.nval );
        }

        if( tok == QUOTE_CHAR )
            return st.sval;

        if( tok == StreamTokenizer.TT_WORD )
            return st.sval;

        if( tok == StreamTokenizer.TT_EOF )
            return "EOF";

        if( tok == StreamTokenizer.TT_EOL )
            return "EOL";

        return Character.toString( (char) tok );
    }
    
    /**
     * Read a number or generate a parse error.
     */
    protected double getNumber() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_NUMBER )
            parseError( "expecting a number, " + gotWhat( tok ) );

        return st.nval;
    }

    /**
     * Read a number (possibly with an exponent) or generate a parse error.
     */
    protected double getNumberExp() throws IOException
    {
        int tok = st.nextToken();

        if( tok != StreamTokenizer.TT_NUMBER )
            parseError( "expecting a number, " + gotWhat( tok ) );

        double nb = st.nval;

        tok = st.nextToken();

        if( tok == StreamTokenizer.TT_WORD
                && ( st.sval.startsWith( "e-" ) || st.sval.startsWith( "e+" ) ) )
        {
            double exp = Double.parseDouble( st.sval.substring( 2 ) );
            return Math.pow( nb, exp );
        }
        else
        {
            st.pushBack();
        }

        return nb;
    }

    /**
     * Return a string containing "got " then the content of the current
     * <code>token</code>.
     */
    protected String gotWhat( int token )
    {
        switch( token )
        {
            case StreamTokenizer.TT_NUMBER :
                return "got number `" + st.nval + "'";
            case StreamTokenizer.TT_WORD :
                return "got word `" + st.sval + "'";
            case StreamTokenizer.TT_EOF :
                return "got EOF";
            default:
                if( token == QUOTE_CHAR )
                    return "got string constant `" + st.sval + "'";
                else
                    return "unknown symbol `" + token + "' (" + ( (char) token )
                            + ")";
        }
    }

    /**
     * Generate a parse error.
     */
    protected abstract void parseError( String message ) throws IOException;
    
}
