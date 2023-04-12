package com.vaadin.vbcteam5.views.about;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

public class QuestionEditor extends FormLayout {
    private Binder<UserQuestion> binder;

    public QuestionEditor() {
        // For binding the form to the data model
        binder = new Binder<>(UserQuestion.class);

        Checkbox anonymousQuestion = new Checkbox("Anonymous");

        TextField nameField = new TextField("Name");
        binder
                .forField(nameField)
                .withValidator(userName -> anonymousQuestion.getValue() && userName.isBlank() || !anonymousQuestion.getValue() && !userName.isBlank(), "Name should not be empty")
                .bind(UserQuestion::getName, UserQuestion::setName);
        add(nameField);

        TextArea questionField = new TextArea("Question");
        binder
                .forField(questionField)
                .withValidator(userQuestion -> userQuestion.length() > 5, "Question should contain 5+ characters")
                .bind(UserQuestion::getQuestion, UserQuestion::setQuestion);
        add(questionField);

        anonymousQuestion.addValueChangeListener(e -> {
            nameField.setEnabled(!e.getValue());
            nameField.setHelperText(e.getValue() ? "Name will not be saved" : "");
            // Clean up invalid state
            nameField.setInvalid(false);
        });
        add(anonymousQuestion);

        setResponsiveSteps(new ResponsiveStep("0", 1));
    }

    public Binder<UserQuestion> getBinder() {
        return binder;
    }
}
