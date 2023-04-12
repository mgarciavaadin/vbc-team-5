package com.vaadin.vbcteam5.views.testia;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.vbcteam5.ai.ModerationResponse;
import com.vaadin.vbcteam5.ai.ModerationService;
import com.vaadin.vbcteam5.views.MainLayout;

@PageTitle("Test Moderation")
@Route(value = "test-moderation", layout = MainLayout.class)
@AnonymousAllowed
public class TestModerationView extends HorizontalLayout {

	private TextField question;
	private Button submit;
	private Div result = new Div();

	public TestModerationView() {
		question = new TextField("Your question");
		submit = new Button("Moderate");
		submit.addClickListener(e -> {
			ModerationResponse moderation = ModerationService.moderateQuestion(question.getValue());

			question.setInvalid(!moderation.isFlagged());
			result.setText(moderation.getExplanation());
		});
		submit.addClickShortcut(Key.ENTER);

		setMargin(true);
		setVerticalComponentAlignment(Alignment.END, question, submit, result);

		add(question, submit, result);
	}

}
