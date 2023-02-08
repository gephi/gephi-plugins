/*
 * Gephi Seadragon Plugin
 *
 * Copyright 2010-2011 Gephi
 * Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * Licensed under Apache 2 License (http://www.apache.org/licenses/LICENSE-2.0)
 */
package org.gephi.plugins.seadragon;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
/**
 *
 * @author Mathieu Bastian
 */
public class SeadragonExporter implements Exporter, LongTask {

    //Const
    private static final String XML_FILE = "map.xml";
    private static final String PATH_MAP = "map";
    private static final String PATH_FILES = "_files";
    //Architecture
    private Workspace workspace;
    private ProgressTicket progress;
    private boolean cancel = false;
    private final PNGExporter pngExporter = new PNGExporter();
    private TileRenderer tileRenderer;
    //Settings
    private int width;
    private int height;
    private int margin;
    private File path;
    private int overlap = 1;
    private int tileSize = 256;
    
    @Override
    public boolean execute() {
        
        Progress.start(progress);
        Progress.setDisplayName(progress, "Export Seadragon");
        
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
        controller.refreshPreview(workspace);
        
        PreviewProperties props = controller.getModel(workspace).getProperties();
        props.putValue("width", width);
        props.putValue("height", height);
        props.putValue(PreviewProperty.MARGIN, (float) margin);
        G2DTarget target = (G2DTarget) controller.getRenderTarget(RenderTarget.G2D_TARGET, workspace);
        
        target.refresh();
    
        Progress.switchToIndeterminate(progress);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(target.getImage(), 0, 0, null);
   
        try {
            export(img);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        createXML();
        exportOtherFiles();
        
        Progress.finish(progress);
        
        return !cancel;
    }
    
    public void export(BufferedImage img) throws Exception {
        delete(new File(path, PATH_MAP));
        File folder = new File(path, PATH_MAP);
        folder.mkdir();
        folder = new File(folder, PATH_MAP + PATH_FILES);
        folder.mkdir();
        
        int numLevels = (int) Math.ceil(Math.log(Math.max(img.getWidth(), img.getHeight())) / Math.log(2.));
        int w = img.getWidth();
        int h = img.getHeight();
        
        //Calculate tasks count
        int tasks = 0;
        for (int level = numLevels; level >= 0; level--) {
            float levelScale = 1f / (1 << (numLevels - level));
            tasks += (int) Math.ceil(levelScale * w / tileSize) * (int) Math.ceil(levelScale * h / tileSize);
        }

        Progress.switchToDeterminate(progress, tasks);
        
        //Tile renderer
        tileRenderer = new TileRenderer(folder, tileSize, overlap);
        tileRenderer.setProgressTicket(progress);
        for (int level = numLevels; level >= 0 && !cancel; level--) {
            File levelFolder = new File(folder, "" + (level));
            levelFolder.mkdir();
            float levelScale = 1f / (1 << (numLevels - level));
            tileRenderer.writeLevel(img, levelScale, level);
        }
        
        tileRenderer = null;
    }
    
    public void createXML() {
        File file = new File(path + File.separator + PATH_MAP + File.separator + XML_FILE);
        org.w3c.dom.Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
        } catch (ParserConfigurationException | DOMException ex) {
            throw new RuntimeException("Can't create XML file", ex);
        }
        
        Element imageElement = document.createElement("Image");
        imageElement.setAttribute("TileSize", String.valueOf(tileSize));
        imageElement.setAttribute("Overlap", String.valueOf(overlap));
        imageElement.setAttribute("Format", "png");
        imageElement.setAttribute("ServerFormat", "Default");
        imageElement.setAttribute("xmlns", "http://schemas.microsoft.com/deepzoom/2009");
        
        Element sizeElement = document.createElement("Size");
        sizeElement.setAttribute("Width", String.valueOf(width));
        sizeElement.setAttribute("Height", String.valueOf(height));
        imageElement.appendChild(sizeElement);
        document.appendChild(imageElement);
        
        try {
            Source source = new DOMSource(document);
            Result result = new StreamResult(file);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
        } catch (IllegalArgumentException | TransformerException ex) {
            throw new RuntimeException("Can't write XML file", ex);
        }
    }
    
    private void exportOtherFiles() {
        try {
            copyFromJar("seadragon.html", path);
            copyFromJar("img/fullpage_grouphover.png", path);
            copyFromJar("img/fullpage_hover.png", path);
            copyFromJar("img/fullpage_pressed.png", path);
            copyFromJar("img/fullpage_rest.png", path);
            copyFromJar("img/home_grouphover.png", path);
            copyFromJar("img/home_hover.png", path);
            copyFromJar("img/home_pressed.png", path);
            copyFromJar("img/home_rest.png", path);
            copyFromJar("img/zoomin_grouphover.png", path);
            copyFromJar("img/zoomin_hover.png", path);
            copyFromJar("img/zoomin_pressed.png", path);
            copyFromJar("img/zoomin_rest.png", path);
            copyFromJar("img/zoomout_grouphover.png", path);
            copyFromJar("img/zoomout_hover.png", path);
            copyFromJar("img/zoomout_pressed.png", path);
            copyFromJar("img/zoomout_rest.png", path);
            copyFromJar("js/seadragon-min.js", path);
        } catch (Exception ex) {
            Logger.getLogger(SeadragonExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void copyFromJar(String source, File folder) throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/org/gephi/plugins/seadragon/" + source)) {
            File file = new File(folder + (folder.getPath().endsWith(File.separator) ? "" : File.separator) + source);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
        }
    }
    
    public int getTileSize() {
        return tileSize;
    }
    
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }
    
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    
    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
    
    public int getMargin() {
        return margin;
    }
    
    public void setMargin(int margin) {
        this.margin = margin;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getOverlap() {
        return overlap;
    }
    
    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public File getPath() {
        return path;
    }
    
    public void setPath(File path) {
        this.path = path;
    }
    
    @Override
    public boolean cancel() {
        this.cancel = true;
        pngExporter.cancel();
        if (tileRenderer != null) {
            tileRenderer.cancel();
        }
        return true;
    }
    
    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
    
    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (f.exists() && !f.delete()) {
            throw new IOException("Failed to delete file: " + f);
        }
    }
}
