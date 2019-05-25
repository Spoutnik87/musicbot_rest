package fr.spoutnik87.musicbot_rest.service

import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.random.Random

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
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(resized, "png", outputStream)
        outputStream.flush()
        val result = outputStream.toByteArray()
        outputStream.close()
        return result
    }

    /**
     * Generate a random image based on UUID.
     */
    fun generateRandomImage(uuid: String): ByteArray {
        val image = BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        val seed = uuid.toByteArray().map { it.toLong() }.reduce { acc, l -> acc + l }
        val colors = (0..5).map { Random(seed + it).nextInt(0, 16777217) }
        for (i in 0..9) {
            for (j in 0..9) {
                g2d.color = Color(colors.random())
                g2d.fillRect(40 * i, 40 * j, 40, 40)
            }
        }
        g2d.dispose()
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        val result = outputStream.toByteArray()
        outputStream.close()
        return result
    }
}