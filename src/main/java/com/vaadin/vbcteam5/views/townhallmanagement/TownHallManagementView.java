package com.vaadin.vbcteam5.views.townhallmanagement;

import com.github.pravin.raha.lexorank4j.LexoRank;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.DescriptionList;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.vbcteam5.data.entity.Question;
import com.vaadin.vbcteam5.data.entity.TownHall;
import com.vaadin.vbcteam5.data.service.QuestionService;
import com.vaadin.vbcteam5.data.service.TownHallService;
import com.vaadin.vbcteam5.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@PageTitle("Manage Town Halls")
@Route(value = "manage-town-halls", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class TownHallManagementView extends VerticalLayout {

    private final TownHallService townHallService;
    private final QuestionService questionService;
    private final DescriptionList.Description townHallCloseDateValue;
    private final Select<TownHall> selectTownHall;
    private final HorizontalLayout townHallDetailsLayout;
    private final Grid<Question> questionsGrid;

    private GridListDataView<Question> questionsDataView;

    public TownHallManagementView(TownHallService townHallService, QuestionService questionService) {
        this.townHallService = townHallService;
        this.questionService = questionService;
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);


        selectTownHall = new Select<>();
        selectTownHall.setLabel("Select Town Hall");
        selectTownHall.getStyle().set("width", "30ch");
        selectTownHall.setItemLabelGenerator((TownHall::getName));
        selectTownHall.addValueChangeListener(event -> {
            showTownHallDetails(event.getValue());
        });

        Button createTownHall = new Button("Create Town Hall",
            new Icon(VaadinIcon.PLUS));
        createTownHall.addClickListener(e -> {
            editTownHallDialog(null);
        });

        var townHallDetails = new DescriptionList();
        townHallDetails.setClassName("town-hall-details");

        var townHallCloseDateTitle = new DescriptionList.Term("Closing date");
        townHallCloseDateValue = new DescriptionList.Description();

        townHallDetails.add(
            townHallCloseDateTitle,
            townHallCloseDateValue
        );

        var editTownHall = new Button("Edit", new Icon(VaadinIcon.PENCIL), e -> {
            editTownHallDialog(selectTownHall.getValue());
        });
        editTownHall.setThemeName("tertiary");
        editTownHall.getStyle().set("margin-inline-start", "auto");

        questionsGrid = new Grid<>();
        questionsGrid.addColumn(createQuestionRenderer()).setHeader("Question").setSortable(true).setComparator(Question::isAnonymous);
        questionsGrid.addComponentColumn((question -> {
            var upvotes = new Span(new Text(String.valueOf(question.getUpvotes().size())));
            upvotes.getElement().getThemeList().add("badge");
            return upvotes;
        })).setHeader("Upvotes").setSortable(true).setComparator((Comparator.comparingInt(
            q -> q.getUpvotes().size()))).setTextAlign(ColumnTextAlign.END);

        questionsGrid.setDropMode(GridDropMode.BETWEEN);
        questionsGrid.setRowsDraggable(true);
        AtomicReference<Question> draggedItemAtomic = new AtomicReference<>();
        questionsGrid.addDragStartListener(e -> draggedItemAtomic.set(
            e.getDraggedItems().get(0)));
        questionsGrid.addDragEndListener(e -> draggedItemAtomic.set(null));
        questionsGrid.addDropListener(e -> {
            var draggedItem = draggedItemAtomic.get();
            if (draggedItem == null) {
                 return;
            }
            var dropLocation = e.getDropLocation();
            var targetQuestion = e.getDropTargetItem().orElse(null);
            var questionWasDroppedOntoItself = draggedItem.equals(targetQuestion);

            if (targetQuestion == null || questionWasDroppedOntoItself) {
                return;
            }

            questionsDataView.removeItem(draggedItem);
            var newRank = new AtomicReference<LexoRank>();
            if (dropLocation == GridDropLocation.BELOW) {
                // We need to get the rank of the next item (if present)
                var itemAfterTargetQuestion = questionsDataView.getNextItem(targetQuestion);
                itemAfterTargetQuestion.ifPresentOrElse(nextItem -> {
                    // Calculating the rank between the target question and the next one
                    newRank.set(LexoRank.parse(targetQuestion.getRank()).between(LexoRank.parse(nextItem.getRank())));
                }, () -> {
                    // If there's no question after the target one, generate next rank
                    newRank.set(LexoRank.parse(targetQuestion.getRank()).genNext());
                });
                questionsDataView.addItemAfter(draggedItem, targetQuestion);
            } else {
                // If drop location is above, we need to get the previous item (if present)
                var itemBeforeTargetQuestion = questionsDataView.getPreviousItem(targetQuestion);
                itemBeforeTargetQuestion.ifPresentOrElse(n -> {
                    // Calculating the rank between the previous question and the target one
                    newRank.set(LexoRank.parse(n.getRank()).between(LexoRank.parse(targetQuestion.getRank())));
                }, () -> {
                    // If there's no previous question, then we need to check if the target rank is the minimum value possible
                    var previousRank = LexoRank.parse(targetQuestion.getRank());
                    if (previousRank.isMin()) {
                        // If that's the case, we give the minimum rank to the dragged question,
                        // and calculate the new rank for the target one
                        newRank.set(previousRank);
                        var newRankForTarget = new AtomicReference<LexoRank>();
                        questionsDataView.getNextItem(targetQuestion).ifPresentOrElse(nextItem ->  {
                            // If there's a question after the target one, get the rank value between the minimum
                            // and the next one and assign to the target question
                            newRankForTarget.set(previousRank.between(LexoRank.parse(nextItem.getRank())));
                        }, () -> {
                            // If there's no question, then generate the next rank and assign it
                            newRankForTarget.set(previousRank.genNext());
                        });
                        targetQuestion.setRank(newRankForTarget.get().toString());
                        questionService.update(targetQuestion);
                    } else {
                        // If target question's rank is not the minimum, get the previous rank
                        newRank.set(LexoRank.parse(targetQuestion.getRank()).genPrev());
                    }
                });
                questionsDataView.addItemBefore(draggedItem, targetQuestion);
            }
            draggedItem.setRank(newRank.toString());
            questionService.update(draggedItem);
        });

        townHallDetailsLayout = new HorizontalLayout(townHallDetails, editTownHall);
        townHallDetailsLayout.getStyle().set("display", "contents");
        townHallDetailsLayout.setPadding(false);

        refreshTownHalls();
        Div separator = new Div();
        separator.setClassName("separator");

        layout.add(selectTownHall, townHallDetailsLayout, separator, createTownHall);
        layout.setWidthFull();
        layout.addClassName("town-hall-toolbar");
        layout.setPadding(true);
        add(layout);
        add(this.questionsGrid);
        setSpacing(false);
    }

    private static Renderer<Question> createQuestionRenderer() {
        return LitRenderer.<Question>of("<vaadin-vertical-layout>"
            + "<span style='font-style: italic; font-size: 0.8rem; color: var(--lumo-secondary-text-color);'>${item.anonymous ? item.name : 'Anonymous'}</span>"
            + "<span>${item.question}</span>"
            + "</vaadin-vertical-layout>")
                .withProperty("question", Question::getText)
                .withProperty("name", (question -> question.getAuthor().getName()))
                .withProperty("anonymous", Question::isAnonymous);
    }

    private void refreshTownHalls() {
        var townHalls = townHallService.list();

        var futureTownHalls = townHalls.stream().filter(
                townHall -> townHall.getCloseDate().isAfter(LocalDateTime.now()))
            .min(Comparator.comparing(TownHall::getCloseDate));
        selectTownHall.setItems(townHalls);

        selectTownHall.setVisible(!townHalls.isEmpty());
        futureTownHalls.ifPresent(
            townHall -> selectTownHall.addComponents(townHall,
                new Hr()));
        if (!townHalls.isEmpty()) {
            selectTownHall.setValue(townHalls.get(0));
        }
        townHallDetailsLayout.setVisible(selectTownHall.getValue() != null);
        questionsGrid.setVisible(selectTownHall.getValue() != null);
    }

    private void editTownHallDialog(TownHall townHall) {
        var dialog = new Dialog();
        dialog.setHeaderTitle(townHall == null ? "Create New Town Hall" : "Edit Town Hall");
        var binder = new BeanValidationBinder<>(TownHall.class);
        var fields = new FormLayout();
        var nameField = new TextField("Name");
        binder.forField(nameField).asRequired("Name is required").bind("name");
        nameField.setValueChangeMode(ValueChangeMode.EAGER);

        var closeDateField = new DateTimePicker("Closing date");
        binder.forField(closeDateField).asRequired("Closing date is required").bind("closeDate");

        AtomicBoolean formIsDirty = new AtomicBoolean(false);
        dialog.addDialogCloseActionListener(e -> {
            if (formIsDirty.get()) {
                var exitEditingConfirmDialog = new ConfirmDialog();
                exitEditingConfirmDialog.setHeader("Unsaved changes");
                exitEditingConfirmDialog.setText("Do you want to discard your changes?");
                exitEditingConfirmDialog.setCancelable(true);
                exitEditingConfirmDialog.setCancelText("Discard");
                exitEditingConfirmDialog.setCancelButtonTheme("tertiary error");
                exitEditingConfirmDialog.addCancelListener(event -> dialog.close());
                exitEditingConfirmDialog.setConfirmText("Continue editing");
                exitEditingConfirmDialog.open();
            } else {
                dialog.close();
            }

        });

        fields.add(nameField, closeDateField);
        dialog.add(fields);

        var cancelButton = new Button("Cancel", e -> dialog.close());
        var saveButton = new Button("Save", e -> {
            if (binder.validate().isOk()) {
                townHallService.save(binder.getBean());
                refreshTownHalls();
                dialog.close();
            }
        });
        saveButton.setEnabled(false);
        binder.setBean(townHall != null ? townHall : new TownHall());
        saveButton.setThemeName("primary");

        binder.addValueChangeListener(e -> {
            formIsDirty.set(true);
            saveButton.setEnabled(true);
        });

        if (townHall != null) {
            var deleteButton = new Button("Delete", ev -> {
                // confirm before delete
                var confirmDeleteDialog = new ConfirmDialog();
                confirmDeleteDialog.setHeader("Are you sure you want to delete this Town Hall?");
                confirmDeleteDialog.setText("This action cannot be reverted.");
                confirmDeleteDialog.setCancelable(true);
                confirmDeleteDialog.setConfirmText("Delete");
                confirmDeleteDialog.setConfirmButtonTheme("primary error");
                confirmDeleteDialog.addConfirmListener(e -> {
                    questionService.listByTownHall(townHall.getId()).forEach(question -> questionService.delete(question.getId()));
                    townHallService.delete(townHall);
                    refreshTownHalls();
                    dialog.close();
                    Notification.show(String.format("Town hall \"%s\" has been deleted.", townHall.getName()));
                });
                confirmDeleteDialog.open();

            });
            deleteButton.setThemeName("tertiary error");
            deleteButton.getStyle().set("margin-inline-end", "auto");
            dialog.getFooter().add(deleteButton);

            var stopTownHallButton = new Button("Stop Town Hall", e -> {
                var confirmStopTownHall = new ConfirmDialog();
                confirmStopTownHall.setHeader("Are you sure you want to stop this Town Hall?");
                confirmStopTownHall.setText("This will change the closing date to the current date and time.");
                confirmStopTownHall.setCancelable(true);
                confirmStopTownHall.setConfirmText("Stop");
                confirmStopTownHall.setConfirmButtonTheme("primary");
                confirmStopTownHall.addConfirmListener(ev -> {
                    townHall.setCloseDate(LocalDateTime.now());
                    townHallService.save(townHall);
                    refreshTownHalls();
                    dialog.close();
                    Notification.show(String.format("Town hall \"%s\" has been stopped.", townHall.getName()));
                });
                confirmStopTownHall.open();
            });
            stopTownHallButton.setThemeName("tertiary");
            dialog.getFooter().add(stopTownHallButton);
        }

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void showTownHallDetails(TownHall townHall) {
        if (townHall == null) {
            return;
        }

        townHallCloseDateValue.setText(townHall.getCloseDate().format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
        questionsDataView = questionsGrid.setItems(questionService.listByTownHall(townHall.getId()));
    }
}
