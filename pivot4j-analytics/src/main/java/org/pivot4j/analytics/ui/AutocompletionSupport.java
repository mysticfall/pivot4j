package org.pivot4j.analytics.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import org.apache.commons.lang3.StringUtils;
import org.olap4j.OlapException;
import org.olap4j.impl.IdentifierParser;
import org.olap4j.mdx.IdentifierSegment;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.PivotModel;
import org.primefaces.extensions.event.CompleteEvent;

@ManagedBean(name = "autoCompletionSupport")
@RequestScoped
public class AutocompletionSupport {

	@ManagedProperty(value = "#{pivotStateManager.model}")
	private PivotModel model;

	public static List<String> FUNCTIONS = Arrays
			.asList("Union", "Hierarchize");

	public static List<String> MEMBER_FUNCTIONS = Arrays.asList("Children");

	public List<String> complete(CompleteEvent event) throws OlapException {
		List<String> suggestions = new ArrayList<String>();

		if (model.isInitialized() && model.getCube() != null
				&& event.getToken() != null) {
			Cube cube = model.getCube();

			List<IdentifierSegment> context = getIdentifiers(event.getContext());

			String token = decode(event.getToken());

			if (context.isEmpty()) {
				matchElements(token, suggestions, cube.getHierarchies());
			} else if (context.size() == 1) {
				String name = context.get(0).getName();
				Hierarchy hierarchy = cube.getHierarchies().get(name);

				if (hierarchy != null) {
					List<? extends Member> members = hierarchy.getRootMembers();

					matchElements(token, suggestions, members);

					for (Member member : members) {
						if (member.isAll()) {
							matchElements(token, suggestions,
									member.getChildMembers());
						}
					}

					matchKeywords(token, suggestions, MEMBER_FUNCTIONS);
				}
			} else {
				Member parent = cube.lookupMember(context);

				if (parent != null) {
					matchElements(token, suggestions, parent.getChildMembers());
					matchKeywords(token, suggestions, MEMBER_FUNCTIONS);
				}
			}

			if (context.isEmpty()) {
				matchKeywords(token, suggestions, FUNCTIONS);
			}
		}

		return suggestions;
	}

	private static void matchElements(String keyword, List<String> result,
			List<? extends MetadataElement> elements) {
		for (MetadataElement element : elements) {
			if (keyword == null || element.getName().startsWith(keyword)) {
				result.add("[" + element.getName() + "]");
			}
		}
	}

	private static void matchKeywords(String keyword, List<String> result,
			List<String> elements) {
		for (String element : elements) {
			if (keyword == null || element.startsWith(keyword)) {
				result.add(element);
			}
		}
	}

	private static String decode(String token) {
		if (StringUtils.isBlank(token) || "null".equals(token)) {
			return null;
		}

		try {
			return URLDecoder.decode(token.trim(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new FacesException(e);
		}
	}

	private static List<IdentifierSegment> getIdentifiers(String token) {
		String identifier = decode(token);

		if (identifier == null) {
			return Collections.emptyList();
		}

		return IdentifierParser.parseIdentifier(identifier);
	}

	/**
	 * @return the model
	 */
	public PivotModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(PivotModel model) {
		this.model = model;
	}
}
