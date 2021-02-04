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
import java.util.ArrayList;
import java.util.HashMap;
import org.gephi.streaming.api.Issue;
import org.gephi.streaming.api.Report;
import org.gephi.streaming.api.StreamReader.StreamReaderStatusListener;


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
public class DGSParser extends BaseParser {
    
    /**
     * Format version.
     */
    protected int version;
    
    /**
     * Name of the graph.
     */
    protected String graphName;
    
    /**
     * Number of step given in the header.
     */
    protected int stepCountAnnounced;
    
    /**
     * Number of events given in the header.
     */
    protected int eventCountAnnounced;
    
    /**
     * An attribute set used everywhere.
     */
    protected HashMap<String,Object> attributes = new HashMap<String,Object>();
    
    /**
     * True as soon as the end of file is reached.
     */
    protected boolean finished;
    
    private DGSParserListener listener;
    private StreamReaderStatusListener statusListener;
    private Report report;
    
    public DGSParser(InputStream inputStream, DGSParserListener listener, Report report, StreamReaderStatusListener statusListener) {
        super(inputStream);
        this.listener = listener;
        this.statusListener = statusListener;
        this.report = report;
    }
    
    public void parse() throws IOException {
        this.begin();
        while(this.next(false, false)) {}
    }
    
    protected void begin() throws IOException
    {
        st.parseNumbers();
        eatWords( "DGS003", "DGS004" );
        
        version = 3;
        
        eatEol();
        graphName           = getWordOrString();
        stepCountAnnounced  = (int)getNumber();//Integer.parseInt( getWord() );
        eventCountAnnounced = (int)getNumber();//Integer.parseInt( getWord() );
        eatEol();
        
        if(  graphName != null ) {
            HashMap attributes = new HashMap<String, Object>();
            listener.onGraphChanged( attributes );
        }
             
        else graphName = "DGS_";
        
        graphName = String.format( "%s_%d", graphName, System.currentTimeMillis()+((long)Math.random()*10) );
    }

    /**
     * Read either one event or several.
     * @param readSteps If true, read several events (usually starting with a
     *        step event, but it may be preceded by other events), until
     *        another step is encountered.
     * @param stop If true stop at the next step encountered (and push it back
     *        so that is is readable at the next call to this method).
     * @return True if it remains things to read.
     */
    protected boolean next( boolean readSteps, boolean stop ) throws IOException
    {
        String  key  = null;
        boolean loop = readSteps;

        // Sorted in probability of appearance ...
        
        do
        {
            key = getWordOrSymbolOrStringOrEolOrEof();
            
            if( key.equals( "ce" ) )
            {
                readCE();
            }
            else if( key.equals( "cn" ) )
            {
                readCN();
            }
            else if( key.equals( "ae" ) )
            {
                readAE();
            }
            else if( key.equals( "an" ) )
            {
                readAN();
            }
            else if( key.equals( "de" ) )
            {
                readDE();
            }
            else if( key.equals( "dn" ) )
            {
                readDN();
            }
            else if( key.equals( "cg" ) )
            {
                readCG();
            }
            else if( key.equals( "st" ) )
            {
                if( readSteps )
                {
                    if( stop )
                    {
                        loop = false;
                        pushBack();
                    }
                    else
                    {
                        stop = true;
                        readST();
                    }
                }
                else
                {
                    readST();
                }
            }
            else if( key.equals( "#" ) )
            {
                eatAllUntilEol();
                return next( readSteps, stop );
            }
            else if( key.equals( "EOL" ) )
            {
                // Probably an empty line.
                // NOP
                return next( readSteps, stop );
            }
            else if( key.equals( "EOF" ) )
            {
                finished = true;
                return false;
            }
            else
            {
                parseError( "unknown token '"+key+"'" );
            }

            if(statusListener!=null) statusListener.onDataReceived();
        }
        while( loop );
        
        return true;
    }
    
    protected void readCE() throws IOException
    {
        String tag = getStringOrWordOrNumber();
        
        readAttributes( attributes );
        
        listener.onEdgeChanged( graphName, tag, new HashMap<String, Object>(attributes) );
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readCN() throws IOException
    {
        String tag = getStringOrWordOrNumber();
        
        readAttributes( attributes );
        
        listener.onNodeChanged( graphName, tag, new HashMap<String, Object>(attributes) );
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readCG() throws IOException
    {
        readAttributes( attributes );

        listener.onGraphChanged(new HashMap<String, Object>(attributes));
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readAE() throws IOException
    {
        int     dir      = 0;
        boolean directed = false;
        String  dirc     = null;
        String  tag      = null;
        String  fromTag  = null;
        String  toTag    = null;
        
        tag     = getStringOrWordOrNumber();
        fromTag = getStringOrWordOrNumber();
        dirc    = getWordOrSymbolOrNumberOrStringOrEolOrEof();
        
        if( dirc.equals( ">" ) )
        {
            directed = true;
            dir      = 1;
        }
        else if( dirc.equals( "<" ) )
        {
            directed = true;
            dir      = 2;
        }
        else
        {
            pushBack();
        }
        
        toTag = getStringOrWordOrNumber();
        
        if( dir == 2 )
        {
            String tmp = toTag;
            toTag      = fromTag;
            fromTag    = tmp;
        }
        
        readAttributes( attributes );
        listener.onEdgeAdded( graphName, tag, fromTag, toTag, directed, new HashMap<String, Object>(attributes) );
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readAN() throws IOException
    {
        String tag = getStringOrWordOrNumber();
        
        readAttributes( attributes );

        listener.onNodeAdded( graphName, tag, new HashMap<String, Object>(attributes) );
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readDE() throws IOException
    {
        String tag = getStringOrWordOrNumber();

        listener.onEdgeRemoved( graphName, tag );
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readDN() throws IOException
    {
        String tag = getStringOrWordOrNumber();

        listener.onNodeRemoved( graphName, tag );
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readST() throws IOException
    {
        String w = getWordOrNumber();
        
        try
        {
            double time = Double.parseDouble( w );

            listener.onStepBegins( graphName, time );
        }
        catch( NumberFormatException e )
        {
            parseError( "expecting a number after `st', got `" + w + "'" );
        }   
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }

    protected void readAttributes( HashMap<String,Object> attributes ) throws IOException
    {
        boolean del = false;
        String  key = getWordOrSymbolOrStringOrEolOrEof();
        
        attributes.clear();
        
        if( key.equals( "-" ) )
        {
            key = getWordOrSymbolOrStringOrEolOrEof();
            del = true;
        }
        
        if( key.equals( "+" ) )
            key = getWordOrSymbolOrStringOrEolOrEof();
        
        while( ! key.equals( "EOF" ) && ! key.equals( "EOL" ) && ! key.equals( "]" ) )
        {
            if( del )
                 attributes.put( key, null );
            else attributes.put( key, readAttributeValue( key ) );
            
            key = getWordOrSymbolOrStringOrEolOrEof();
            
            if( key.equals( "-" ) )
            {
                key = getWordOrStringOrEolOrEof();
                del = true;
            }
            
            if( key.equals( "+" ) )
            {
                key = getWordOrStringOrEolOrEof();
                del = false;
            }
        }
        
        pushBack();
    }
    
    /**
     * Read an attribute. The "key" (attribute name) is already read.
     * @param key The attribute name, already read.
     */
    protected Object readAttributeValue( String key ) throws IOException
    {
        ArrayList<Object> vector = null;
        Object            value  = null;
        Object            value2 = null;
        String            next   = null;
        
        eatSymbols( ":=" );
        
        value = getStringOrWordOrSymbolOrNumberO();
        
        if( value.equals( "[" ) )
        {
            HashMap<String,Object> map = new HashMap<String,Object>();
            
            readAttributes( map );
            eatSymbol( ']' );
            
            value = map;
        }
        else
        {
            pushBack();
            
            value = getStringOrWordOrNumberO();
            next  = getWordOrSymbolOrNumberOrStringOrEolOrEof();
            
            while( next.equals( "," ) )
            {
                if( vector == null )
                {
                    vector = new ArrayList<Object>();
                    vector.add( value );
                }
                
                value2 = getStringOrWordOrNumberO();
                next   = getWordOrSymbolOrNumberOrStringOrEolOrEof();
                
                vector.add( value2 );
            }
            
            pushBack();
        }
        
        if( vector != null )
             return vector.toArray();
        else return value;
    }

        /**
     * Generate a parse error.
     */
    protected void parseError( String message ) throws IOException
    {
        if(statusListener!=null) statusListener.onError();
        if (report!=null) {
            Issue issue = new Issue("parse error: "
                + st.lineno() + ": " + message, Issue.Level.SEVERE);
            report.logIssue(issue);
        }
        throw new IOException( "parse error: "
                + st.lineno() + ": " + message );
    }
    
}
