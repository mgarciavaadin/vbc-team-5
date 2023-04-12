package com.vaadin.vbcteam5.views.about;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.vbcteam5.views.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@AnonymousAllowed
public class AboutView extends VerticalLayout {

    Grid<UserQuestion> questionsGrid = new Grid<>();

    // TODO use actual data instead of dummy one
    List<UserQuestion> questions = new ArrayList<>();
    String userId = "Random-1";

    public AboutView() {
        // Select townhall to operate with
        Select<String> townHallSelector = new Select<>();
        townHallSelector.setLabel("Townhall");
        // TODO: use actual data
        townHallSelector.setItems("June Town Hall", "Winter Town Hall");
        townHallSelector.setValue("June Town Hall");
        townHallSelector.addValueChangeListener(e -> switchTownHall(e.getValue()));
        add(townHallSelector);

        // Post a question
        QuestionEditor questionEditor = new QuestionEditor();
        add(questionEditor);

        QuestionToolbar questionToolbar = new QuestionToolbar(questionEditor, this::saveQuestion, this::updateQuestion, this::deleteQuestion);
        add(questionToolbar);

        questionsGrid.addColumn(createNameRenderer()).setHeader("Name");
        questionsGrid.addColumn(UserQuestion::getQuestion).setHeader("Question");
        questionsGrid.addComponentColumn(userQuestion -> {
            // TODO proper check for the user
            boolean userVoted = userQuestion.getVoteIds().indexOf(userId) > 0;

            Button voteButton = new Button("",
                    new Icon(userVoted ? VaadinIcon.THUMBS_DOWN : VaadinIcon.THUMBS_UP));
            voteButton.addClickListener(e -> {
                voteButton.setIcon(new Icon(userVoted ? VaadinIcon.THUMBS_UP : VaadinIcon.THUMBS_DOWN));
                voteForTheQuestion(userQuestion);
            });
            return voteButton;
        });
        questionsGrid.addSelectionListener(e -> e.getFirstSelectedItem().ifPresentOrElse(item -> {
            questionEditor.getBinder().readBean(item);
            questionToolbar.setEdited(item);
        }, () -> questionToolbar.setEdited(null)));
        add(questionsGrid);

        // Pre-defined styling
        setSpacing(false);
        setSizeFull();
    }

    private void voteForTheQuestion(UserQuestion userQuestion) {
        // TODO clean up and use updateQuestion
        int index = questions.indexOf(userQuestion);
        userQuestion.addVoteName(userId);
        questions.set(index, userQuestion);

        setItems(questions);
    }

    private void saveQuestion(UserQuestion userQuestion) {
        // TODO: Apply proper user
        userQuestion.setUserId("Random");

        // TODO: save the question to the DB
        questions.add(userQuestion);

        setItems(questions);
    }

    private void updateQuestion(UserQuestion userQuestion, UserQuestion newUserQuestion) {
        // TODO: save the question to the DB
        questions.set(questions.indexOf(userQuestion), newUserQuestion);
        setItems(questions);
    }

    private void deleteQuestion(UserQuestion userQuestion) {
        // TODO: delete the question from the DB
        questions.remove(userQuestion);
        setItems(questions);
    }

    // TODO clean up
    private void setItems(List<UserQuestion> questions) {
        questionsGrid.setItems(questions);
        questionsGrid.getDataProvider().refreshAll();
    }

    private void switchTownHall(String townHall) {

    }

    private static Renderer<UserQuestion> createNameRenderer() {
        return LitRenderer.<UserQuestion> of(
                        "<span>${item.name ? item.name: \"Anonymous\"}</span>")
                .withProperty("name", UserQuestion::getName);
    }
}
