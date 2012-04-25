package inform.dist;

import java.nio.charset.Charset;

public interface Constants {

	final static String UTF8_ENCODING = "UTF-8";
	final static Charset UTF8_CHARSET = Charset.forName(UTF8_ENCODING);
	
	public static final String GIST_WORD_SEPARATOR = " ";
	public static final String GIST_CONTEXT_SEPARATOR = "\n";
	public static final String GIST_THIS_WORD_MARKER = "_";
	
	public static final short GIST_BINARY_CONTEXT_SEPARATOR = Short.MIN_VALUE + 1;
	
	public final static String FIELD_TEXT = "text";
	public final static String FIELD_ID = "id";
	
	public final static int BZIP2_BLOCK_SIZE = 900 * 1000;
	
	public final static int MATRIX_CELL_UNINITIALIZED = -1;

}
