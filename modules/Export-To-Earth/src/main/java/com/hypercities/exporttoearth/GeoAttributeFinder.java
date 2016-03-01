/**
 * Copyright (c) 2012, David Shepard All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.hypercities.exporttoearth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.graph.api.Column;
import org.openide.util.NbBundle;

/**
 * Find columns with geographic attributes in Data Laboratory.
 * 
 * @author Dave Shepard
 */
public class GeoAttributeFinder {

    private Column longitudeColumn;
    private Column latitudeColumn;

    private String getMessage(String resource) {
        return NbBundle.getMessage(GeoAttributeFinder.class, resource);
    }

    Column[] findGeoFields(Column[] columns) {
        ArrayList<String> latAttributes = new ArrayList<String>();
        latAttributes.add(getMessage("Latitude"));
        for (String name : getMessage("LatitudeShortNames").split(",")) {
            latAttributes.add(name + "$");
        }
        latAttributes.add("^y$");
        for (String name : getMessage("LatitudeShortNames").split(",")) {
            latAttributes.add("(.*)" + name + "(.*)");
        }

        ArrayList<String> lonAttributes = new ArrayList<String>();
        lonAttributes.add(getMessage("Longitude"));
        for (String name : getMessage("LongitudeShortNames").split(",")) {
            lonAttributes.add(name + "$");
        }
        lonAttributes.add("^x$");
        for (String name : getMessage("LongitudeShortNames").split(",")) {
            latAttributes.add("(.*)" + name + "(.*)");
        }

        // find attributes by iterating over property names
        longitudeColumn = getAttributeField(lonAttributes.toArray(new String[0]), columns);
        latitudeColumn = getAttributeField(latAttributes.toArray(new String[0]), columns);
        Column[] result = {getLongitudeColumn(), getLatitudeColumn()};
        return result;
    }

    Column getAttributeField(String[] patterns, Column[] columns) {
        for (Column col : columns) {
            for (String str : patterns) {
                Pattern pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(col.getTitle());
                if (matcher.find() && (col.getTypeClass() == Float.class
                        || col.getTypeClass() == Double.class
                        || col.getTypeClass() == BigDecimal.class
                        )) {
                    return col;
                }
            }
        }
        return null;
    }

    /**
     * @return the column selected as the longitude column
     */
    public Column getLongitudeColumn() {
        return longitudeColumn;
    }

    /**
     * @return the column selected as the latitude column
     */
    public Column getLatitudeColumn() {
        return latitudeColumn;
    }
}
