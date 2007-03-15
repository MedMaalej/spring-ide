/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditorInput;

/**
 * Shows the WebflowEditor for the currently selected {@link IWebflowConfig}
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenWebflowGraphAction extends Action {

	private ICommonActionExtensionSite site;

	private IWebflowConfig element;

	public OpenWebflowGraphAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("Open &Graphical Editor"); // TODO externalize text
		setImageDescriptor(WebflowUIImages.DESC_OBJS_WEBFLOW)
	;}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				if (tElement instanceof IWebflowConfig) {
					element = (IWebflowConfig) tElement;
					return true;
				}
				else if (tElement instanceof IFile) {
					if (WebflowModelUtils.isWebflowConfig((IFile) tElement)) {
						element = WebflowModelUtils
								.getWebflowConfig((IFile) tElement);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		IEditorInput input = new WebflowEditorInput(element);
		SpringUIUtils.openInEditor(input, WebflowEditor.EDITOR_ID);
	}
}
