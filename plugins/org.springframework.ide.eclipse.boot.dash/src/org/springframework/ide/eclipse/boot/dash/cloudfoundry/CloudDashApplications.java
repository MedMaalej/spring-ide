/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.Collection;

import org.springframework.ide.eclipse.boot.dash.livexp.DisposingFactory;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSetUtil;
import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression.AsyncMode;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * An instance of this class is responsible for managing a list of {@link CloudAppDashElement}. This means:
 * <p>
 * <ul>
 *   <li> calling 'dispose' on any no longer needed elements.
 *   <li> ensuring that only a single object exists to represent an element with a given identity.
 *   <li> creating the elements as needed.
 * </ul>
 *
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class CloudDashApplications extends AbstractDisposable {

	private final LiveSetVariable<String> appNames = new LiveSetVariable<>(AsyncMode.SYNC);
	private final ObservableSet<CloudAppDashElement> applications;
	private final DisposingFactory<String, CloudAppDashElement> factory;

	public CloudDashApplications(final CloudFoundryBootDashModel model) {
		factory = new DisposingFactory<String, CloudAppDashElement>(appNames) {
			@Override
			protected CloudAppDashElement create(String appName) {
				return new CloudAppDashElement(model, appName, model.getPropertyStore());
			}
		};
		applications = LiveSetUtil.map(appNames, AsyncMode.SYNC, AsyncMode.ASYNC, new Function<String, CloudAppDashElement>() {
			@Override
			public CloudAppDashElement apply(String appName) {
				return factory.createOrGet(appName);
			}
		});
		addDisposableChild(factory);
		addDisposableChild(applications);
	}

	public void setAppNames(Collection<String> names) {
		appNames.replaceAll(names);
	}

	public ObservableSet<CloudAppDashElement> getApplications() {
		return applications;
	}

	public ImmutableSet<CloudAppDashElement> getApplicationValues() {
		return applications.getValues();
	}

	public CloudAppDashElement getApplication(String appName) {
		return factory.createOrGet(appName);
	}

	public CloudAppDashElement addApplication(String name) {
		appNames.add(name);
		return factory.createOrGet(name);
	}

	public void removeApplication(String name) {
		appNames.remove(name);
	}
}
