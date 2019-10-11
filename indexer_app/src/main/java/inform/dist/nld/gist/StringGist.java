package inform.dist.nld.gist;

import inform.dist.Constants;
import inform.dist.nld.gist.combining.GistCombiningPolicy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

@Deprecated
public abstract class StringGist implements Gist {

	protected String gist;
	
	public StringGist(String s) {
		this.gist = s;
	}
	
	@Override
	public int size() {
		return gist.length();
	}

	@Override
	public void writeTo(OutputStream os) {
		try {
			os.write(gist.getBytes(Constants.UTF8_ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		};
	}

	@Override
	public Gist combine(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy) {
		return GistCombiningPolicy.REGISTRY.get(combiningPolicy).combine(this,anotherGist);

//		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//		anotherGist.writeTo(bytes);
//		try {
//			this.gist = this.gist + Constants.GIST_CONTEXT_SEPARATOR + new String(bytes.toByteArray(), Constants.UTF8_ENCODING);
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e);
//		}
//		throw new RuntimeException("interweaveBlockSize unimplemented");
	}

	@Override
	public Gist clone() {
		throw new RuntimeException();
		//return new StringGist(this.gist) ;
	}

	@Override
	public long getSizeInBytes() {
		return this.gist.length();
	}
}
