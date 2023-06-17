package inform.dist.web;

public class Util {

    public static String replaceTilde(String str) {
        return str.replaceFirst("^~", System.getProperty("user.home"));
    }

}
