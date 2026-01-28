package com.teklif.app.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Slf4j
@Component
public class ImageCompressionUtil {

    @Value("${file.upload.image.compression.enabled:true}")
    private boolean compressionEnabled;

    @Value("${file.upload.image.compression.quality:0.8}")
    private float compressionQuality;

    @Value("${file.upload.image.thumbnail.max-width:1920}")
    private int maxThumbnailWidth;

    @Value("${file.upload.image.thumbnail.max-height:1080}")
    private int maxThumbnailHeight;

    /**
     * Compress image if compression is enabled
     */
    public byte[] compressImage(InputStream inputStream, String contentType) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }

        // If compression is disabled, return original
        if (!compressionEnabled) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, getImageFormat(contentType), baos);
            return baos.toByteArray();
        }

        // Resize if necessary
        BufferedImage processedImage = resizeIfNeeded(originalImage);

        // Compress
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = getImageFormat(contentType);

        // For JPEG, we can use compression quality
        if ("jpg".equals(format) || "jpeg".equals(format)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();

                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(compressionQuality);

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                    writer.write(null, new IIOImage(processedImage, null, null), param);
                }
            } else {
                ImageIO.write(processedImage, format, baos);
            }
        } else {
            // For PNG and other formats, use default
            ImageIO.write(processedImage, format, baos);
        }

        return baos.toByteArray();
    }

    /**
     * Resize image if it exceeds max dimensions
     */
    private BufferedImage resizeIfNeeded(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Check if resize is needed
        if (originalWidth <= maxThumbnailWidth && originalHeight <= maxThumbnailHeight) {
            return originalImage;
        }

        // Calculate new dimensions maintaining aspect ratio
        int newWidth = maxThumbnailWidth;
        int newHeight = (int) ((double) originalHeight / originalWidth * maxThumbnailWidth);

        // If height is still too large, scale based on height
        if (newHeight > maxThumbnailHeight) {
            newHeight = maxThumbnailHeight;
            newWidth = (int) ((double) originalWidth / originalHeight * maxThumbnailHeight);
        }

        // Resize
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        log.info("Image resized from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);

        return resizedImage;
    }

    private String getImageFormat(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        if (contentType.contains("png")) {
            return "png";
        } else if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return "jpg";
        } else if (contentType.contains("gif")) {
            return "gif";
        } else if (contentType.contains("bmp")) {
            return "bmp";
        } else if (contentType.contains("webp")) {
            return "webp";
        }
        return "jpg";
    }

    /**
     * Check if content type is an image
     */
    public static boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}

