/**
 * Original Version Copyright (c) 2012, David Shepard All rights reserved.
 * Modified by Roman Seidl (2017)
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
 * Imported Version: March 26, 2013
 * https://github.com/shepdl/Export-To-Earth/commit/01333bc3be4bc34049fe325502c2e83c070c37c7
 */
package at.granul.gephi.shpexporter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;

/**
 *
 * @author Dave Shepard
 */
public class GeoAttributeFinder {

    private Column longitudeColumn;
    private Column latitudeColumn;

    public Column[] findGeoFields(Column[] columns) {
        String[] latAttributes = {"latitude", "^lat$", "^y$", "(.*)lat(.*)"};
        String[] lonAttributes = {"longitude", "lon", "lng", "^x$", "(.*)lon(.*)", "(.*)lng(.*)"};

        // find attributes by iterating over property names
        longitudeColumn = getAttributeField(lonAttributes, columns);
        latitudeColumn = getAttributeField(latAttributes, columns);
        Column[] result = {getLongitudeColumn(), getLatitudeColumn()};
        return result;
    }

    Column getAttributeField(String[] patterns, Column[] columns) {
        for (Column col : columns) {
            for (String str : patterns) {
                Pattern pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(col.getTitle());
                if (matcher.find() && AttributeUtils.isNumberType(col.getTypeClass())) {
                    return col;
                }
            }
        }
        return null;
    }

    /**
     * @return the longitudeColumn
     */
    public Column getLongitudeColumn() {
        return longitudeColumn;
    }

    /**
     * @return the latitudeColumn
     */
    public Column getLatitudeColumn() {
        return latitudeColumn;
    }
}
