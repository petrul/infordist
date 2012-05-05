package similarity.compress

import org.apache.commons.compress.compressors.bzip2.{BZip2CompressorInputStream, BZip2CompressorOutputStream}
import java.io._
import similarity.model.TextBlock

class Bzip2Compressor extends Compressor {

  override def compress(text: TextBlock, ostream: OutputStream ) = {

      val bzos = new BZip2CompressorOutputStream(ostream)
      bzos.write(text.text.getBytes)
      bzos.close

  }

  override def uncompress(inputStream: InputStream) : TextBlock = {

    val bytes = new ByteArrayOutputStream
    val bzip2 = new BZip2CompressorInputStream(new BufferedInputStream(inputStream))


    Util.copyStream(inputStream, bytes)

    TextBlock(new String(bytes.toByteArray))
  }
}