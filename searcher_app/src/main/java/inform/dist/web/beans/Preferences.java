package inform.dist.web.beans;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;

@Log4j2
public class Preferences extends HashMap<String, Object> {
	public Preferences() {
		this.put("term-rows", 20);
		log.info("initialized " + this);
	}
	private static final long serialVersionUID = 1L;

}
