package com.vattima.lego.imaging.service;

import com.vattima.lego.imaging.LegoImagingException;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ImageScalingService {
    public Path scale(Path path) {
        Path tempFile = null;

        BufferedImage originalImage = null;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        double scaleFactor = 1.0d;
        try {
            tempFile = java.nio.file.Files.createTempFile(Paths.get("C:\\temp"), "resized-" + path.getFileName(), ".jpg");

            InputStream fis = new FileInputStream(path.toFile());
            bis = new BufferedInputStream(fis, 1024*16);
            originalImage = ImageIO.read(bis);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(baos, 1024*16);
            int iteration = 0;
            boolean iterationWasUnderSize = false;
            BufferedImage resizedImage = originalImage;
            int width = originalImage.getWidth();
            do {
                if (iteration > 0) {
                    scaleFactor = (iterationWasUnderSize?1.5:scaleFactor - 0.001d);
                    resizedImage = Scalr.resize(originalImage, (int)(width*scaleFactor));
                    width = resizedImage.getWidth();
                }
                baos = new ByteArrayOutputStream();
                bos = new BufferedOutputStream(baos, 1024*16);
                ImageIO.write(resizedImage, "JPG", bos);
                int size = baos.size();
                bos.close();
                iterationWasUnderSize = size < 2000000L;
                if (inRange(size) || (iterationWasUnderSize && iteration == 0)) {
                    FileOutputStream fos = new FileOutputStream(tempFile.toFile());
                    bos = new BufferedOutputStream(fos, 1024*16);
                    ImageIO.write(resizedImage, "JPG", bos);
                    break;
                }
                iteration++;
            } while (true);
        } catch (Exception e) {
            throw new LegoImagingException(e);
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tempFile;
    }

    public Path scale2(Path path) {
        Path tempFile = null;

        BufferedImage originalImage = null;

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            tempFile = java.nio.file.Files.createTempFile(Paths.get("C:\\temp"), "resized-" + path.getFileName(), ".jpg");

            InputStream fis = new FileInputStream(path.toFile());
            bis = new BufferedInputStream(fis, 1024*16);
            originalImage = ImageIO.read(bis);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(baos, 1024*16);
            int iteration = 0;
            boolean iterationWasUnderSize = false;
            BufferedImage resizedImage = originalImage;
            int width = originalImage.getWidth();
            do {
                if (iteration > 0) {
                    double scaleFactor = (iterationWasUnderSize?1.5:0.5);
                    resizedImage = Scalr.resize(originalImage, (int)(width*scaleFactor));
                    width = resizedImage.getWidth();
                }
                baos = new ByteArrayOutputStream();
                bos = new BufferedOutputStream(baos, 1024*16);
                ImageIO.write(resizedImage, "JPG", bos);
                int size = baos.size();
                bos.close();
                iterationWasUnderSize = size < 2000000L;
                if (inRange(size) || (iterationWasUnderSize && iteration == 0)) {
                    FileOutputStream fos = new FileOutputStream(tempFile.toFile());
                    bos = new BufferedOutputStream(fos, 1024*16);
                    ImageIO.write(resizedImage, "JPG", bos);
                    break;
                }
                iteration++;
                System.out.println("iteration ["+iteration+"]");
            } while (true);
        } catch (Exception e) {
            throw new LegoImagingException(e);
        } finally {
            try {
                bis.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tempFile;
    }
    private boolean inRange(long size) {
        return (size < 2000000L);
    }
}
