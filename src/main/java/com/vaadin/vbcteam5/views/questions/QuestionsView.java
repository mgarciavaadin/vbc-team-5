package com.vaadin.vbcteam5.views.questions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.vbcteam5.data.entity.Question;
import com.vaadin.vbcteam5.data.entity.TownHall;
import com.vaadin.vbcteam5.data.entity.User;
import com.vaadin.vbcteam5.data.service.QuestionService;
import com.vaadin.vbcteam5.data.service.TownHallService;
import com.vaadin.vbcteam5.security.AuthenticatedUser;
import com.vaadin.vbcteam5.views.MainLayout;
import org.vaadin.lineawesome.LineAwesomeIcon;

@PageTitle("Questions")
@Route(value = "questions", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class QuestionsView extends VerticalLayout {

    private Select<TownHall> townHallSelector;
    private AddQuestionDialog addQuestionDialog;
    private Button showAddQuestionDialog;
    Grid<Question> questionsGrid = new Grid<>();

    List<Question> questions;

    // services
    private final AuthenticatedUser authenticatedUser;

    private final QuestionService questionService;

    private final TownHallService townHallService;

    public QuestionsView(AuthenticatedUser authenticatedUser, QuestionService questionService, TownHallService townHallService) {
        this.authenticatedUser = authenticatedUser;
        this.questionService = questionService;
        this.townHallService = townHallService;
        if (this.authenticatedUser.get().isEmpty()) {
            return;
        }
        User currentUser = this.authenticatedUser.get().get();
        // Select town hall to operate with
        townHallSelector = new Select<>();
        townHallSelector.setLabel("Select Town Hall");
        townHallSelector.setWidth("40ch");
        List<TownHall> townHalls = this.townHallService.list();
        townHallSelector.setItems(townHalls);
        townHallSelector.setValue(townHalls.get(0));
        townHallSelector.setItemLabelGenerator(th -> th.getName() + " | " + th.getCloseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // Post a question
        addQuestionDialog = new AddQuestionDialog(authenticatedUser, questionService);
        addQuestionDialog.addOpenedChangeListener(l -> {
            if (!l.isOpened()) {
                refreshQuestions();
            }
        });
        showAddQuestionDialog = new Button("Add question", LineAwesomeIcon.PLUS_SOLID.create(), e -> {
            addQuestionDialog.setTownHall(townHallSelector.getValue());
            addQuestionDialog.open();
        });
        showAddQuestionDialog.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // is town hall closed?
        Span townHallClosedMessage = new Span("This town hall is closed.");
        townHallClosedMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        townHallSelector.addValueChangeListener(e -> {
            refreshQuestions();
            boolean isTownHallClosed = e.getValue().getCloseDate().isBefore(LocalDateTime.now());
            townHallClosedMessage.setVisible(isTownHallClosed);
            showAddQuestionDialog.setEnabled(!isTownHallClosed);
        });
        boolean isTownHallClosed = townHallSelector.getValue().getCloseDate().isBefore(LocalDateTime.now());
        showAddQuestionDialog.setEnabled(!isTownHallClosed);
        townHallClosedMessage.setVisible(isTownHallClosed);

        HorizontalLayout questionDialogLayout = new HorizontalLayout(townHallSelector, showAddQuestionDialog, townHallClosedMessage);
        questionDialogLayout.setAlignItems(Alignment.BASELINE);
        add(questionDialogLayout);

        questionsGrid.addColumn(createQuestionRenderer()).setHeader("Question").setAutoWidth(true);
        questionsGrid.addComponentColumn(userQuestion -> {
            int upvotes = userQuestion.getUpvotes().size();
            Span numOfVotes = new Span(String.format("%d %s", upvotes, upvotes == 1 ? "upvote" : "upvotes"));
            boolean townHallClosed = townHallSelector.getValue().getCloseDate().isBefore(LocalDateTime.now());
            numOfVotes.getElement().getThemeList().add("badge");
            // check if current user is the question's author
            if (userQuestion.getAuthor().equals(currentUser)) {
                Button deleteButton = new Button(LineAwesomeIcon.TRASH_ALT.create(), e -> {
                    ConfirmDialog confirmDialog = new ConfirmDialog();
                    confirmDialog.setHeader("Confirm");
                    confirmDialog.setText(new Html(String.format("<p>Are you sure want to delete this question?<br /><span class=\"font-bold\">%s<span></p>", userQuestion.getText())));
                    confirmDialog.addConfirmListener(confirmEvent -> {
                        this.questionService.delete(userQuestion.getId());
                        refreshQuestions();
                    });
                    confirmDialog.setCancelable(true);
                    confirmDialog.setConfirmText("Delete");
                    confirmDialog.setConfirmButtonTheme("primary error");
                    confirmDialog.open();
                });
                deleteButton.getElement().setAttribute("aria-label", "Remove question: " + userQuestion.getText());
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                HorizontalLayout buttonsLayout = new HorizontalLayout(numOfVotes);
                buttonsLayout.setJustifyContentMode(JustifyContentMode.END);
                if (!townHallClosed) {
                    buttonsLayout.add(deleteButton);
                }
                return buttonsLayout;
            }
            boolean userVoted = userQuestion.getUpvotes().contains(currentUser);

            Button voteButton = new Button("", LineAwesomeIcon.THUMBS_UP.create());
            if (userVoted) {
                voteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                voteButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
            voteButton.addClickListener(e -> {
                voteForTheQuestion(userQuestion);
            });
            HorizontalLayout votesLayout = new HorizontalLayout(numOfVotes);
            votesLayout.setJustifyContentMode(JustifyContentMode.END);
            if (!townHallClosed) {
                votesLayout.add(voteButton);
            }
            return votesLayout;
        });
        questions = this.questionService.listByTownHall(townHallSelector.getValue().getId());
        questionsGrid.setItems(questions);
//        questionsGrid.addSelectionListener(e -> e.getFirstSelectedItem().ifPresentOrElse(item -> {
//            questionEditor.getBinder().readBean(item);
//            questionToolbar.setEdited(item);
//        }, () -> questionToolbar.setEdited(null)));
        add(questionsGrid);

        // Pre-defined styling
        setSpacing(false);
        setSizeFull();
    }

    private static Renderer<Question> createQuestionRenderer() {
        return LitRenderer.<Question>of("<vaadin-vertical-layout style='padding-block: var(--lumo-space-s);'>"
                + "<span style='font-style: italic; font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);'>${item.anonymous ? 'Anonymous' : item.name }</span>"
                + "<span>${item.question}</span>"
                + "</vaadin-vertical-layout>")
            .withProperty("question", Question::getText)
            .withProperty("name", (question -> question.getAuthor().getName()))
            .withProperty("anonymous", Question::isAnonymous);
    }

    private void refreshQuestions() {
        this.questions.clear();
        this.questions.addAll(this.questionService.listByTownHall(townHallSelector.getValue().getId()));
        this.questionsGrid.getDataProvider().refreshAll();
    }

    private void voteForTheQuestion(Question userQuestion) {
        User currentUser = this.authenticatedUser.get().get();
        if (userQuestion.getUpvotes().contains(currentUser)) {
            userQuestion.getUpvotes().remove(currentUser);
        } else {
            userQuestion.getUpvotes().add(currentUser);
        }
        questionService.update(userQuestion);
        refreshQuestions();
    }

}
