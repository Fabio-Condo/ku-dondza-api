package com.fabiocondo.service.impl;

import com.fabiocondo.enumeration.ImageType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.fabiocondo.enumeration.ImageType.COURSE;
import static com.fabiocondo.enumeration.ImageType.PROFILE;

@Service
public class ImageProcessingService {

    public byte[] processImage(MultipartFile file, ImageType type) throws IOException {

        int width;
        int height;

        switch (type) {
            case PROFILE:
                width = 300; height = 300;
                break;
            case COURSE:
                width = 800; height = 450;
                break;
            default:
                throw new IllegalArgumentException("Unknown type");
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        BufferedImage resized = resize(originalImage, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", outputStream);

        return outputStream.toByteArray();
    }

    private BufferedImage resize(BufferedImage original, int width, int height) {
        Image tmp = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }
}
