package com.triptune.domain.profile;

import com.triptune.domain.BaseTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileImageTest extends BaseTest {

    protected byte[] createTestImage(String extension) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, extension, baos);

        return baos.toByteArray();
    }
}
