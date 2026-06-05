package com.fabiocondo.service.impl;

import com.fabiocondo.enumeration.ImageType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageProcessingService {

    public byte[] processImage(MultipartFile file, ImageType type) throws IOException {

        int maxWidth;
        int maxHeight;

        switch (type) {
            case PROFILE:
                maxWidth = 300;
                maxHeight = 300;
                break;

            case SUBJECT:
                maxWidth = 800;
                maxHeight = 450;
                break;

            case QUESTION:
                maxWidth = 1000;
                maxHeight = 1000;
                break;

            default:
                throw new IllegalArgumentException("Unknown image type: " + type);
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        BufferedImage resizedImage =
                resizeKeepingAspectRatio(originalImage, maxWidth, maxHeight);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Mantém JPEG para compatibilidade com o teu sistema atual
        ImageIO.write(resizedImage, "jpg", outputStream);

        return outputStream.toByteArray();
    }

    private BufferedImage resizeKeepingAspectRatio(BufferedImage original, int maxWidth, int maxHeight) {

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // Não ampliar imagens pequenas
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original;
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;

        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = (int) Math.round(originalWidth * scale);
        int newHeight = (int) Math.round(originalHeight * scale);

        Image scaledImage = original.getScaledInstance(
                newWidth,
                newHeight,
                Image.SCALE_SMOOTH
        );

        BufferedImage resized = new BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.drawImage(scaledImage, 0, 0, null);
        graphics.dispose();

        return resized;
    }
}