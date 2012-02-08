/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.preview;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.preview.api.PreviewPreset;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
public class PresetUtils {

    private List<PreviewPreset> presets;

    public void savePreset(PreviewPreset preset) {
        int exist = -1;
        for (int i = 0; i < presets.size(); i++) {
            PreviewPreset p = presets.get(i);
            if (p.getName().equals(preset.getName())) {
                exist = i;
                break;
            }
        }
        if (exist == -1) {
            addPreset(preset);
        } else {
            presets.set(exist, preset);
        }

        try {
            //Create file if dont exist
            FileObject folder = FileUtil.getConfigFile("previewpresets");
            if (folder == null) {
                folder = FileUtil.getConfigRoot().createFolder("previewpresets");
            }
            FileObject presetFile = folder.getFileObject(preset.getName(), "xml");
            if (presetFile == null) {
                presetFile = folder.createData(preset.getName(), "xml");
            }

            //Create doc
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);

            //Write doc
            writeXML(document, preset);

            //Write XML file
            Source source = new DOMSource(document);
            Result result = new StreamResult(FileUtil.toFile(presetFile));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PreviewPreset[] getPresets() {
        if (presets == null) {
            presets = new ArrayList<PreviewPreset>();
            loadPresets();
        }
        return presets.toArray(new PreviewPreset[0]);
    }

    private void loadPresets() {
        FileObject folder = FileUtil.getConfigFile("previewpresets");
        if (folder != null) {
            for (FileObject child : folder.getChildren()) {
                if (child.isValid() && child.hasExt("xml")) {
                    try {
                        InputStream stream = child.getInputStream();
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(stream);
                        PreviewPreset preset = readXML(document);
                        addPreset(preset);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void writeXML(Document doc, PreviewPreset preset) {
        Element presetE = doc.createElement("previewpreset");
        presetE.setAttribute("name", preset.getName());
        presetE.setAttribute("version", "0.7");

        for (Entry<String, String> entry : PreviewPreset.serialize(preset).entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();

            Element propertyE = doc.createElement("previewproperty");
            propertyE.setAttribute("name", propertyName);
            propertyE.setTextContent(propertyValue);
            presetE.appendChild(propertyE);
        }
        doc.appendChild(presetE);
    }

    private PreviewPreset readXML(Document document) {
        Element presetE = document.getDocumentElement();
        Map<String, String> propertiesMap = new HashMap<String, String>();

        NodeList propertyList = presetE.getElementsByTagName("previewproperty");
        for (int i = 0; i < propertyList.getLength(); i++) {
            Node n = propertyList.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element propertyE = (Element) n;
                String name = propertyE.getAttribute("name");
                String value = propertyE.getTextContent();
                if (!value.isEmpty()) {
                    propertiesMap.put(name, value);
                }
            }
        }
        String name = presetE.getAttribute("name");
        return PreviewPreset.deserialize(name, propertiesMap);
    }

    private void addPreset(PreviewPreset preset) {
        presets.add(preset);
    }
}
