package com.vaadin.vbcteam5.ai;

public class ModerationResponse {

	boolean Flagged = false;
	String explanation = "This message is ok";

	static String flaggedExplanationTemplate = "This message is not accepted because it expresses a %%CATEGORIES%% message";

	private static String createFlaggedExplanation(boolean hate, boolean hateThreatening, boolean violence,
			boolean violenceGraphic, boolean sexual, boolean sexualMinors, boolean selfHarm) {
		String explanation = flaggedExplanationTemplate;

		String flags = "";

		if (hate)
			flags = flags + ", hate";
		else if (hateThreatening)
			flags = flags + ", hate threatening";
		else if (violence)
			flags = flags + ", violence";
		else if (violenceGraphic)
			flags = flags + ", graphic violence";
		else if (sexual)
			flags = flags + ", sexual";
		else if (sexualMinors)
			flags = flags + ", sexual related to minors";
		else if (selfHarm)
			flags = flags + ", self harm";
		return (flags.length() > 0 ? explanation.replace("%%CATEGORIES%%", flags.substring(1)) : "");
	}

	protected ModerationResponse(String explanation) {
		super();
		this.explanation = explanation;
	}

	protected ModerationResponse(boolean flagged, boolean hate, boolean hateThreatening, boolean violence,
			boolean violenceGraphic, boolean sexual, boolean sexualMinors, boolean selfHarm) {
		super();
		Flagged = flagged;
		this.explanation = createFlaggedExplanation(hate, hateThreatening, violence, violenceGraphic, sexual,
				sexualMinors, selfHarm);
	}

	static ModerationResponse createAcceptedExplanation() {
		return new ModerationResponse("The message is OK");
	}

	static ModerationResponse createModerationResponse(boolean flagged, boolean hate, boolean hateThreatening,
			boolean violence, boolean violenceGraphic, boolean sexual, boolean sexualMinors, boolean selfHarm) {
		return new ModerationResponse(flagged, hate, hateThreatening, violence, violenceGraphic, sexual, sexualMinors,
				selfHarm);
	}

	public boolean isFlagged() {
		return Flagged;
	}

	public String getExplanation() {
		return explanation;
	}

}
