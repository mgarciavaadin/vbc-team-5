package com.vaadin.vbcteam5.ai;

import com.theokanning.openai.moderation.Moderation;
import com.theokanning.openai.moderation.ModerationRequest;
import com.theokanning.openai.moderation.ModerationResult;
import com.theokanning.openai.service.OpenAiService;

public class ModerationService {

	static String APIKEY = "APIKEY";

	public static ModerationResponse moderateQuestion(String input) {
		OpenAiService service = new OpenAiService(APIKEY);

		try {
		ModerationRequest moderationRequest = ModerationRequest.builder().input(input).build();

		ModerationResult moderationResult = service.createModeration(moderationRequest);

		Moderation moderation = moderationResult.getResults().get(0);

		if (moderation == null)
			return ModerationResponse.createAcceptedExplanation();

		ModerationResponse result = null;

		if (!moderation.isFlagged())
			result = ModerationResponse.createAcceptedExplanation();
		else
			result = ModerationResponse.createModerationResponse(moderation.isFlagged(),
					moderation.getCategories().isHate(), moderation.getCategories().isHateThreatening(),
					moderation.getCategories().isViolence(), moderation.getCategories().isViolenceGraphic(),
					moderation.getCategories().isSexual(), moderation.getCategories().isSexualMinors(),
					moderation.getCategories().isSelfHarm());
		service.shutdownExecutor();

		return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			return  ModerationResponse.createAcceptedExplanation();
		}
	}
}
