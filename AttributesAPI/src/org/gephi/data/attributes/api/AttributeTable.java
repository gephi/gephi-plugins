/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla
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
package org.gephi.data.attributes.api;

import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.properties.PropertiesColumn;

/**
 * Table hosts columns and permits all manipulation on them. Columns can be
 * appened with different level of details. The table maintains a map with
 * column identifier and title (header) in order they can be retrieved efficiently.
 * <p>
 * Tracking added or removed columns can be performed by adding an
 * {@link AttributeListener} to this table.
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 * @see AttributeColumn
 * @see AttributeRow
 */
public interface AttributeTable {

    /**
     * Returns the name of this table.
     *
     * @return          the name of this table
     */
    public String getName();

    /**
     * Returns the current columns. Call this method to iterate over columns.
     *
     * @return              the current columns.
     */
    public AttributeColumn[] getColumns();

    /**
     * Returns the number of column in this table.
     *
     * @return              the number of columns
     */
    public int countColumns();

    /**
     * <p>Creates and add a new column to this table. The default origin is set at <code>DATA</code>.</p>
     * <p>The title of the column is the identifier.</p>
     * @param id            the identifier of the column
     * @param type          the type of the column
     * @return              the newly created column
     */
    public AttributeColumn addColumn(String id, AttributeType type);

    /**
     * <p>Creates and add a new column to this table.</p>
     * <p>The title of the column is the identifier.</p>
     * @param id            the identifier of the column
     * @param type          the type of the column
     * @param origin        the origin of the column
     * @return              the newly created column
     */
    public AttributeColumn addColumn(String id, AttributeType type, AttributeOrigin origin);

    /**
     * <p>Creates and add a new column to this table.</p>
     * <p>The title can't be null, empty or already existing in the table</p>
     * @param id            the identifier of the column
     * @param title         the title of the column
     * @param type          the type of the column
     * @param origin        the origin of the column.
     * @param defaultValue  the default value of the column.
     * @return              the newly created column
     */
    public AttributeColumn addColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue);

    /**
     * <p>Creates and add a new column to this table.</p>
     * <p>The title can't be null, empty or already existing in the table</p>
     * <p>Attribute origin will be set to AttributeOrigin.DELEGATE.</p>     *
     * @param id                             the identifier of the column
     * @param title                          the title of the column
     * @param type                           the type of the column
     * @param attributeValueDelegateProvider the attribute value delegate provider of the column
     * @param defaultValue                   the default value of the column
     * @return                               the newly created column
     */
    public AttributeColumn addColumn(String id, String title, AttributeType type, AttributeValueDelegateProvider attributeValueDelegateProvider, Object defaultValue);

    /**
     * Creates and add a new properties column to this table. All needed informations are set in
     * PropertiesColumn enum instance.     *
     * @param propertiesColumn the properties column
     * @return                 the newly created column
     */
    public AttributeColumn addPropertiesColumn(PropertiesColumn propertiesColumn);

    /**
     * If exists, remove the column and all rows values.
     *
     * @param column        the column that is to be removed
     */
    public void removeColumn(AttributeColumn column);

    /**
     * If exists, replace <code>source</code> by the new column created from params.
     * @param source                        the column that is to be removed
     * @param id                             the identifier of the column
     * @param title                          the title of the column
     * @param type                           the type of the column
     * @param defaultValue                   the default value of the column
     * @return                               the newly created column, or
     * <code>null</code> if <code>source</code> can't be found
     */
    public AttributeColumn replaceColumn(AttributeColumn source, String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue);

    /**
     * Gets the column at the <code>index</code> of <code>null</code> if the
     * index is not valid.
     *
     * @param index         a valid column index range
     * @return              the column, or <code>null</code> if not found
     */
    public AttributeColumn getColumn(int index);

    /**
     * Gets the column with the given identifier or <code>null</code> if it is
     * not found.
     * 
     * @param id            the column <code>id</code> or <code>title</code>
     * @return              the column, or <code>null</code> if not found
     */
    public AttributeColumn getColumn(String id);

    /**
     * Gets the column which match the given parameters or <code>null</code> 
     * if it is not found.
     *
     * @param title         the column <code>id</code> or <code>title</code>
     * @param type          the column <code>type</code>
     * @return              the column, or <code>null</code> if not found
     */
    public AttributeColumn getColumn(String title, AttributeType type);

    /**
     * Return <code>true</code> if this table has a column with the given
     * <code>title</code> or <code>id</code>.
     * 
     * @param title         the column <code>title</code> that is to be searched
     * @return              <code>true</code> if found, or <code>false</code>
     *                      otherwise
     */
    public boolean hasColumn(String title);

    /**
     * Merge this table with the given <code>table</code> given. New columns from
     * <code>table</code> are added to this table. 
     * <p>
     * Columns are compared according to their <code>id</code> and <code>type</code>.
     * Columns found in <code>model</code> are appended only if they no column
     * exist with the same <code>id</code> and <code>type</code>.
     * 
     * @param table         the table that is to be merged with this table
     */
    public void mergeTable(AttributeTable table);
}
