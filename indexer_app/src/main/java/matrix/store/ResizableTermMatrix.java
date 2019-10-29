package matrix.store;

import java.io.File;

/**
 * this is a term matrix that automatically expands to fit new terms while they are being added.
 *
 */
public class ResizableTermMatrix implements TermMatrix {

    TermMatrixRW inner;

    public ResizableTermMatrix(int initialSize, File dir) {
        this(initialSize, dir, 5, "");
    }

    public ResizableTermMatrix(int initialSize, File dir, int cacheSize,String comment) {
        String[] terms = new String[initialSize];
        this.inner = new TermMatrixRW(terms, null, dir, cacheSize, comment);
    }

    @Override
    public void setCombinedComplexity(String term1, String term2, int value) {
        throw new RuntimeException("undefined");
    }

    @Override
    public void setComplexity(String term, int value) {
        throw new RuntimeException("undefined");
    }

    @Override
    public int getComplexity(String term) {
        throw new RuntimeException("undefined");
    }

    @Override
    public int getCombinedComplexity(String term1, String term2) {
        throw new RuntimeException("undefined");
    }

    @Override
    public int[] getCombinedComplexityRow(String term) {
        throw new RuntimeException("undefined");
    }

    @Override
    public int getTermIndex(String term) {
        throw new RuntimeException("undefined");
    }

    @Override
    public String[] getTerms() {
        throw new RuntimeException("undefined");
    }

    @Override
    public String getTerm(int index) {
        throw new RuntimeException("undefined");
    }

    @Override
    public long getVariable(String name) {
        throw new RuntimeException("undefined");
    }

    @Override
    public void close() {
        throw new RuntimeException("undefined");
    }
}
