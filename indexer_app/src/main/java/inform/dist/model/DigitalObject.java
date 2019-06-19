package inform.dist.model;

import java.io.InputStream;

/**
 * a "link" to an object (file, from database etc). that potentially need to have its NCD computed.
 * it should be lightweight. it should not contain the object but rather know how to get the data.
 */
abstract class DigitalObject {

    long id;

    abstract InputStream openStream();

}
