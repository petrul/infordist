package similarity.compress

import java.io.{OutputStream, InputStream}

object Util {

    def copyStream(is: InputStream, os: OutputStream ) : Unit= {
      val crt = is.read;
      if (crt != -1) {
        os.write(crt)
        copyStream(is, os)
      }
    }
}