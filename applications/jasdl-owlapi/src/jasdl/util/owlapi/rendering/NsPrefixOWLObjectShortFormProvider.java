/* 
 *  Copyright (C) 2008 Thomas Klapiscak (t.g.klapiscak@durham.ac.uk)
 *  
 *  This file is part of JASDL.
 *
 *  JASDL is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JASDL is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the Lesser GNU General Public License
 *  along with JASDL.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package jasdl.util.owlapi.rendering;

import jasdl.bridge.JASDLOntologyManager;
import jasdl.bridge.mapping.aliasing.Alias;
import jasdl.util.exception.JASDLException;

import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.util.ShortFormProvider;

/**
 * Renders entity in JASDL's ns prefix format, e.g.
 * http://.../travel.owl#Hotel  ->  travel:hotel  (assuming travel is Hotel's ontology label and hotel is Hotel's alias)
 * @author tom
 *
 */
public class NsPrefixOWLObjectShortFormProvider implements ShortFormProvider {

	private JASDLOntologyManager jasdlOntologyManager;

	public NsPrefixOWLObjectShortFormProvider(JASDLOntologyManager jasdlOntologyManager) {
		this.jasdlOntologyManager = jasdlOntologyManager;
	}

	public void dispose() {
	}

	public String getShortForm(OWLEntity entity) {
		try {
			Alias alias = jasdlOntologyManager.getAliasManager().getLeft(entity);
			return alias.getLabel() + ":" + alias.getFunctor();
		} catch (JASDLException e) {
			jasdlOntologyManager.getLogger().warning("Exception caught attempting to render " + entity + ". Reason: ");
			e.printStackTrace();
			return null;
		}
	}
}
