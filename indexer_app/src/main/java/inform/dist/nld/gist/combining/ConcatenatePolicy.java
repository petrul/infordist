package inform.dist.nld.gist.combining;

import inform.dist.nld.gist.Gist;
import inform.dist.nld.gist.StringGist;
import inform.dist.nld.gist.StringListGist;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
