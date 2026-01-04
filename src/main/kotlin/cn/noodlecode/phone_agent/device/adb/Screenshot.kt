package cn.noodlecode.phone_agent.device.adb

import cn.noodlecode.phone_agent.device.ScreenshotInfo
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Base64
import java.util.UUID
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.MemoryCacheImageOutputStream


private const val screencapPath = "/sdcard/tmp.png"

class Screenshot(deviceId: String? = null) : AdbShell(deviceId) {

    fun getScreenshot(): ScreenshotInfo? {
        val screencapResult = runShellCommand("screencap", "-p", screencapPath)
        if (!screencapResult.isBlank()) return null

        val tempPath = Files.createTempFile("screenshot_${UUID.randomUUID()}", ".png").toString()
        runCommand("pull", screencapPath, tempPath)

        val img: BufferedImage = try {
            ImageIO.read(File(tempPath))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val width = img.width
        val height = img.height

        val baos = ByteArrayOutputStream()

        // 1. 转换图片格式 (PNG 可能带透明通道，直接转 JPG 会导致颜色异常，需转为 RGB)
        val newImg = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = newImg.createGraphics()
        // 填充白色背景（防止透明区域变黑）
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)
        graphics.drawImage(img, 0, 0, null)
        graphics.dispose()

        // 2. 获取 JPEG 写入器
        val writers = ImageIO.getImageWritersByFormatName("jpg")
        if (!writers.hasNext()) throw IllegalStateException("No JPEG writer found")
        val writer = writers.next()

        // 3. 设置压缩参数
        val param = writer.defaultWriteParam
        param.compressionMode = ImageWriteParam.MODE_EXPLICIT

        // 0.0f (最大压缩，质量最差) -> 1.0f (最小压缩，质量最好)
        // 推荐 0.7f - 0.8f，通常能保持很好的清晰度且体积很小
        param.compressionQuality = 0.7f

        // 4. 写入流
        val ios = ImageIO.createImageOutputStream(baos)
        writer.output = ios
        writer.write(null, IIOImage(newImg, null, null), param)

        // 5. 资源释放
        writer.dispose()
        ios.close() // 重要：ImageOutputStream 需要关闭

        // --- 重点修改结束 ---

        val base64Data = Base64.getEncoder().encodeToString(baos.toByteArray())

        // 计算压缩后的大小供调试
        val compressedSizeMB = baos.size().toDouble() / (1024 * 1024)
        println("截图压缩后大小: ${String.format("%.2f", compressedSizeMB)} MB")

        // 清理临时文件
        File(tempPath).delete()

        return ScreenshotInfo(base64Data, width, height)
    }
}
