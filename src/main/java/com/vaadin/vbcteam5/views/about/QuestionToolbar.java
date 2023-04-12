package com.vaadin.vbcteam5.views.about;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;

public class QuestionToolbar extends HorizontalLayout {

    Button deleteButton = new Button("Delete");
    Button discardButton = new Button("Discard");

    private UserQuestion editedQuestion;
    private QuestionEditor questionEditor;

    public QuestionToolbar(QuestionEditor questionEditor,
                           SerializableConsumer<UserQuestion> saveCallback,
                           SerializableBiConsumer<UserQuestion, UserQuestion> updateCallback,
                           SerializableConsumer<UserQuestion> deleteCallback) {

        this.questionEditor = questionEditor;

        // Save button
        Button saveButton = new Button("Save", e -> {
            UserQuestion question = new UserQuestion();
            try {
                questionEditor.getBinder().writeBean(question);
                if (editedQuestion != null) {
                    updateCallback.accept(editedQuestion, question);
                } else {
                    saveCallback.accept(question);
                }
            } catch (ValidationException exception) {
                Notification.show("Question was not submitted, please check the fields!");
            }
        });
        add(saveButton);

        // Delete button
        deleteButton.addClickListener(e -> deleteCallback.accept(editedQuestion));
        add(deleteButton);

        // Discard button
        discardButton.addClickListener(e -> this.discardQuestion());
        add(discardButton);

        setButtonsVisible(false);
    }

    public void setEdited(UserQuestion question) {
        this.editedQuestion = question;
        setButtonsVisible(question != null);
        if (question == null) {
            discardQuestion();
        }
    }

    private void discardQuestion() {
        this.editedQuestion = null;
        questionEditor.getBinder().readBean(new UserQuestion());
    }

    private void setButtonsVisible(boolean visible) {
        deleteButton.setVisible(visible);
        discardButton.setVisible(visible);
    }
}
