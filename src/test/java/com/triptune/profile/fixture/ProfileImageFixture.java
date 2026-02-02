package com.triptune.profile.fixture;


import com.triptune.profile.entity.ProfileImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileImageFixture {

    public static ProfileImage createProfileImage(String fileName){
        return ProfileImage.createProfileImage(
                "/test/" + fileName + ".jpg",
                "/img/test/" + fileName + ".jpg",
                fileName + "_original.jpg",
                fileName + ".jpg",
                "jpg",
                20
        );
    }

    public static byte[] createByteTypeImage(String extension) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, extension, baos);

        return baos.toByteArray();
    }
}
