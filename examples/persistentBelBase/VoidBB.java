import jason.asSemantics.*;
import jason.asSyntax.*;
import java.util.*;
import org.w3c.dom.*;


/**
  * Void Belief Base: store nothing!
  */
public class VoidBB implements jason.bb.BeliefBase {

    public void init(Agent ag, String[] args) {
    }
    public void stop() {
    }
    public int size() {
        return 0;
    }

    public Iterator<Literal> getPercepts() {
        return new ArrayList<Literal>().iterator();
    }

    public boolean add(Literal l) {
        return true;
    }
    
    public Literal contains(Literal l) {
        return l;
    }

    public Iterator<Literal> iterator() {
        return new ArrayList<Literal>().iterator();
    }
    
    public Iterator<Literal> getAll() {
        return iterator();
    }

    public boolean remove(Literal l) {
        return true;
    }

    public boolean abolish(PredicateIndicator pi) {
        return true;
    }

    public Iterator<Literal> getRelevant(Literal l) {
        return new ArrayList<Literal>().iterator();
    }

    public Iterator<Unifier> logCons(final Literal l, final Unifier un) {
        return new ArrayList<Unifier>().iterator();
    }

    public Element getAsDOM(Document document) {
        return null;
    }
}
