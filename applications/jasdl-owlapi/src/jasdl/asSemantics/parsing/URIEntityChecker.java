package jasdl.asSemantics.parsing;

import jasdl.asSemantics.JasdlAgent;

import java.net.URI;

import org.semanticweb.owl.expression.OWLEntityChecker;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;

/**
 * For parsing class-expressions where entities are defined by their fully-qualified URIs.
 * E.g. "http://www.dur.ac.uk.t.g.klapiscak/travel.owl#Hotel and http://www.dur.ac.uk.t.g.klapiscak/travel.owl#Accommodation".
 * @author tom
 *
 */
public class URIEntityChecker implements OWLEntityChecker{

	private JasdlAgent agent;

	public URIEntityChecker(JasdlAgent agent){
		this.agent = agent;
	}

	public OWLClass getOWLClass(String uri) {			
		OWLEntity entity = convert(uri);
		if(entity == null){
			return null;
		}
		if(entity.isOWLClass()){
			return entity.asOWLClass();
		}else{
			return null;
		}
	}

	public OWLDataProperty getOWLDataProperty(String uri) {
		OWLEntity entity = convert(uri);
		if(entity == null){
			return null;
		}
		if(entity.isOWLDataProperty()){
			return entity.asOWLDataProperty();
		}else{
			return null;
		}
	}

	public OWLDataType getOWLDataType(String uri) {
		OWLEntity entity = convert(uri);
		if(entity == null){
			return null;
		}
		if(entity.isOWLDataType()){
			return entity.asOWLDataType();
		}else{
			return null;
		}
	}

	public OWLIndividual getOWLIndividual(String uri) {
		OWLEntity entity = convert(uri);
		if(entity == null){
			return null;
		}
		if(entity.isOWLIndividual()){
			return entity.asOWLIndividual();
		}else{
			return null;
		}
	}

	public OWLObjectProperty getOWLObjectProperty(String uri) {
		OWLEntity entity = convert(uri);
		if(entity == null){
			return null;
		}
		if(entity.isOWLObjectProperty()){
			return entity.asOWLObjectProperty();
		}else{
			return null;
		}
	}

	private OWLEntity convert(String _uri){
		try {
			URI uri = new URI(_uri);
			return agent.toEntity(uri);
		} catch (Exception e) {
			return null;
		}			
	}


}