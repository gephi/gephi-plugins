/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.streaming.impl.dgs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;


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
    
    public DGSParser(InputStream inputStream, DGSParserListener listener) {
        super(inputStream);
        this.listener = listener;
    }
    
    public void parse() throws IOException {
        this.begin();
        while(this.next(true, true)) {};
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
        
        if(  graphName != null )
             listener.onGraphAttributeAdded( graphName, "label", graphName );
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
        }
        while( loop );
        
        return true;
    }
    
    protected void readCE() throws IOException
    {
        String tag = getStringOrWordOrNumber();
        
        readAttributes( attributes );
        
        for( String key: attributes.keySet() )
        {
            Object value = attributes.get( key );
                
            if( value == null )
                listener.onEdgeAttributeRemoved( graphName, tag, key );
            else listener.onEdgeAttributeChanged( graphName, tag, key, null, value );
        }
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readCN() throws IOException
    {
        String tag = getStringOrWordOrNumber();
        
        readAttributes( attributes );
        
        for( String key: attributes.keySet() )
        {
            Object value = attributes.get( key );
                
            if( value == null )
                listener.onNodeAttributeRemoved( graphName, tag, key );
            else listener.onNodeAttributeChanged( graphName, tag, key, null, value );
        }
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readCG() throws IOException
    {
        readAttributes( attributes );
        
        for( String key: attributes.keySet() )
        {
            Object value = attributes.get( key );
                
            if( value == null )
                listener.onGraphAttributeRemoved( graphName, key );
            else listener.onGraphAttributeChanged( graphName, key, null, value );
        }
        
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
        listener.onEdgeAdded( graphName, tag, fromTag, toTag, directed );
        
        for( String key: attributes.keySet() )
        {
            Object value = attributes.get( key );
            listener.onEdgeAttributeAdded( graphName, tag, key, value );
        }
        
        if( eatEolOrEof() == StreamTokenizer.TT_EOF )
            pushBack();
    }
    
    protected void readAN() throws IOException
    {
        String tag = getStringOrWordOrNumber();
        
        readAttributes( attributes );

        listener.onNodeAdded( graphName, tag, attributes );
        
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
            
            readAttributes( map );;
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
    
}
