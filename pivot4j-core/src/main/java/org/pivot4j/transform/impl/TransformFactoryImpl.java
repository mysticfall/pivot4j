/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.olap4j.OlapConnection;
import org.pivot4j.PivotException;
import org.pivot4j.query.QueryAdapter;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.transform.DrillExpandMember;
import org.pivot4j.transform.DrillExpandPosition;
import org.pivot4j.transform.DrillReplace;
import org.pivot4j.transform.DrillThrough;
import org.pivot4j.transform.NonEmpty;
import org.pivot4j.transform.PlaceHierarchiesOnAxes;
import org.pivot4j.transform.PlaceLevelsOnAxes;
import org.pivot4j.transform.PlaceMembersOnAxes;
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.transform.Transform;
import org.pivot4j.transform.TransformFactory;

/**
 * Default implementation of TransformFactory service. Registered Transform
 * implementations should have a public constructor with a single argument of
 * type {@link QueryAdapter} in order to be looked up successfully.
 * 
 * Users can override the {@link #createTransform(Class, QueryAdapter)} method
 * to change this behavior.
 */
public class TransformFactoryImpl implements TransformFactory {

	private Map<Class<? extends Transform>, Class<? extends Transform>> transforms;

	public TransformFactoryImpl() {
		this.transforms = new HashMap<Class<? extends Transform>, Class<? extends Transform>>();

		registerDefaultTransforms(transforms);
	}

	/**
	 * @param transforms
	 */
	protected void registerDefaultTransforms(
			Map<Class<? extends Transform>, Class<? extends Transform>> transforms) {
		transforms.put(DrillExpandMember.class, DrillExpandMemberImpl.class);
		transforms
				.put(DrillExpandPosition.class, DrillExpandPositionImpl.class);
		transforms.put(DrillReplace.class, DrillReplaceImpl.class);
		transforms.put(DrillThrough.class, DrillThroughImpl.class);
		transforms.put(NonEmpty.class, NonEmptyImpl.class);
		transforms.put(SwapAxes.class, SwapAxesImpl.class);
		transforms.put(PlaceHierarchiesOnAxes.class,
				PlaceHierarchiesOnAxesImpl.class);
		transforms.put(PlaceMembersOnAxes.class, PlaceMembersOnAxesImpl.class);
		transforms.put(PlaceLevelsOnAxes.class, PlaceLevelsOnAxesImpl.class);
		transforms.put(ChangeSlicer.class, ChangeSlicerImpl.class);
	}

	/**
	 * @see org.pivot4j.transform.TransformFactory#createTransform(java.lang.Class,
	 *      org.pivot4j.query.QueryAdapter, org.olap4j.OlapConnection)
	 */
	public <T extends Transform> T createTransform(Class<T> type,
			QueryAdapter queryAdapter, OlapConnection connection) {
		T transform = null;

		@SuppressWarnings("unchecked")
		Class<T> implementationType = (Class<T>) transforms.get(type);

		if (implementationType != null) {
			try {
				Constructor<T> constructor = implementationType.getConstructor(
						QueryAdapter.class, OlapConnection.class);
				transform = constructor.newInstance(queryAdapter, connection);
			} catch (NoSuchMethodException e) {
				String msg = "The registered implementation class does not have a suitable constructor : "
						+ implementationType;
				throw new IllegalArgumentException(msg);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				String msg = "Failed to instantiate the transfom class : "
						+ implementationType;
				throw new PivotException(msg, e);
			}
		}

		return transform;
	}
}
