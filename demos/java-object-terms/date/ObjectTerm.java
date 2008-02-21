package date;

import jason.asSyntax.DefaultTerm;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ObjectTerm extends DefaultTerm {

    private final Object o;
    
    public ObjectTerm(Object o) {
        this.o = o;
    }
    
    public Object getObject() {
        return o;
    }
    
    @Override
    protected int calcHashCode() {
        return o.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.o.equals(o);
    }
    
    @Override
    public Object clone() {
        return this; // TODO: discover a way to clone o
    }

    @Override
    public String toString() {
        return o.toString();
    }
    
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("object-term");
        u.appendChild(document.createTextNode(o.toString()));
        return u;
    }
}
