/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.devtools.DevtoolsPortRefresher;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker.ProjectRunStateListener;
import org.springframework.ide.eclipse.boot.dash.views.BootDashModelConsoleManager;
import org.springframework.ide.eclipse.boot.dash.views.BootDashTreeView;
import org.springframework.ide.eclipse.boot.dash.views.LocalElementConsoleManager;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ProjectChangeListenerManager.ProjectChangeListener;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Model of the contents for {@link BootDashTreeView}, provides mechanism to attach listeners to model
 * and attaches itself as a workspace listener to keep the model in synch with workspace changes.
 *
 * @author Kris De Volder
 */
public class LocalBootDashModel extends BootDashModel {

	private IWorkspace workspace;
	ProjectChangeListenerManager openCloseListenerManager;
	ClasspathListenerManager classpathListenerManager;
	BootDashElementFactory elementFactory;
	ProjectRunStateTracker runStateTracker;
	LiveSet<BootDashElement> elements; //lazy created
	private BootDashModelConsoleManager consoleManager;

	private BootDashModelStateSaver modelState;
	private DevtoolsPortRefresher devtoolsPortRefresher;

	public class WorkspaceListener implements ProjectChangeListener, ClasspathListener {

		@Override
		public void projectChanged(IProject project) {
			updateElementsFromWorkspace();
		}

		@Override
		public void classpathChanged(IJavaProject jp) {
			updateElementsFromWorkspace();
		}
	}

	public LocalBootDashModel(BootDashModelContext context) {
		super(RunTargets.LOCAL);
		this.workspace = context.getWorkspace();
		this.elementFactory = new BootDashElementFactory(this, context.getProjectProperties());
		this.consoleManager = new LocalElementConsoleManager();
		try {
			ISavedState lastState = workspace.addSaveParticipant(BootDashActivator.PLUGIN_ID, modelState = new BootDashModelStateSaver(context, elementFactory));
			modelState.restore(lastState);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		this.devtoolsPortRefresher = new DevtoolsPortRefresher(this, elementFactory);
	}

	void init() {
		if (elements==null) {
			this.elements = new LiveSet<BootDashElement>();
			WorkspaceListener workspaceListener = new WorkspaceListener();
			this.openCloseListenerManager = new ProjectChangeListenerManager(workspace, workspaceListener);
			this.classpathListenerManager = new ClasspathListenerManager(workspaceListener);
			this.runStateTracker = new ProjectRunStateTracker() {
				@Override
				protected boolean isInteresting(ILaunch l) {
					try {
						ILaunchConfiguration conf = l.getLaunchConfiguration();
						if (conf!=null) {
							IProject p = BootLaunchConfigurationDelegate.getProject(conf);
							if (p!=null) {
								BootDashElement pe = elementFactory.createOrGet(p);
								if (pe!=null) {
									return conf.equals(pe.getActiveConfig());
								}
							}
						}
					} catch (Exception e) {
						BootActivator.log(e);
					}
					return false;
				}
			};
			runStateTracker.setListener(new ProjectRunStateListener() {
				public void stateChanged(IProject p) {
					BootDashElement e = elementFactory.createOrGet(p);
					if (e!=null) {
						notifyElementChanged(e);
					}
				}
			});
			updateElementsFromWorkspace();
		}
	}

	void updateElementsFromWorkspace() {
		Set<BootDashElement> newElements = new HashSet<BootDashElement>();
		for (IProject p : this.workspace.getRoot().getProjects()) {
			BootDashElement element = elementFactory.createOrGet(p);
			if (element!=null) {
				newElements.add(element);
			}
		}
		for (BootDashElement oldElement : elements.getValues()) {
			if (!newElements.contains(oldElement)) {
				if (oldElement instanceof Disposable) {
					((Disposable) oldElement).dispose();
				}
			}
		}
		elements.replaceAll(newElements);
	}

	public synchronized LiveSet<BootDashElement> getElements() {
		init();
		return elements;
	}

	/**
	 * When no longer needed the model should be disposed, otherwise it will continue
	 * listening for changes to the workspace in order to keep itself in synch.
	 */
	public void dispose() {
		if (elements!=null) {
			elements = null;
			openCloseListenerManager.dispose();
			elementFactory.dispose();
			classpathListenerManager.dispose();
			runStateTracker.dispose();
			devtoolsPortRefresher.dispose();
		}
	}

	/**
	 * Trigger manual model refresh.
	 */
	public void refresh() {
		updateElementsFromWorkspace();
	}

	@Override
	public BootDashModelConsoleManager getElementConsoleManager() {
		return consoleManager;
	}


	////////////// listener cruft ///////////////////////////



	public ProjectRunStateTracker getRunStateTracker() {
		return runStateTracker;
	}

	public ILaunchConfiguration getPreferredConfigs(WrappingBootDashElement<IProject> e) {
		return modelState.getPreferredConfig(e);
	}

	public void setPreferredConfig(
			WrappingBootDashElement<IProject> e,
			ILaunchConfiguration c) {
		modelState.setPreferredConfig(e, c);
	}
}
