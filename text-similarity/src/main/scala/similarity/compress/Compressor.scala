package similarity.compress

import java.io.{OutputStream, InputStream}
import similarity.model.TextBlock

/**
 * specifies what a compressor should do
 */

abstract class Compressor {

  def compress(text: TextBlock, ostream: OutputStream) : Unit

  def uncompress(istream: InputStream): TextBlock

}