package jasdl.util.owlapi.rendering;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.util.ShortFormProvider;

public class URIOWLObjectRenderer implements ShortFormProvider{
	public void dispose() {			
	}
	public String getShortForm(OWLEntity entity) {
		return entity.getURI().toString();
	}
}
