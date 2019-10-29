package inform.dist.ncd.gist;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;

import inform.dist.ncd.compressor.Bzip2Compressor;
import inform.dist.ncd.compressor.Compressor;
import inform.dist.ncd.compressor.Compressors;
import inform.dist.ncd.compressor.GzipCompressor;
import inform.dist.ncd.gist.combining.GistCombiningPolicy;

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
    }

    protected void initFromUrl(URL url, Compressor compressor) {
        this.resource = url;
        final String s = url.toExternalForm();

        if (compressor == null)
            this.initialCompressor = Compressors.getCompressorForFilename(s);
        else
            this.initialCompressor = compressor;

        try {
            final InputStream inputStream = url.openStream();
            this.initFromInputStream(inputStream, this.initialCompressor);
            inputStream.close();
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
            final InputStream inputStream = this.openStreamForReading();
            IOUtils.copy(inputStream, os);
            os.flush();
            inputStream.close();
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
            final InputStream inputStream = this.openStreamForReading();
            IOUtils.copy(inputStream, baos);
            inputStream.close();
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

//    @Override
//    public OutputStream openStreamForWriting() {
//        throw new RuntimeException("undefined");
//    }

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

    /**
     * uses extension to infer compressor
     */
    public long computeComplexity() {
        try {
            String filename = this.resource.toURI().getPath();
            Compressor compressor = Compressors.getCompressorForFilename(filename);
            return super.computeComplexity(compressor);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

//    void truncate(int maxLimit, OutputStream os) {
//        int counter = 0;
//
//        if (maxLimit >= this.getSizeInBytes()) {
//            try {
//                IOUtils.copy(this.openStreamForReading(), os);
//                os.flush();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            return;
//        }
//
//        LineNumberReader reader = new LineNumberReader(new BufferedReader(new InputStreamReader(this.openStreamForReading())));
//        while (counter < maxLimit) {
//            try {
//                String crtline = reader.readLine();
//                if (crtline == null)
//                    break;
//
//                counter += crtline.length();
//                if (counter > maxLimit)
//                    crtline = crtline.substring(0, counter - maxLimit);
//
//                os.write(crtline.getBytes(StandardCharsets.UTF_8));
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
//    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (String s : this) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }


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

            final boolean retValue = this.nextLine != null;

            return retValue;
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
