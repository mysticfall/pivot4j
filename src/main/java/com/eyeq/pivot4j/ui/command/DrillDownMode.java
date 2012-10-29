/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.command;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class DrillDownMode implements Serializable {

	private static final long serialVersionUID = -4715289686127729961L;

	public static final DrillDownMode POSITION = new DrillDownMode() {

		private static final long serialVersionUID = 8476006057196756345L;

		@Override
		public String getName() {
			return "position";
		}

		@Override
		public Set<DrillDownCommand> getCommands() {
			Set<DrillDownCommand> commands = new HashSet<DrillDownCommand>(2);
			commands.add(new DrillExpandPositionCommand());
			commands.add(new DrillCollapsePositionCommand());
			return commands;
		}
	};

	public static final DrillDownMode MEMBER = new DrillDownMode() {

		private static final long serialVersionUID = 4379908352253055398L;

		@Override
		public String getName() {
			return "member";
		}

		@Override
		public Set<DrillDownCommand> getCommands() {
			Set<DrillDownCommand> commands = new HashSet<DrillDownCommand>(2);
			commands.add(new DrillExpandMemberCommand());
			commands.add(new DrillCollapseMemberCommand());
			return commands;
		}
	};

	public static final DrillDownMode REPLACE = new DrillDownMode() {

		private static final long serialVersionUID = -1064522759713078988L;

		@Override
		public String getName() {
			return "replace";
		}

		@Override
		public Set<DrillDownCommand> getCommands() {
			Set<DrillDownCommand> commands = new HashSet<DrillDownCommand>(2);
			commands.add(new DrillDownReplaceCommand());
			commands.add(new DrillUpReplaceCommand());
			return commands;
		}
	};

	public abstract String getName();

	public abstract Set<DrillDownCommand> getCommands();
}
