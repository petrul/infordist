package inform.dist.ncd.gist.combining;

import inform.dist.ncd.gist.Gist;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;

public class ConcatenatePolicy extends GistCombiningPolicy {

    @Override
    public void combine(Gist g1, Gist g2, OutputStream outputStream) {

        try {
            IOUtils.copy(g1.openStreamForReading(), outputStream);
            outputStream.write('\n');
            IOUtils.copy(g2.openStreamForReading(), outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
