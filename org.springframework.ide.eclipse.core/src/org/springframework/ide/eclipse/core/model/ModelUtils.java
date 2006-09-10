/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 */
public final class ModelUtils {

	/**
	 * Trys to adapt given element to <code>IModelElement</code>.
	 */
	public static Object adaptToModelElement(Object element) {
		if (!(element instanceof IModelElement) &&
											 (element instanceof IAdaptable)) {
			return ((IAdaptable) element).getAdapter(IModelElement.class);
		}
		return element;
	}

	/**
	 * Removes all Spring problem markers (including the inherited ones) from
	 * given <code>IModelElement</code>.
	 */
	public static void deleteProblemMarkers(IModelElement element) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			SpringCoreUtils.deleteProblemMarkers(resource);
		}
	}

	/**
	 * Returns the full path of the give element's resource or
	 * <code>null</code> if no element or resource found. 
	 */
	public static String getResourcePath(IResourceModelElement element) {
		if (element != null) {
			IResource resource = element.getElementResource();
			if (resource != null) {
				return resource.getFullPath().toString();
			}
		}
		return null;
	}
}
