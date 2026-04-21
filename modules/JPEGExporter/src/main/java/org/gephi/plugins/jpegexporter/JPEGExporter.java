package org.gephi.plugins.jpegexporter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

public class JPEGExporter implements VectorExporter, ByteExporter, LongTask {

    private static final float MM_PER_INCH = 25.4f;
    private static final float CM_PER_INCH = 2.54f;
    private static final float DEFAULT_WIDTH_CM = 16.0f;
    private static final float DEFAULT_HEIGHT_CM = 9.0f;
    private static final int DEFAULT_DPI = 300;

    private ProgressTicket progress;
    private boolean cancel;
    private Workspace workspace;
    private OutputStream stream;
    private float widthCm = DEFAULT_WIDTH_CM;
    private float heightCm = DEFAULT_HEIGHT_CM;
    private int dpi = DEFAULT_DPI;
    private int margin = 4;
    private G2DTarget target;

    @Override
    public boolean execute() {
        Progress.start(progress);

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel model = controller.getModel(workspace);

        setExportProperties(model);
        controller.refreshPreview(workspace);

        target = (G2DTarget) controller.getRenderTarget(RenderTarget.G2D_TARGET, workspace);
        if (target instanceof LongTask) {
            ((LongTask) target).setProgressTicket(progress);
        }

        try {
            target.refresh();
            Progress.switchToIndeterminate(progress);

            int widthPx = cmToPixels(widthCm, dpi);
            int heightPx = cmToPixels(heightCm, dpi);

            Image sourceImg = target.getImage();
            BufferedImage img = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = img.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, widthPx, heightPx);
            graphics.drawImage(sourceImg, 0, 0, null);
            graphics.dispose();

            writeJpegWithDpi(img, stream, dpi);
            stream.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            discardExportProperties(model);
            Progress.finish(progress);
        }

        return !cancel;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public float getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(float widthCm) {
        this.widthCm = widthCm;
    }

    public float getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(float heightCm) {
        this.heightCm = heightCm;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    private synchronized void setExportProperties(PreviewModel model) {
        PreviewProperties props = model.getProperties();
        props.putValue(PreviewProperty.VISIBILITY_RATIO, 1.0F);
        int widthPx = cmToPixels(widthCm, dpi);
        int heightPx = cmToPixels(heightCm, dpi);
        props.putValue("width", widthPx);
        props.putValue("height", heightPx);
        props.putValue(PreviewProperty.MARGIN, (float) margin);
    }

    private synchronized void discardExportProperties(PreviewModel model) {
        PreviewProperties props = model.getProperties();
        props.removeSimpleValue("width");
        props.removeSimpleValue("height");
        props.removeSimpleValue(PreviewProperty.MARGIN);
    }

    static void writeJpegWithDpi(BufferedImage image, OutputStream output, int dpiValue) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG ImageWriter available");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(0.95f);
        }

        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, writeParam);
        setDpiMetadata(metadata, dpiValue);

        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(image, null, metadata), writeParam);
            imageOutputStream.flush();
        } finally {
            writer.dispose();
        }
    }

    static int cmToPixels(float cm, int dpi) {
        return Math.max(1, Math.round((cm / CM_PER_INCH) * dpi));
    }

    static void setDpiMetadata(IIOMetadata metadata, int dpiValue) {
        if (metadata == null) {
            return;
        }

        if (metadata.isStandardMetadataFormatSupported()) {
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_1.0");
            IIOMetadataNode dimension = getOrCreateChild(root, "Dimension");
            float pixelsPerMM = MM_PER_INCH / dpiValue;

            IIOMetadataNode horizontal = getOrCreateChild(dimension, "HorizontalPixelSize");
            horizontal.setAttribute("value", Float.toString(pixelsPerMM));

            IIOMetadataNode vertical = getOrCreateChild(dimension, "VerticalPixelSize");
            vertical.setAttribute("value", Float.toString(pixelsPerMM));

            try {
                metadata.mergeTree("javax_imageio_1.0", root);
            } catch (Exception ignored) {
            }
        }

        // Avoid mutating native JPEG tree (`javax_imageio_jpeg_image_1.0`) as it can
        // break marker sequencing in some writers and produce unreadable JPEG files.
    }

    private static IIOMetadataNode getOrCreateChild(IIOMetadataNode parent, String name) {
        for (int i = 0; i < parent.getLength(); i++) {
            if (name.equals(parent.item(i).getNodeName())) {
                return (IIOMetadataNode) parent.item(i);
            }
        }
        IIOMetadataNode child = new IIOMetadataNode(name);
        parent.appendChild(child);
        return child;
    }
}
