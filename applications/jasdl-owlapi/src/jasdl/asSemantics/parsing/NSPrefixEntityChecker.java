package jasdl.asSemantics.parsing;
import jasdl.asSemantics.JasdlAgent;
import jasdl.bridge.alias.Alias;
import jasdl.bridge.alias.AliasFactory;
import jason.asSyntax.Atom;

import org.semanticweb.owl.expression.OWLEntityChecker;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;

/**
 * For parsing class-expressions defined with entities in the alias label:functor format.
 * E.g. "(travel:hotel and travel:hasActivity some travel:museums) and owl:thing"
 * @author Tom Klapiscak
 *
 */
public class NSPrefixEntityChecker implements OWLEntityChecker{
		
		private JasdlAgent agent;

		public NSPrefixEntityChecker(JasdlAgent agent){
			this.agent = agent;
		}

		public OWLClass getOWLClass(String name) {			
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLClass()){
				return entity.asOWLClass();
			}else{
				return null;
			}
		}

		public OWLDataProperty getOWLDataProperty(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLDataProperty()){
				return entity.asOWLDataProperty();
			}else{
				return null;
			}
		}

		public OWLDataType getOWLDataType(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLDataType()){
				return entity.asOWLDataType();
			}else{
				return null;
			}
		}

		public OWLIndividual getOWLIndividual(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				// TODO: instantiate individuals?				
				return null;
			}
			if(entity.isOWLIndividual()){
				return entity.asOWLIndividual();
			}else{
				return null;
			}
		}

		public OWLObjectProperty getOWLObjectProperty(String name) {
			OWLEntity entity = convert(name);
			if(entity == null){
				return null;
			}
			if(entity.isOWLObjectProperty()){
				return entity.asOWLObjectProperty();
			}else{
				return null;
			}
		}
		
		private OWLEntity convert(String name){
			String[] tokens = name.split(":");
			try {
				Atom functor = new Atom(tokens[1]);
				Atom label = new Atom(tokens[0]);
				Alias alias = AliasFactory.INSTANCE.create(functor, label);
				return (OWLEntity)agent.getAliasManager().getRight(alias); // guaranteed to be an entity? Not for anonymous classes!
			} catch (Exception e) {
				return null;
			}			
		}
		
		
	}