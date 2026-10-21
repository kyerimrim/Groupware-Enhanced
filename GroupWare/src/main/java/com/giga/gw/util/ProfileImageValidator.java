package com.giga.gw.util;

import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.web.multipart.MultipartFile;

public final class ProfileImageValidator {
    public static final long MAX_BYTES = 5 * 1024 * 1024;

    private ProfileImageValidator() {}

    public static String encode(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("이미지는 5MB 이하로 선택해주세요.");
        }
        try (java.io.InputStream stream = file.getInputStream();
                ImageInputStream input = ImageIO.createImageInputStream(stream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("png")) throw invalidImage();
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > 16_000_000) {
                    throw new IllegalArgumentException("이미지 해상도는 1,600만 화소 이하로 선택해주세요.");
                }
                if (reader.read(0) == null) throw invalidImage();
                return "data:image/" + format + ";base64,"
                        + Base64.getEncoder().encodeToString(file.getBytes());
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw invalidImage();
        }
    }

    private static IllegalArgumentException invalidImage() {
        return new IllegalArgumentException("정상적인 JPG 또는 PNG 이미지를 선택해주세요.");
    }
}
