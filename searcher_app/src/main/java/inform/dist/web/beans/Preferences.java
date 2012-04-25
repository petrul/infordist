package inform.dist.web.beans;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class Preferences extends HashMap<String, Object> {
	public Preferences() {
		this.put("term-rows", 20);
		LOG.info("initialized " + this);
	}
	private static final long serialVersionUID = 1L;

	
	static Logger LOG = Logger.getLogger(Preferences.class);
}
