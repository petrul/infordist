package inform.dist.model;

import java.util.Iterator;

interface ObjectStore extends Iterable<DigitalObject> {

    @Override
    Iterator<DigitalObject> iterator();

    DigitalObject get(long id);

}
