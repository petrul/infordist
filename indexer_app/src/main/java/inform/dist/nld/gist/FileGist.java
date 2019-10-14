package inform.dist.nld.gist;


import inform.dist.Constants;
import inform.dist.nld.compressor.Bzip2Compressor;
import inform.dist.nld.compressor.Compressor;
import inform.dist.nld.compressor.Compressors;
import inform.dist.nld.compressor.GzipCompressor;
import inform.dist.nld.gist.combining.GistCombiningPolicy;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Because {@link Gist#combine(Gist, GistCombiningPolicy.Policy, OutputStream)} combine is the CPU bottleneck of
 * the NCD computation, the code must be clean and optimized.
 *
 * A {@link FileGist} is for now a Gist stored as a file identifyed by an {@link URL}.
 *
 * The contents should be lazily loaded and stored as a string. The {@link Gist#combine(Gist, GistCombiningPolicy.Policy, OutputStream)}
 * should not require a lot of memory.
 *
 * @author petru
 *
 */
public class FileGist extends AbstractGist {

    // if this string was retrieved from a compressed format (a .bz2 file for ex), this attribute is the compressor which was used to
    // decompress it.
    Compressor initialCompressor = null;
    URL resource;
    InputStream inputStream;
    int nrLines = 0;


    /**
     * use this constructor when the string is stored already compressed
     */
    public FileGist(InputStream inputStream, Compressor compressor) {
        this.initFromInputStream(inputStream, compressor);
    }

    protected void initFromInputStream(InputStream inputStream, Compressor compressor) {
        this.inputStream = inputStream;
        this.initialCompressor = compressor;
//        byte[] bytes = compressor.uncompress(new BufferedInputStream(inputStream));
//        String s = new String(bytes, StandardCharsets.UTF_8);
//        String[] strings = s.split(Constants.GIST_CONTEXT_SEPARATOR);
//        this.gist = Arrays.asList(strings);

    }

    protected void initFromUrl(URL url, Compressor compressor) {
        this.resource = url;
        final String s = url.toExternalForm();
        String extension = s.substring(s.lastIndexOf("."));
        if (extension.startsWith("."))
            extension = extension.substring(1);

        if (compressor == null)
            this.initialCompressor = Compressors.REGISTRY.get(extension);
        else
            this.initialCompressor = compressor;

        try {
            this.initFromInputStream(url.openStream(), this.initialCompressor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileGist(URL url, Compressor compressor) {
        initFromUrl(url, compressor);
    }

    public FileGist(URL url) {
        this(url, null);
    }

    public FileGist(File file) {
        try {
            initFromUrl(file.toURI().toURL(), null);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public int nrLines() {
        return this.nrLines;
    }



//    public void writeToSorted(OutputStream os) {
//        int len = gist.size();
//        for (int i = 0; i < len; i++) {
//            try {
//                String s = gist.get(i);
//                String[] words = s.split(" ");
//                Arrays.sort(words);
//                int words_length = words.length;
//                for (int j = 0; j < words_length; j++) {
//                    String w = words[j];
//                    os.write(w.getBytes(Constants.UTF8_ENCODING));
//                    if (j != words_length - 1)
//                        os.write(' ');
//                }
//                if (i != len - 1)
//                    os.write(Constants.GIST_CONTEXT_SEPARATOR.getBytes());
//            } catch (UnsupportedEncodingException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    public void writeTo(OutputStream os) {
        try {
            IOUtils.copy(this.openStreamForReading(), os);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        int len = gist.size();
//        for (int i = 0; i < len; i++) {
//            try {
//                String s = gist.get(i);
//                os.write(s.getBytes(Constants.UTF8_ENCODING));
//
//                if (i != len - 1)
//                    os.write(Constants.GIST_CONTEXT_SEPARATOR.getBytes());
//            } catch (UnsupportedEncodingException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    public List<String> getStringList() {

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(this.openStreamForReading(), baos);
            String s = new String(baos.toByteArray());
            return Arrays.asList(s.split("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public InputStream openStreamForReading() {
            try {
                InputStream res;
                if (this.resource != null) {
                    res = this.resource.openStream();
                } else
                    res = this.inputStream; // hoping the inputStream is still a virgin

                if (this.initialCompressor == null)
                    return res;
                else {
                    if (this.initialCompressor instanceof Bzip2Compressor)
                        res = new BZip2CompressorInputStream(res);
                    else if (this.initialCompressor instanceof GzipCompressor)
                        res = new GZIPInputStream(res);

                }
                return res;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

    }

    @Override
    public OutputStream openStreamForWriting() {
        throw new RuntimeException("undefined");
    }

    @Override
    public long getSizeInBytes() {
         {
            try {
                if (this.resource.toExternalForm().startsWith("file:/"))
                    return new File(this.resource.toURI()).length();
                else if (this.resource.toExternalForm().startsWith("http://")) {
                    return this.resource.openConnection().getContentLength();
                } else
                    throw new RuntimeException(String.format("don't know how to get size of %s", this.resource));
             } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Compressor getInitialCompressor() {
        return initialCompressor;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (String s : this) {
            sb.append(s).append("\n");
//			if (counter++ > 5) {
//				sb.append("[...]");
//				break;
//			}
        }
        return sb.toString();
    }

//    @Override
//    public boolean equals(Object obj) {
//        StringListGist slg = (StringListGist) obj;
//        return this.gist.equals(slg.gist);
//    }


    @Override
    public Iterator<String> iterator() {
        return new FileGistIterator(this);
    }

    class FileGistIterator implements Iterator<String> {

        LineNumberReader lineReader;
        String nextLine = null;

        public FileGistIterator(FileGist fileGist) {
            this.lineReader = new LineNumberReader(new InputStreamReader(fileGist.openStreamForReading()));
        }

        @Override
        public boolean hasNext() {
            if (this.nextLine == null) {
                try {
                    this.nextLine = this.lineReader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return  this.nextLine != null;
        }

        @Override
        public String next() {
            if (this.nextLine == null) {
                try {
                    this.nextLine = this.lineReader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String res = this.nextLine;
            this.nextLine = null;
            return res;
        }
    }
}
