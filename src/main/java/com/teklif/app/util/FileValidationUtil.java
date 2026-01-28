package com.teklif.app.util;

import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public class FileValidationUtil {

    // Allowed file extensions
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"
    ));

    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv", ".rtf"
    ));

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm", ".mpeg"
    ));

    // Magic numbers for file type validation (first bytes)
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04, 0x50, 0x4B, 0x05, 0x06}; // DOCX, XLSX are ZIP files

    // Max file sizes (in bytes)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long MAX_OTHER_SIZE = 5 * 1024 * 1024; // 5MB

    // Suspicious file patterns (path traversal attempts)
    private static final List<String> SUSPICIOUS_PATTERNS = Arrays.asList(
            "../", "..\\", "./", ".\\", "%2e%2e", "%252e", "null", "undefined"
    );

    public static ValidationResult validateFile(MultipartFile file) {
        ValidationResult result = new ValidationResult();
        result.valid = true;

        // Check if file is empty
        if (file == null || file.isEmpty()) {
            result.valid = false;
            result.error = "File is empty";
            return result;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            result.valid = false;
            result.error = "File name is empty";
            return result;
        }

        // Check for suspicious patterns in filename
        for (String pattern : SUSPICIOUS_PATTERNS) {
            if (originalFilename.toLowerCase().contains(pattern)) {
                result.valid = false;
                result.error = "Suspicious file name detected";
                return result;
            }
        }

        // Get file extension
        String extension = getFileExtension(originalFilename).toLowerCase();

        // Validate extension
        if (!isAllowedExtension(extension)) {
            result.valid = false;
            result.error = "File type not allowed. Allowed types: images (jpg, png, gif, webp), documents (pdf, docx, xlsx), videos (mp4, mov)";
            return result;
        }

        // Validate file size based on type
        long maxSize =getMaxSizeForType(extension);
        if (file.getSize() > maxSize) {
            result.valid = false;
            result.error = String.format("File size exceeds maximum allowed size of %d MB for this file type", maxSize / (1024 * 1024));
            return result;
        }

        // Validate magic number (file signature)
        try {
            if (!isValidMagicNumber(file, extension)) {
                result.valid = false;
                result.error = "File content does not match the file extension. The file may be corrupted or renamed.";
                return result;
            }
        } catch (IOException e) {
            result.valid = false;
            result.error = "Failed to validate file content";
            return result;
        }

        // Check for double extensions (e.g., file.jpg.exe)
        if (hasDoubleExtension(originalFilename)) {
            result.valid = false;
            result.error = "Files with double extensions are not allowed";
            return result;
        }

        result.fileType = determineFileType(extension);
        result.extension = extension;

        return result;
    }

    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return "";
    }

    private static boolean isAllowedExtension(String extension) {
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension) ||
               ALLOWED_DOCUMENT_EXTENSIONS.contains(extension) ||
               ALLOWED_VIDEO_EXTENSIONS.contains(extension);
    }

    private static long getMaxSizeForType(String extension) {
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return MAX_IMAGE_SIZE;
        } else if (ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            return MAX_DOCUMENT_SIZE;
        } else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return MAX_VIDEO_SIZE;
        }
        return MAX_OTHER_SIZE;
    }

    private static boolean isValidMagicNumber(MultipartFile file, String extension) throws IOException {
        byte[] fileBytes = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(fileBytes);
            if (bytesRead < 4) {
                return false;
            }
        }

        // For images, check magic numbers
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            if (".png".equals(extension)) {
                return startsWith(fileBytes, PNG_MAGIC);
            } else if (".jpg".equals(extension) || ".jpeg".equals(extension)) {
                return startsWith(fileBytes, JPEG_MAGIC);
            } else if (".gif".equals(extension) || ".bmp".equals(extension) || ".webp".equals(extension)) {
                // These formats are harder to validate by magic number, skip for now
                return true;
            } else if (".svg".equals(extension)) {
                // SVG is XML text, check content instead
                return validateSvgContent(file);
            }
        }

        // For documents
        if (".pdf".equals(extension)) {
            return startsWith(fileBytes, PDF_MAGIC);
        } else if (".docx".equals(extension) || ".xlsx".equals(extension) || ".pptx".equals(extension)) {
            return startsWith(fileBytes, ZIP_MAGIC);
        }

        // For videos, we'll rely on extension validation
        if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return true;
        }

        return true;
    }

    private static boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateSvgContent(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        return content.trim().startsWith("<svg") || content.contains("<?xml");
    }

    private static boolean hasDoubleExtension(String filename) {
        String lowerName = filename.toLowerCase();
        int dotCount = 0;
        for (int i = 0; i < lowerName.length(); i++) {
            if (lowerName.charAt(i) == '.') {
                dotCount++;
                if (dotCount > 1) {
                    // Check if this is a legitimate double extension (e.g., .tar.gz)
                    if (!lowerName.endsWith(".tar.gz")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String determineFileType(String extension) {
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return "IMAGE";
        } else if (ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            return "DOCUMENT";
        } else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return "VIDEO";
        }
        return "OTHER";
    }

    public static class ValidationResult {
        public boolean valid;
        public String error;
        public String fileType;
        public String extension;
    }
}
