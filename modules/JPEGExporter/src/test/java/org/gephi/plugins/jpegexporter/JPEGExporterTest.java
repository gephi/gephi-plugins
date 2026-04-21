package org.gephi.plugins.jpegexporter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

public class JPEGExporterTest {

    @Test
    public void testCmToPixels() {
        Assert.assertEquals(300, JPEGExporter.cmToPixels(2.54f, 300));
        Assert.assertEquals(2480, JPEGExporter.cmToPixels(21.0f, 300));
        Assert.assertEquals(1, JPEGExporter.cmToPixels(0.001f, 72));
    }

    @Test
    public void testWriteJpegWithDpiProducesReadableJpeg() throws Exception {
        BufferedImage source = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        JPEGExporter.writeJpegWithDpi(source, outputStream, 300);

        byte[] bytes = outputStream.toByteArray();
        Assert.assertTrue(bytes.length > 0);
        BufferedImage parsed = ImageIO.read(new ByteArrayInputStream(bytes));
        Assert.assertNotNull("Written JPEG should be readable", parsed);
        Assert.assertEquals(320, parsed.getWidth());
        Assert.assertEquals(240, parsed.getHeight());
    }

    @Test
    public void testSetDpiMetadataWritesStandardDimension() throws Exception {
        IIOMetadata metadata = createJpegMetadata();
        Assert.assertTrue(metadata.isStandardMetadataFormatSupported());

        JPEGExporter.setDpiMetadata(metadata, 300);

        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_1.0");
        IIOMetadataNode dimension = findChild(root, "Dimension");
        Assert.assertNotNull(dimension);
        IIOMetadataNode horizontal = findChild(dimension, "HorizontalPixelSize");
        IIOMetadataNode vertical = findChild(dimension, "VerticalPixelSize");
        Assert.assertNotNull(horizontal);
        Assert.assertNotNull(vertical);
        Assert.assertTrue(Float.parseFloat(horizontal.getAttribute("value")) > 0f);
        Assert.assertTrue(Float.parseFloat(vertical.getAttribute("value")) > 0f);
    }

    private IIOMetadata createJpegMetadata() throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        Assert.assertTrue("No JPEG writer found", writers.hasNext());
        ImageWriter writer = writers.next();
        try {
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            return writer.getDefaultImageMetadata(
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB),
                writeParam
            );
        } finally {
            writer.dispose();
        }
    }

    private IIOMetadataNode findChild(IIOMetadataNode parent, String name) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (name.equals(node.getNodeName())) {
                return (IIOMetadataNode) node;
            }
        }
        return null;
    }
}
