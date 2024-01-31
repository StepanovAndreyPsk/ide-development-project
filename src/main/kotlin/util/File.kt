package util

import util.rope.Rope
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

val HomeFolder: File get() = java.io.File(System.getProperty("user.home")).toProjectFile()
interface File {
    val name: String
    val isDirectory: Boolean
    val children: List<File>
    val hasChildren: Boolean
    val absolutePath: String
    val extension: String?

    fun read(): Rope<LineMetrics>
}

fun java.io.File.toProjectFile(): File = object : File {
    override val name: String
        get() = this@toProjectFile.name

    override val isDirectory: Boolean
        get() = this@toProjectFile.isDirectory

    override val children: List<File>
        get() = this@toProjectFile
            .listFiles { _, name -> !name.startsWith(".") }
            .orEmpty()
            .map { it.toProjectFile() }

    private val numberOfFiles
        get() = listFiles()?.size ?: 0

    override val hasChildren: Boolean
        get() = isDirectory && numberOfFiles > 0

    override val absolutePath: String
        get() = this@toProjectFile.absolutePath

    override val extension: String?
        get() = this@toProjectFile.extension.ifEmpty { null }


    override fun read(): Rope<LineMetrics> {
        var byteBufferSize: Long
        val byteBuffer = RandomAccessFile(this@toProjectFile, "r").use { file ->
            byteBufferSize = file.length()
            file.channel.map(FileChannel.MapMode.READ_ONLY, 0, byteBufferSize)
        }

        val string = StandardCharsets.UTF_8.decode(byteBuffer).toString()
        return Rope(string, LineMetricsCalculator())
    }
}
