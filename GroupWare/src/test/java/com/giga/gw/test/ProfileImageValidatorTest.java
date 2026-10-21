package com.giga.gw.test;

import static org.junit.Assert.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.giga.gw.util.ProfileImageValidator;

public class ProfileImageValidatorTest {
    @Test
    public void optionalImageIsAllowed() throws Exception {
        assertNull(ProfileImageValidator.encode(null));
        assertNull(ProfileImageValidator.encode(new MockMultipartFile("profileImage", new byte[0])));
    }

    @Test
    public void acceptsPngAndJpegBasedOnContents() throws Exception {
        for (String format : new String[] {"png", "jpeg"}) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), format, bytes);
            String encoded = ProfileImageValidator.encode(
                    new MockMultipartFile("profileImage", "photo", "application/octet-stream", bytes.toByteArray()));
            assertTrue(encoded.startsWith("data:image/" + format + ";base64,"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDisguisedText() throws Exception {
        ProfileImageValidator.encode(new MockMultipartFile(
                "profileImage", "photo.png", "image/png", "<script>alert(1)</script>".getBytes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOversizedFile() throws Exception {
        ProfileImageValidator.encode(new MockMultipartFile(
                "profileImage", new byte[(int) ProfileImageValidator.MAX_BYTES + 1]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsGif() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "gif", bytes);
        ProfileImageValidator.encode(new MockMultipartFile("profileImage", bytes.toByteArray()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExcessiveDimensionsBeforeDecoding() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4001, 4000, BufferedImage.TYPE_BYTE_GRAY), "png", bytes);
        ProfileImageValidator.encode(new MockMultipartFile("profileImage", bytes.toByteArray()));
    }
}
