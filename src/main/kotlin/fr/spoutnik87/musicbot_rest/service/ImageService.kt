package fr.spoutnik87.musicbot_rest.service

import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class ImageService {

    /**
     * Resize image and return it as a png
     */
    fun resize(image: ByteArray, width: Int, height: Int): ByteArray {
        val img = ImageIO.read(ByteArrayInputStream(image))
        val tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = resized.createGraphics()
        graphics.drawImage(tmp, 0, 0, null)
        graphics.dispose()
        val baos = ByteArrayOutputStream()
        ImageIO.write(resized, "png", baos)
        baos.flush()
        val result = baos.toByteArray()
        baos.close()
        return result
    }
}