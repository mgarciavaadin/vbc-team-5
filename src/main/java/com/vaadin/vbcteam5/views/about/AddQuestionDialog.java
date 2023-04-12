package com.vaadin.vbcteam5.views.about;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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

    public AddQuestionDialog(AuthenticatedUser authenticatedUser, QuestionService questionService, final TownHall townHall) {
        User currentUser = authenticatedUser.get().get();

        setHeaderTitle("Add question");

        text = new TextArea("Your question");
        text.setRequired(true);
        text.setRequiredIndicatorVisible(false);
        text.setMinWidth("440px");
        text.setMinHeight("120px");
        add(text);

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
        Button cancelButton = new Button("Cancel", e -> close());
        Button saveButton = new Button("Create", e -> {
            Question question = new Question(text.getValue(), currentUser, townHall, postAnonymously.getValue());
            questionService.update(question);
            close();
        });
        saveButton.addThemeName("primary");
        saveButton.setEnabled(false);
        text.addValueChangeListener(event -> saveButton.setEnabled(!text.getValue().isBlank()));

        getFooter().add(cancelButton, saveButton);
    }

}
