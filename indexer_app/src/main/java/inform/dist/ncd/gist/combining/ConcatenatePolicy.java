package inform.dist.ncd.gist.combining;

import inform.dist.ncd.gist.Gist;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConcatenatePolicy extends GistCombiningPolicy {

    @Override
    public void combine(Gist g1, Gist g2, OutputStream outputStream) {

        try {
            final InputStream inputStream1 = g1.openStreamForReading();
            IOUtils.copy(inputStream1, outputStream);
            outputStream.write('\n');
            final InputStream inputStream2 = g2.openStreamForReading();
            IOUtils.copy(inputStream2, outputStream);
            outputStream.flush();

            inputStream1.close();
            inputStream2.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
