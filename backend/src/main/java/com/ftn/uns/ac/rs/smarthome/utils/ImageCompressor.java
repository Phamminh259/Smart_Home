//package com.ftn.uns.ac.rs.smarthome.utils;
//
//import javax.imageio.IIOImage;
//import javax.imageio.ImageIO;
//import javax.imageio.ImageWriteParam;
//import javax.imageio.ImageWriter;
//import javax.imageio.stream.ImageOutputStream;
//import java.awt.image.BufferedImage;
//import java.io.*;
//import java.nio.file.Files;
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//import java.util.Properties;
//
//import com.ftn.uns.ac.rs.smarthome.config.MqttConfiguration;
//import org.apache.commons.io.FilenameUtils;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ResponseStatusException;
//
//
//public class ImageCompressor {
//    public static File compressImage(File image, float qualityOfOutputImage, String username) throws IOException {
//        Properties env = new Properties();
//        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
//        String tempFilePath = env.getProperty("tempfolder.path");
//        String imageFile = image.getName();
//        String extension = FilenameUtils.getExtension(imageFile);
//        String finalNamePath = tempFilePath + "/" + username + "." + extension;
//        File compressedImageFile = new File(finalNamePath);
//        try {
//            /*BufferedImage originalImage = ImageIO.read(image);
//
//            ImageWriteParam imageWriteParam = ImageIO.getImageWritersByFormatName(extension).next().getDefaultWriteParam();
//
//            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            imageWriteParam.setCompressionQuality(qualityOfOutputImage);
//
//            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(extension);
//            ImageWriter imageWriter = imageWriters.next();
//            File source = new File(finalNamePath);
//
//            imageWriter.setOutput(ImageIO.createImageOutputStream(source));
//            imageWriter.write(null, new IIOImage(originalImage, null, null), imageWriteParam);
//            imageWriter.dispose();
//            image.delete();
//            return new File(finalNamePath);*/
//
//            InputStream inputStream = new FileInputStream(image);
//            OutputStream outputStream = new FileOutputStream(compressedImageFile);
//
//            BufferedImage bufferedImage = ImageIO.read(inputStream);
//
//            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(extension);
//
//            if (!imageWriters.hasNext())
//                throw new NoSuchElementException("Writers Not Found!!");
//
//            ImageWriter imageWriter =  imageWriters.next();
//            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
//            imageWriter.setOutput(imageOutputStream);
//
//            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
//
//            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            imageWriteParam.setCompressionQuality(qualityOfOutputImage);
//
//            imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParam);
//
//            inputStream.close();
//            outputStream.close();
//            imageOutputStream.close();
//            imageWriter.dispose();
//            image.delete();
//            return compressedImageFile;
//
//        } catch (IOException e) {
//            try{
//                image.delete();
//                File file = new File(finalNamePath);
//                Files.deleteIfExists(file.toPath());
//                System.out.println(e.getMessage());
//                return  null;
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                return null;
//            }
//        }
//        catch(NoSuchElementException ex) {
//            image.delete();
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image format not supported!");
//        }
//    }
//}

package com.ftn.uns.ac.rs.smarthome.utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.ftn.uns.ac.rs.smarthome.config.MqttConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ImageCompressor {

    public static File compressImage(File image, float qualityOfOutputImage, String username) throws IOException {
        // Load properties
        Properties env = new Properties();
        env.load(MqttConfiguration.class.getClassLoader().getResourceAsStream("application.properties"));
        String tempFilePath = env.getProperty("tempfolder.path");

        // Check if the folder is writable
        File tempFolder = new File(tempFilePath);
        if (!tempFolder.exists()) {
            tempFolder.mkdirs();
        }

        if (!tempFolder.canWrite()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot write to the temporary folder.");
        }

        // File details
        String imageFile = image.getName();
        String extension = FilenameUtils.getExtension(imageFile);
        String finalNamePath = tempFilePath + "/" + username + "." + extension;
        File compressedImageFile = new File(finalNamePath);

        try {
            // Check if the image extension is supported
            if (!extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("jpeg") && !extension.equalsIgnoreCase("png")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported image format.");
            }

            // Compress the image
            InputStream inputStream = new FileInputStream(image);
            OutputStream outputStream = new FileOutputStream(compressedImageFile);

            BufferedImage bufferedImage = ImageIO.read(inputStream);
            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(extension);

            if (!imageWriters.hasNext()) {
                throw new NoSuchElementException("Image writer not found for the format: " + extension);
            }

            ImageWriter imageWriter = imageWriters.next();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            imageWriter.setOutput(imageOutputStream);

            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(qualityOfOutputImage);

            imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParam);

            // Close streams and dispose of writer
            inputStream.close();
            outputStream.close();
            imageOutputStream.close();
            imageWriter.dispose();

            // Optionally delete the original image after compressing
            image.delete();

            return compressedImageFile;

        } catch (IOException e) {
            // Handle errors related to I/O
            System.err.println("IOException occurred during image compression: " + e.getMessage());
            e.printStackTrace();
            try {
                // Cleanup in case of failure
                Files.deleteIfExists(compressedImageFile.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error compressing the image.");
        } catch (NoSuchElementException ex) {
            // Handle the case where no image writer is found for the format
            System.err.println("No such element: " + ex.getMessage());
            ex.printStackTrace();
            image.delete();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image format not supported!");
        } catch (Exception ex) {
            // General exception handling
            System.err.println("An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred while processing the image.");
        }
    }
}
