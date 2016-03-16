/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/*
 Copyright 2008-2013 Clement Levallois
 Authors : Clement Levallois <clementlevallois@gmail.com>
 Website : http://www.clementlevallois.net


 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Clement Levallois. All rights reserved.

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

 Contributor(s): Clement Levallois

 */
public class Utils {

    public List<String> getListOfLinks(String[] arrayOfObjects, boolean selfLoopsRemoved) {
        List<String> listObjects = new ArrayList();

        if (selfLoopsRemoved) {
            Set<String> setObjects = new HashSet();
            setObjects.addAll(Arrays.asList(arrayOfObjects));
            listObjects.addAll(setObjects);
        } else {
            listObjects.addAll(Arrays.asList(arrayOfObjects));
        }

        List<String> listPairs = new ArrayList();
        Iterator<String> listIterator1 = listObjects.listIterator();
        Iterator<String> listIterator2;
        int count = 1;
        String object1;
        while (listIterator1.hasNext()) {
            object1 = listIterator1.next().trim();
            listIterator2 = listObjects.listIterator(count++);
            while (listIterator2.hasNext()) {
                String object2 = listIterator2.next().trim();
                if (!object2.trim().isEmpty() & !object1.trim().isEmpty()) {
                    if (object2.compareTo(object1) > 0) {
                        listPairs.add(object2 + "|" + object1);
                    } else {
                        listPairs.add(object1 + "|" + object2);
                    }
                }
            }
        }
        return listPairs;
    }

    public static String getCharacter(String labelCharacter) {
        if (labelCharacter == null) {
            return null;
        } else if (labelCharacter.equals("tab")) {
            return "\t";
        } else if (labelCharacter.equals("comma")) {
            return ",";
        } else if (labelCharacter.equals("semicolon")) {
            return ";";
        } else if (labelCharacter.equals("space")) {
            return " ";
        } else {
            return labelCharacter;
        }
    }
}
