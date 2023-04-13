package com.vaadin.vbcteam5.views.questions;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.vbcteam5.ai.ModerationResponse;
import com.vaadin.vbcteam5.ai.ModerationService;
import com.vaadin.vbcteam5.data.entity.Question;
import com.vaadin.vbcteam5.data.entity.TownHall;
import com.vaadin.vbcteam5.data.entity.User;
import com.vaadin.vbcteam5.data.service.QuestionService;
import com.vaadin.vbcteam5.security.AuthenticatedUser;

public class AddQuestionDialog extends Dialog {

    private final TextArea text;
    private final Avatar avatar;
    private final TextField postingAs;
    private final Checkbox postAnonymously;
    private TownHall townHall;

    public AddQuestionDialog(AuthenticatedUser authenticatedUser, QuestionService questionService) {
        User currentUser = authenticatedUser.get().get();

        setHeaderTitle("Add question");

        text = new TextArea("Your question");
        text.setMinWidth("440px");
        text.setMinHeight("120px");
        add(text);
        text.setValueChangeMode(ValueChangeMode.EAGER);

        // binder
        Binder<Question> binder = new Binder<>(Question.class);
        binder.forField(text)
                .asRequired("This field is required.")
                .bind(Question::getText, Question::setText);

        // lower layout
        avatar = new Avatar(currentUser.getName());
        postingAs = new TextField("Posting as");
        postingAs.setReadOnly(true);
        postingAs.setValue(currentUser.getName());
        postAnonymously = new Checkbox("Post anonymously");
        postAnonymously.addValueChangeListener(event -> {
            String name = event.getValue() ? "Anonymous" : currentUser.getName();
            avatar.setName(name);
            postingAs.setValue(name);
        });
        HorizontalLayout postLayout = new HorizontalLayout(avatar, postingAs, postAnonymously);
        postLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        add(postLayout);

        // buttons
        Button cancelButton = new Button("Cancel", e -> {
            close();
            text.clear();
            postAnonymously.setValue(false);
        });
        Button saveButton = new Button("Create", e -> {
            Question question = new Question(text.getValue(), currentUser, this.townHall, !postAnonymously.getValue());
            ModerationResponse moderation = ModerationService.moderateQuestion(question.getText());
			text.setInvalid(moderation.isFlagged());

			if (moderation.isFlagged())
			{
				Notification.show(moderation.getExplanation(), 3000, Position.MIDDLE);
				return;
			}
			
            questionService.update(question);
            close();
            text.clear();
            postAnonymously.setValue(false);
        });
        saveButton.addThemeName("primary");
        saveButton.setEnabled(false);

        text.addValueChangeListener(event -> {
            boolean invalid = event.getValue().isBlank();
            saveButton.setEnabled(!invalid);
        });

        getFooter().add(cancelButton, saveButton);
    }

    public TownHall getTownHall() {
        return townHall;
    }

    public void setTownHall(TownHall townHall) {
        this.townHall = townHall;
    }
}
