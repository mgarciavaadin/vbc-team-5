package com.vaadin.vbcteam5.views.townhallmanagement;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.vbcteam5.data.entity.TownHall;
import com.vaadin.vbcteam5.data.service.TownHallService;
import com.vaadin.vbcteam5.views.MainLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;

@PageTitle("Manage Town Halls")
@Route(value = "manage-town-halls", layout = MainLayout.class)
@AnonymousAllowed
public class TownHallManagementView extends VerticalLayout {

    private final TownHallService townHallService;
    private Button createTownHall;
    private TextField selectedTownHallName;
    private TextField selectedTownHallCloseDate;
    private Select<TownHall> selectTownHall;

    public TownHallManagementView(TownHallService townHallService) {
        this.townHallService = townHallService;
        HorizontalLayout layout = new HorizontalLayout();
        layout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);


        selectTownHall = new Select<>();
        selectTownHall.setLabel("Select Town Hall");
        selectTownHall.setItemLabelGenerator((TownHall::getName));
        selectTownHall.addValueChangeListener(event -> {
            showTownHallDetails(event.getValue());
        });

        createTownHall = new Button("Create Town Hall", new Icon(VaadinIcon.PLUS));
        createTownHall.addClickListener(e -> {
            editTownHallDialog(null);
        });
        createTownHall.addClickShortcut(Key.ENTER);

        selectedTownHallName = new TextField("Name");
        selectedTownHallName.setReadOnly(true);
        selectedTownHallCloseDate = new TextField("Closing date");
        selectedTownHallCloseDate.setReadOnly(true);

        var townHallDetails = new HorizontalLayout(selectedTownHallName,
            selectedTownHallCloseDate);
        var editTownHall = new Button("Edit", e -> {
            editTownHallDialog(selectTownHall.getValue());
        });
        var townHallDetailsLayout = new VerticalLayout(townHallDetails, editTownHall);

        setMargin(true);

        fillSelectTownHall();
        townHallDetailsLayout.setVisible(selectTownHall.getValue() != null);


        layout.add(selectTownHall, createTownHall);
        add(layout);
        add(townHallDetailsLayout);
    }

    private void fillSelectTownHall() {
        var townHalls = getTownHallsInDescendentOrder(townHallService);

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
    }

    private static List<TownHall> getTownHallsInDescendentOrder(TownHallService townHallService) {
        return townHallService.list().stream().sorted(
                (tw1, tw2) -> tw2.getCloseDate().compareTo(tw1.getCloseDate()))
            .toList();
    }

    private void editTownHallDialog(TownHall townHall) {
        var dialog = new Dialog();
        dialog.setHeaderTitle(townHall == null ? "Create New Town Hall" : "Edit Town Hall");
        var fields = new FormLayout();
        var nameField = new TextField("Name");
        nameField.setRequired(true);
        var closeDateField = new DateTimePicker("Closing date");
        closeDateField.setRequiredIndicatorVisible(true);
        fields.add(nameField, closeDateField);
        dialog.add(fields);

        var cancelButton = new Button("Cancel", e -> dialog.close());
        var saveButton = new Button("Save", e -> {
            var townHallToSave = townHall != null ? townHall : new TownHall();
            townHallToSave.setName(nameField.getValue());
            townHallToSave.setCloseDate(closeDateField.getValue());
            townHallService.save(townHallToSave);
            fillSelectTownHall();
            dialog.close();
        });

        if (townHall != null) {
            nameField.setValue(townHall.getName());
            closeDateField.setValue(townHall.getCloseDate());
        }

        saveButton.setThemeName("primary");
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void showTownHallDetails(TownHall townHall) {
        if (townHall == null) {
            return;
        }
        selectedTownHallName.setValue(townHall.getName());
        selectedTownHallCloseDate.setValue(townHall.getCloseDate().format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));

    }

    private List<TownHall> createTownHallList() {
        return List.of(
            new TownHall("Town hall June/23", LocalDateTime.of(2023, 6, 12, 12, 0)),
            new TownHall("Town hall May/23", LocalDateTime.of(2023, 5, 23, 12, 0)),
            new TownHall("Town hall Jan/23", LocalDateTime.of(2023, 1, 29, 12, 0)),
            new TownHall("Town hall Sep/22", LocalDateTime.of(2022, 9, 17, 12, 0))
        );
    }
}
