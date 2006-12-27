// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.dur.ac.uk/r.bordini
// http://www.inf.furb.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a list node as in prolog .(t1,.(t2,.(t3,.))).
 * 
 * Each nth-ListTerm has both a term and the next ListTerm.
 * The last ListTem is a emptyListTerm (term==null).
 * In lists with tail ([a|X]), next is the Tail (next=X).
 *
 * @author jomi
 */
public class ListTermImpl extends Structure implements ListTerm {
	
	private static final long serialVersionUID = 1L;

	private Term term;
	private Term next;

	static private Logger logger = Logger.getLogger(ListTermImpl.class.getName());
	
	public ListTermImpl() {
		super((String)null);
	}

    public static ListTerm parseList(String sList) {
        as2j parser = new as2j(new StringReader(sList));
        try {
            return (ListTerm)parser.list();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing list "+sList,e);
			return null;
        }
    }
	
	/** make a hard copy of the terms */
	public Object clone() {
		ListTermImpl t = new ListTermImpl();
		if (term != null) {
			t.term = (Term)this.term.clone();
		}
		if (next != null) {
			t.next = (Term)this.next.clone();
		}
		return t;
	}
	

    @Override
    public boolean equals(Object t) {
        if (t == null) return false;
        if (t == this) return true;

		if (t instanceof Term &&  ((Term)t).isVar() )  // unground var is not equals a list
		    return false;
		if (t instanceof ListTerm) {
			ListTerm tAsList = (ListTerm)t;
			if (term == null && tAsList.getTerm() != null) {
				return false;
			}
			if (term != null && !term.equals(tAsList.getTerm())) {
				return false;
			}
			if (next == null && tAsList.getNext() != null) {
				return false;
			}
			if (next != null) {
				return next.equals(tAsList.getNext());
			}
			return true;
		} 
	    return false;
	}
	
    @Override
    public int calcHashCode() {
        int code = 37;
        if (term != null) 
            code += term.hashCode();
        if (next != null)
            code += next.hashCode();
        return code;
    }
    
	public void setTerm(Term t) {
		term = t;
	}
	
	/** gets the term of this ListTerm */
	public Term getTerm() {
		return term;
	}
	
	public void setNext(Term l) {
		next = l;
	}
	
	public ListTerm getNext() {
		try {
			return (ListTerm)next;
		} catch (Exception e){}
		return null;
	}
	
	
	// for unifier compatibility
	public int getTermsSize() {
		if (isEmpty()) {
			return 0;
		} else {
			return 2; // term and next
		}
	}
	// for unifier compatibility
	public Term getTerm(int i) {
		if (i == 0) {
			return term;
		}
		if (i == 1) {
			return next;
		}
		return null;
	}
	
	/** return the this ListTerm elements (0=Term, 1=ListTerm) */
	public List<Term> getTerms() {
		List<Term> l = new ArrayList<Term>(2);
		if (term != null) {
			l.add(term);
		}
		if (next != null) {
			l.add(next);
		}
		return l;
	}
	
	public void addTerm(Term t) {
		logger.warning("Do not use addTerm in lists! Use add.");
	}

	public int size() {
		if (isEmpty()) {
			return 0;
		} else if (isTail()) {
			return 1;
		} else {
			return getNext().size() + 1;
		}
	}
	
    @Override
    public boolean isList() {
		return true;
	}

    public boolean isEmpty() {
		return term == null;
	}
	public boolean isEnd() {
		return isEmpty() || isTail();
	}

	public boolean isGround() {
	    Iterator i = iterator();
		while (i.hasNext()) {
			Term t = (Term)i.next();
			if (!t.isGround()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isTail() {
		return next != null && next.isVar();
	}
	
	/** returns this ListTerm's tail element in case the List has the Tail, otherwise, returns null */
	public VarTerm getTail() {
		if (isTail()) {
			return (VarTerm)next;
		} else if (next != null) {
			return getNext().getTail();
		} else {
			return null;
		}
	}
	
	/** set the tail of this list */
	public void setTail(VarTerm v) {
		if (getNext().isEmpty()) {
			next = v;
		} else {
			getNext().setTail(v);
		}
	}
	
	/** get the last ListTerm of this List */
	public ListTerm getLast() {
		if (isEnd()) {
			return this;
		} else if (next != null) {
			return getNext().getLast();
		} 
		return null; // !!! no last!!!!
	}
	
	
	/** 
	 * add a term in the end of the list
	 * @return the ListTerm where the term was added
	 */
	public ListTerm append(Term t) {
		if (isEmpty()) {
			term = t;
			next = new ListTermImpl();
			return this;
		} else if (isTail()) {
			// What to do?
			return null;
		} else {
			return getNext().append(t);
		}
	}

	
	/** 
     * Add a list in the end of this list.
	 * This method do not clone <i>lt</i>.
	 * @return the last ListTerm of the new list
	 */
	public ListTerm concat(ListTerm lt) {
		if (isEmpty()) {
			term = lt.getTerm();
			next = (Term)lt.getNext();
		} else if (((ListTerm)next).isEmpty() ) {
			next = (Term)lt;
		} else {
			((ListTerm)next).concat(lt);
		}
		return lt.getLast();
	}

	
	/** returns an iterator where each element is a ListTerm */
	public Iterator<ListTerm> listTermIterator() {
		final ListTermImpl lt = this;
		return new Iterator<ListTerm>() {
			ListTerm nextLT  = lt;
			ListTerm current = null;
			public boolean hasNext() {
				return nextLT != null && !nextLT.isEmpty() && nextLT.isList(); 
			}
			public ListTerm next() {
				current = nextLT;
				nextLT = nextLT.getNext();
				return current;
			}
			public void remove() {
				if (current != null) {
					if (nextLT != null) {
						current.setTerm(nextLT.getTerm());
						current.setNext((Term)nextLT.getNext());
						nextLT = current;
					}
				}
			}
		};
	}

	/** returns an iterator where each element is a Term of this list */
	public Iterator<Term> iterator() {
		final Iterator<ListTerm> i = this.listTermIterator();
		return new Iterator<Term>() {
			public boolean hasNext() {
				return i.hasNext();
			}
			public Term next() {
				return i.next().getTerm();
			}
			public void remove() {
				i.remove();
			}
		};
	}
	
	
	/** 
	 * Returns this ListTerm as a Java List. 
	 * Note: the list Tail is considered just as the last element of the list!
	 */
    public List<Term> getAsList() {
        List<Term> l = new ArrayList<Term>();
        Iterator<Term> i = iterator();
        while (i.hasNext()) {
            l.add( i.next() );
        }
        return l;
    }

	
	public String toString() {
        StringBuilder s = new StringBuilder("[");
		Iterator i = listTermIterator();
		while (i.hasNext()) {
			ListTerm lt = (ListTerm)i.next();
			//System.out.println(s+"/cur="+lt.getTerm()+"/"+lt.getNext()+"/"+lt.getClass());
			s.append( lt.getTerm() );
			if (lt.isTail()) {
				s.append("|");
				s.append(lt.getNext());
			} else if (i.hasNext()) {
				s.append(",");
			}
		}
		s.append("]");
		return s.toString();
	}

	//
	// Java List interface methods
	//
	
	public void add(int index, Term o) {
        if (index == 0) {
            ListTermImpl n = new ListTermImpl();
            n.term = this.term;
            n.next = this.next;
            this.term = o;
            this.next = n;
        } else if (index > 0 && getNext() != null) {
            getNext().add(index-1,o);
        }
	}
	public boolean add(Term o) {
		return append((Term)o) != null;
	}
	public boolean addAll(Collection c) {
		ListTerm lt = this; // where to add
		Iterator i = c.iterator();
		while (i.hasNext()) {
			lt = lt.append((Term)i.next());
		}
		return true;
	}
	public boolean addAll(int index, Collection c) {
		Iterator i = c.iterator();
		int p = index;
		while (i.hasNext()) {
			add(p, (Term)i.next()); 
			p++;
		}
		return true;
	}
	public void clear() {
		term = null;
		next = null;
	}

	public boolean contains(Object o) {
		if (term != null && term.equals(o)) {
			return true;
		} else if (getNext() != null) {
			return getNext().contains(o);
		}
		return false;
	}

	public boolean containsAll(Collection c) {
		boolean r = true;
		Iterator i = c.iterator();
		while (i.hasNext() && r) {
			r = r && contains((Term)i.next()); 
		}
		return r;
	}

	public Term get(int index) {
		if (index == 0) {
			return this.term;
		} else if (getNext() != null) {
			return getNext().get(index-1);
		}
		return null;
	}

	public int indexOf(Object o) {
		if (this.term.equals(o)) {
			return 0;
		} else if (getNext() != null) {
			int n = getNext().indexOf(o);
			if (n >= 0) {
				return n+1;
			}
		}
		return -1;
	}
	public int lastIndexOf(Object arg0) {
		return getAsList().lastIndexOf(arg0);
	}

	public ListIterator<Term> listIterator() {
		return listIterator(0);
	}
	public ListIterator<Term> listIterator(final int startIndex) {
        final ListTermImpl list = this;
        return new ListIterator<Term>() {
            int pos = startIndex;
            int last = -1;
            int size = size();

            public void add(Term o) {
                list.add(last,o);
            }
            public boolean hasNext() {
                return pos < size;
            }
            public boolean hasPrevious() {
                return pos > startIndex;
            }
            public Term next() {
                last = pos;
                pos++;
                return get(last);
            }
            public int nextIndex() {
                return pos+1;
            }
            public Term previous() {
                last = pos;
                pos--;
                return get(last);
            }
            public int previousIndex() {
                return pos-1;
            }
            public void remove() {
                list.remove(last);
            }
            public void set(Term o) {
                remove();
                add(o);
            }            
        };
	}

	public Term remove(int index) {
		if (index == 0) {
			Term bt = this.term;
			if (getNext() != null) {
				this.term = getNext().getTerm();
				this.next = (Term)getNext().getNext();
			} else {
				clear();
			}
			return bt;
		} else if (getNext() != null) {
			return getNext().remove(index-1);
		}
		return null;
	}

	public boolean remove(Object o) {
		if (term != null && term.equals(o)) {
			if (getNext() != null) {
				this.term = getNext().getTerm();
				this.next = (Term)getNext().getNext();
			} else {
				clear();
			}
			return true;
		} else if (getNext() != null) {
			return getNext().remove(o);
		}
		return false;
	}

	public boolean removeAll(Collection c) {
		boolean r = true;
		Iterator i = c.iterator();
		while (i.hasNext() && r) {
			r = r && remove(i.next()); 
		}
		return r;
	}

	public boolean retainAll(Collection c) {
		boolean r = true;
		Iterator i = iterator();
		while (i.hasNext()) {
			Term t = (Term)i.next();
			if (!c.contains(t)) {
				r = r && remove(t);
			}
		}
		return r;
	}

	public Term set(int index, Term t) {
		if (index == 0) {
			this.term = (Term)t;
			return t;
		} else if (getNext() != null) {
			return getNext().set(index-1, t);
		}
		return null;
	}

	public List<Term> subList(int arg0, int arg1) {
		return getAsList().subList(arg0, arg1);
	}

	public Object[] toArray() {
		return getAsList().toArray();
	}

    @SuppressWarnings("unchecked")
	public Object[] toArray(Object[] arg0) {
		return getAsList().toArray(arg0);
	}
    
    public Element getAsDOM(Document document) {
        Element u = (Element) document.createElement("list-term");
        for (Term t: this) {
            u.appendChild(t.getAsDOM(document));
        }
        return u;
    }

}
