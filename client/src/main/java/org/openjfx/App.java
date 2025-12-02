package org.openjfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class App extends Application {

    // --- DATA MODELS ---

    private static class User {
        String id;
        String displayName;
        boolean isOnline;

        public User(String id, String displayName, boolean isOnline) {
            this.id = id;
            this.displayName = displayName;
            this.isOnline = isOnline;
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return id.equals(user.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    private static class UserProfile {
        String username;
        String email;
        String displayName;
        String status;

        public UserProfile(String username, String email, String displayName, String status) {
            this.username = username;
            this.email = email;
            this.displayName = displayName;
            this.status = status;
        }
    }

    private static class Contact {
        String id;
        String name;
        String colorHex;
        Image image;
        int unreadCount;
        boolean isGroup;
        boolean isOnline;
        List<User> participants;

        public Contact(String id, String name, String colorHex, Image image, boolean isGroup, boolean isOnline) {
            this.id = id;
            this.name = name;
            this.colorHex = colorHex;
            this.image = image;
            this.unreadCount = 0;
            this.isGroup = isGroup;
            this.isOnline = isOnline;
            this.participants = new ArrayList<>();
        }

        public String getParticipantNames() {
            if (participants.isEmpty()) return "";
            return participants.stream()
                    .map(u -> u.displayName.split(" ")[0]) // First names only for brevity
                    .collect(Collectors.joining(", "));
        }
    }

    // --- UI COMPONENTS ---
    private BorderPane root;
    private VBox sidebar;
    private VBox chatView;
    private VBox placeholderView;

    private VBox chatArea;
    private ScrollPane chatScrollPane;
    private HBox inputArea;
    private TextField messageInput;
    private VBox contactListContainer;

    // Header Components
    private Label currentChatLabel;
    private Label currentChatSubtitle;
    private StackPane currentChatAvatarPane;
    private HBox chatHeaderActions; // Container for top-right buttons

    // --- STATE ---
    private final List<Contact> allContacts = new ArrayList<>();
    private final List<User> mockAllUsers = new ArrayList<>();

    private String currentConversationId = null;
    private UserProfile myProfile;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GroupSpeak");

        loadMockData();

        root = new BorderPane();
        root.getStyleClass().add("root");

        sidebar = createSidebar();
        chatView = createChatPane();
        placeholderView = createPlaceholderPane();

        root.setLeft(sidebar);
        root.setCenter(placeholderView);

        Scene scene = new Scene(root, 1100, 750);

        URL cssUrl = getClass().getResource("/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.show();

        startMockBackgroundListener();
    }

    private void loadMockData() {
        myProfile = new UserProfile("john_doe", "john@example.com", "John Doe", "Online");

        User u1 = new User("101", "Alice Johnson", true);
        User u2 = new User("102", "Bob Smith", false);
        User u3 = new User("103", "Charlie Brown", true);
        User u4 = new User("104", "David Warner", true);
        User u5 = new User("105", "Eve Polastri", false);
        User u6 = new User("106", "Frank Castle", true);
        User u7 = new User("107", "Grace Hopper", true);
        User u8 = new User("108", "Adam Mohamed", false);

        mockAllUsers.add(u1); mockAllUsers.add(u2); mockAllUsers.add(u3);
        mockAllUsers.add(u4); mockAllUsers.add(u5); mockAllUsers.add(u6);
        mockAllUsers.add(u7); mockAllUsers.add(u8);

        // Active Chats
        Contact c1 = new Contact("1", "Alice Johnson", "#ef4444", null, false, true);
        c1.participants.add(u1);
        allContacts.add(c1);

        Contact c2 = new Contact("2", "Bob Smith", "#3b82f6", null, false, false);
        c2.participants.add(u2);
        allContacts.add(c2);

        Contact group = new Contact("3", "Project Team", "#10b981", null, true, true);
        group.participants.add(u1); // Alice
        group.participants.add(u2); // Bob
        allContacts.add(group);
    }

    private void startMockBackgroundListener() {
        Thread mockListener = new Thread(() -> {
            try {
                Thread.sleep(4000);
                Platform.runLater(() -> {
                    if (!allContacts.isEmpty()) {
                        // Find the currently open contact to make the demo feel alive
                        Contact current = null;
                        if (currentConversationId != null) {
                            current = allContacts.stream()
                                    .filter(c -> c.id.equals(currentConversationId))
                                    .findFirst()
                                    .orElse(null);
                        }

                        if (current != null) {
                            // If chat is open, receive a message there
                            String sender = "Mock User";
                            if (!current.participants.isEmpty()) {
                                sender = current.participants.get(0).displayName;
                            }
                            addMessage("Hey! This is a test message to show the sender name.", sender, false);
                        } else {
                            // If no chat open, just increment Alice's unread
                            Contact alice = allContacts.get(0);
                            alice.unreadCount++;
                            renderContactList(allContacts);
                        }
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        mockListener.setDaemon(true);
        mockListener.start();
    }

    // --- CUSTOM LIST CELL FACTORY (Avatar + Name) ---
    private Callback<ListView<User>, ListCell<User>> getUserListCellFactory(boolean isMultiSelect) {
        return listView -> new ListCell<User>() {
            {
                selectedProperty().addListener((obs, wasSelected, isSelected) -> updateVisualState(isSelected));

                if (isMultiSelect) {
                    addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                        if (!isEmpty() && getItem() != null) {
                            MultipleSelectionModel<User> sm = getListView().getSelectionModel();
                            if (sm.getSelectedIndices().contains(getIndex())) {
                                sm.clearSelection(getIndex());
                            } else {
                                sm.select(getIndex());
                            }
                            event.consume();
                        }
                    });
                }
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    StackPane avatar = createAvatarPane(user.displayName, "#3b82f6", 32, 12, user.isOnline);

                    VBox textInfo = new VBox(2);
                    Label name = new Label(user.displayName);
                    name.setStyle("-fx-text-fill: -text-primary; -fx-font-weight: bold;");

                    Label status = new Label(user.isOnline ? "Online" : "Offline");
                    status.setStyle("-fx-text-fill: " + (user.isOnline ? "#22c55e" : "-text-secondary") + "; -fx-font-size: 11px;");

                    textInfo.getChildren().addAll(name, status);
                    box.getChildren().addAll(avatar, textInfo);
                    setGraphic(box);

                    updateVisualState(isSelected());
                }
            }

            private void updateVisualState(boolean isSelected) {
                if (getItem() == null || getGraphic() == null) return;

                HBox box = (HBox) getGraphic();
                VBox textInfo = (VBox) box.getChildren().get(1);
                Label name = (Label) textInfo.getChildren().get(0);

                if (isSelected) {
                    setStyle("-fx-background-color: -selected-color;");
                    name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-background-color: transparent;");
                    name.setStyle("-fx-text-fill: -text-primary; -fx-font-weight: bold;");
                }
            }
        };
    }

    // --- NEW: Group Info Window (View Members / Add / Remove) ---
    private void showGroupInfoDialog(Contact group) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Group Info");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        if (root.getStyleClass().contains("light-theme")) pane.getStyleClass().add("light-theme");

        pane.getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);

        StackPane avatar = createAvatarPane(group.name, group.colorHex, 60, 20, false);
        Label nameLabel = new Label(group.name);
        nameLabel.getStyleClass().add("dialog-text");
        nameLabel.setStyle("-fx-font-size: 18px;");

        Label countLabel = new Label(group.participants.size() + " participants");
        countLabel.getStyleClass().add("contact-sub");

        VBox headerBox = new VBox(5, avatar, nameLabel, countLabel);
        headerBox.setAlignment(Pos.CENTER);

        ListView<User> membersList = new ListView<>();
        membersList.setItems(FXCollections.observableArrayList(group.participants));
        membersList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        membersList.setCellFactory(getUserListCellFactory(true));
        membersList.getStyleClass().add("dialog-list-view");
        membersList.setPrefHeight(250);

        Button addBtn = new Button("Add Participant");
        addBtn.getStyleClass().add("send-btn");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setPrefHeight(40);
        HBox.setHgrow(addBtn, Priority.ALWAYS);

        addBtn.setOnAction(e -> {
            showAddParticipantDialog(group);
            membersList.setItems(FXCollections.observableArrayList(group.participants));
            countLabel.setText(group.participants.size() + " participants");
        });

        Button removeBtn = new Button("Remove Selected");
        removeBtn.getStyleClass().add("logout-btn");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setPrefHeight(40);
        HBox.setHgrow(removeBtn, Priority.ALWAYS);

        removeBtn.setDisable(true);
        membersList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<User>) c ->
                removeBtn.setDisable(membersList.getSelectionModel().getSelectedItems().isEmpty())
        );

        removeBtn.setOnAction(e -> {
            List<User> selected = new ArrayList<>(membersList.getSelectionModel().getSelectedItems());
            if (!selected.isEmpty()) {
                group.participants.removeAll(selected);
                membersList.setItems(FXCollections.observableArrayList(group.participants));
                countLabel.setText(group.participants.size() + " participants");
                updateChatHeader(group);

                removeBtn.setDisable(true);
            }
        });

        HBox buttonBox = new HBox(15, addBtn, removeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setFillHeight(true);

        content.getChildren().addAll(headerBox, new Separator(), membersList, buttonBox);
        pane.setContent(content);

        dialog.showAndWait();
    }

    private void showNewConversationDialog() {
        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("New Conversation");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        if (root.getStyleClass().contains("light-theme")) dialogPane.getStyleClass().add("light-theme");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 20, 20));

        // 1. Radio Buttons
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton rbOneToOne = new RadioButton("One-to-One Conversation");
        rbOneToOne.setToggleGroup(typeGroup);
        rbOneToOne.setSelected(true);
        rbOneToOne.getStyleClass().add("dialog-radio");

        RadioButton rbGroup = new RadioButton("Group Chat");
        rbGroup.setToggleGroup(typeGroup);
        rbGroup.getStyleClass().add("dialog-radio");

        HBox radioBox = new HBox(15, rbOneToOne, rbGroup);

        // 2. Common ListView for selecting people (Single for DM, Multi for Group)
        Label selectUserLabel = new Label("Select Person:");
        selectUserLabel.getStyleClass().add("dialog-text");

        ListView<User> userListView = new ListView<>();
        userListView.setPrefHeight(200);
        userListView.setPrefWidth(300);
        userListView.getStyleClass().add("dialog-list-view");

        // 3. Group Name Field
        Label groupNameLabel = new Label("Group Name:");
        groupNameLabel.getStyleClass().add("dialog-text");
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("Enter group name...");
        groupNameField.getStyleClass().add("search-field");

        // Logic to switch between modes
        Runnable setupForOneToOne = () -> {
            grid.getChildren().removeAll(groupNameLabel, groupNameField);
            selectUserLabel.setText("Select Person:");

            // Filter: Online & Not already chatted with
            List<User> available = mockAllUsers.stream()
                    .filter(u -> u.isOnline)
                    .filter(u -> allContacts.stream().noneMatch(c -> c.name.equals(u.displayName)))
                    .collect(Collectors.toList());

            userListView.setItems(FXCollections.observableArrayList(available));
            userListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            userListView.setCellFactory(getUserListCellFactory(false)); // False = No custom click toggle

            if (available.isEmpty()) userListView.setPlaceholder(new Label("No new online users."));
        };

        Runnable setupForGroup = () -> {
            if (!grid.getChildren().contains(groupNameField)) {
                grid.add(groupNameLabel, 0, 1);
                grid.add(groupNameField, 1, 1);
            }
            selectUserLabel.setText("Participants (Online):");

            // --- CHANGED: Filter for online users only ---
            List<User> onlineUsers = mockAllUsers.stream()
                    .filter(u -> u.isOnline)
                    .collect(Collectors.toList());

            userListView.setItems(FXCollections.observableArrayList(onlineUsers));
            userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            userListView.setCellFactory(getUserListCellFactory(true)); // True = Click to toggle enabled

            if (onlineUsers.isEmpty()) userListView.setPlaceholder(new Label("No online users found."));
        };

        // Initial Layout
        grid.add(radioBox, 0, 0, 2, 1);
        grid.add(selectUserLabel, 0, 2);
        grid.add(userListView, 1, 2);

        setupForOneToOne.run(); // Default state

        // Toggle Listeners
        rbOneToOne.setOnAction(e -> {
            setupForOneToOne.run();
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        });
        rbGroup.setOnAction(e -> {
            setupForGroup.run();
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        });

        dialogPane.setContent(grid);

        Button createBtn = (Button) dialogPane.lookupButton(createButtonType);
        createBtn.setDisable(true);

        // Validation
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            boolean hasSelection = !userListView.getSelectionModel().getSelectedItems().isEmpty();
            if (rbOneToOne.isSelected()) {
                createBtn.setDisable(!hasSelection);
            } else {
                boolean hasName = !groupNameField.getText().trim().isEmpty();
                createBtn.setDisable(!hasSelection || !hasName);
            }
        });

        groupNameField.textProperty().addListener((obs, old, val) -> {
            if (rbGroup.isSelected()) {
                boolean hasSelection = !userListView.getSelectionModel().getSelectedItems().isEmpty();
                createBtn.setDisable(val.trim().isEmpty() || !hasSelection);
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == createButtonType) {
                if (rbOneToOne.isSelected()) {
                    User u = userListView.getSelectionModel().getSelectedItem();
                    Contact c = new Contact(String.valueOf(System.currentTimeMillis()), u.displayName, "#6366f1", null, false, true);
                    c.participants.add(u);
                    return c;
                } else {
                    String name = groupNameField.getText();
                    Contact c = new Contact(String.valueOf(System.currentTimeMillis()), name, "#10b981", null, true, true);
                    c.participants.addAll(userListView.getSelectionModel().getSelectedItems());
                    return c;
                }
            }
            return null;
        });

        Optional<Contact> result = dialog.showAndWait();
        result.ifPresent(contact -> {
            allContacts.add(0, contact);
            renderContactList(allContacts);
            openChat(contact);
        });
    }

    private void showAddParticipantDialog(Contact group) {
        Dialog<List<User>> dialog = new Dialog<>();
        dialog.setTitle("Add Participants");
        dialog.setHeaderText("Add people to " + group.name);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        if (root.getStyleClass().contains("light-theme")) dialogPane.getStyleClass().add("light-theme");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // --- CHANGED: Filter for online users who are NOT already in the group ---
        List<User> available = mockAllUsers.stream()
                .filter(u -> u.isOnline)
                .filter(u -> group.participants.stream().noneMatch(p -> p.id.equals(u.id)))
                .collect(Collectors.toList());

        ListView<User> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(available));
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setCellFactory(getUserListCellFactory(true)); // Click to toggle
        listView.setPrefHeight(200);
        listView.getStyleClass().add("dialog-list-view");

        if (available.isEmpty()) {
            listView.setPlaceholder(new Label("No other online users available."));
        }

        VBox content = new VBox(10, new Label("Select users:"), listView);
        content.setPadding(new Insets(20));
        dialogPane.setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == addButtonType) return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
            return null;
        });

        Optional<List<User>> result = dialog.showAndWait();
        result.ifPresent(users -> {
            if(!users.isEmpty()) {
                group.participants.addAll(users);
                if (currentConversationId != null && currentConversationId.equals(group.id)) {
                    updateChatHeader(group);
                }
            }
        });
    }

    // --- GENERAL UI METHODS ---

    private void showProfileView() {
        VBox profileView = new VBox(20);
        profileView.setAlignment(Pos.CENTER);
        profileView.getStyleClass().add("chat-pane");
        profileView.setPadding(new Insets(40));

        Button backBtn = new Button("← Back to Chats");
        backBtn.getStyleClass().add("simple-btn");
        backBtn.setOnAction(e -> restoreMainView());

        StackPane bigAvatar = createAvatarPane(myProfile.displayName, "#3b82f6", 80, 24, false);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxWidth(500);

        String labelStyle = "-fx-text-fill: -text-secondary; -fx-font-size: 14px;";
        String valueStyle = "-fx-text-fill: -text-primary; -fx-font-size: 16px; -fx-font-weight: bold;";

        addProfileRow(grid, 0, "Display Name", myProfile.displayName, labelStyle, valueStyle);
        addProfileRow(grid, 1, "Username", "@" + myProfile.username, labelStyle, valueStyle);
        addProfileRow(grid, 2, "Email", myProfile.email, labelStyle, valueStyle);

        Label statusLabel = new Label("Status");
        statusLabel.setStyle(labelStyle);
        Label statusValue = new Label(myProfile.status);
        statusValue.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 16px; -fx-font-weight: bold;");
        grid.add(statusLabel, 0, 3);
        grid.add(statusValue, 1, 3);

        profileView.getChildren().addAll(backBtn, bigAvatar, grid);

        root.setLeft(null);
        root.setCenter(profileView);
    }

    private void addProfileRow(GridPane grid, int row, String label, String value, String lStyle, String vStyle) {
        Label l = new Label(label);
        l.setStyle(lStyle);
        Label v = new Label(value);
        v.setStyle(vStyle);
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private void restoreMainView() {
        root.setLeft(sidebar);
        if (currentConversationId != null) {
            root.setCenter(chatView);
        } else {
            root.setCenter(placeholderView);
        }
    }

    private VBox createSidebar() {
        VBox sidebarBox = new VBox();
        sidebarBox.getStyleClass().add("sidebar");
        sidebarBox.setPrefWidth(340);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 20, 10, 20));

        Label appTitle = new Label("Chats");
        appTitle.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newChatBtn = new Button("+");
        newChatBtn.getStyleClass().add("icon-btn");
        newChatBtn.setTooltip(new Tooltip("New Conversation"));
        newChatBtn.setOnAction(e -> showNewConversationDialog());

        Button profileBtn = new Button("👤");
        profileBtn.getStyleClass().add("icon-btn");
        profileBtn.setTooltip(new Tooltip("My Profile"));
        profileBtn.setOnAction(e -> showProfileView());

        ToggleButton themeSwitch = new ToggleButton("☀");
        themeSwitch.getStyleClass().add("icon-btn");
        themeSwitch.setOnAction(e -> {
            if (themeSwitch.isSelected()) {
                root.getStyleClass().add("light-theme");
                themeSwitch.setText("☾");
            } else {
                root.getStyleClass().remove("light-theme");
                themeSwitch.setText("☀");
            }
        });

        header.getChildren().addAll(appTitle, spacer, newChatBtn, profileBtn, themeSwitch);

        HBox searchContainer = new HBox();
        searchContainer.setPadding(new Insets(0, 20, 10, 20));
        TextField searchField = new TextField();
        searchField.setPromptText("Search conversations...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefHeight(35);
        searchField.prefWidthProperty().bind(sidebarBox.widthProperty().subtract(40));
        searchField.textProperty().addListener((obs, old, newVal) -> filterContacts(newVal));
        searchContainer.getChildren().add(searchField);

        contactListContainer = new VBox(5);
        contactListContainer.getStyleClass().add("contact-list");
        contactListContainer.setPadding(new Insets(5, 10, 5, 10));

        ScrollPane contactScroll = new ScrollPane(contactListContainer);
        contactScroll.getStyleClass().add("scroll-pane-clean");
        contactScroll.setFitToWidth(true);
        VBox.setVgrow(contactScroll, Priority.ALWAYS);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(15, 20, 15, 20));
        footer.getStyleClass().add("sidebar-footer");

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(logoutBtn, Priority.ALWAYS);
        logoutBtn.setOnAction(e -> showLogoutDialog());

        footer.getChildren().add(logoutBtn);

        renderContactList(allContacts);

        sidebarBox.getChildren().addAll(header, searchContainer, contactScroll, footer);
        return sidebarBox;
    }

    private void filterContacts(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            renderContactList(allContacts);
        } else {
            List<Contact> filtered = allContacts.stream()
                    .filter(c -> c.name.toLowerCase().contains(searchText.toLowerCase()))
                    .collect(Collectors.toList());
            renderContactList(filtered);
        }
    }

    private void renderContactList(List<Contact> contactsToDisplay) {
        contactListContainer.getChildren().clear();

        for (Contact contact : contactsToDisplay) {
            HBox contactBox = new HBox(12);
            contactBox.getStyleClass().add("contact-item");
            contactBox.setAlignment(Pos.CENTER_LEFT);
            contactBox.setPadding(new Insets(12, 15, 12, 15));

            StackPane avatarPane = createAvatarPane(contact.name, contact.colorHex, 42, 14, contact.isOnline);

            VBox infoBox = new VBox(2);
            Label nameLabel = new Label(contact.name);
            nameLabel.getStyleClass().add("contact-name");
            Label subLabel = new Label(contact.isGroup ? "Group Chat" : "One-to-One Conversation");
            subLabel.getStyleClass().add("contact-sub");
            infoBox.getChildren().addAll(nameLabel, subLabel);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            contactBox.getChildren().addAll(avatarPane, infoBox);

            if (contact.unreadCount > 0) {
                Label unreadBadge = new Label(String.valueOf(contact.unreadCount));
                unreadBadge.getStyleClass().add("unread-badge");
                contactBox.getChildren().add(unreadBadge);
            }

            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete Conversation");
            deleteItem.setOnAction(e -> deleteConversation(contact));
            contextMenu.getItems().add(deleteItem);

            contactBox.setOnContextMenuRequested(e ->
                    contextMenu.show(contactBox, e.getScreenX(), e.getScreenY()));

            contactBox.setOnMouseClicked(e -> openChat(contact));

            if (currentConversationId != null && currentConversationId.equals(contact.id)) {
                contactBox.getStyleClass().add("selected");
            }

            contactListContainer.getChildren().add(contactBox);
        }
    }

    private void deleteConversation(Contact contact) {
        allContacts.remove(contact);
        if (currentConversationId != null && currentConversationId.equals(contact.id)) {
            currentConversationId = null;
            root.setCenter(placeholderView);
        }
        renderContactList(allContacts);
    }

    private StackPane createAvatarPane(String name, String colorHex, double size, double fontSize, boolean isOnline) {
        StackPane stack = new StackPane();
        stack.setPrefSize(size, size);
        stack.setAlignment(Pos.CENTER);

        Circle bg = new Circle(size / 2);
        bg.getStyleClass().add("avatar");
        bg.setStyle("-fx-fill: " + colorHex + ";");

        Label initials = new Label(getInitials(name));
        initials.getStyleClass().add("initials");
        initials.setStyle("-fx-font-size: " + fontSize + "px; -fx-text-fill: white; -fx-font-weight: bold;");

        stack.getChildren().addAll(bg, initials);

        if (isOnline) {
            Circle onlineDot = new Circle(6);
            onlineDot.getStyleClass().add("green-dot");
            StackPane.setAlignment(onlineDot, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(onlineDot, new Insets(0, 2, 2, 0));
            stack.getChildren().add(onlineDot);
        }

        return stack;
    }

    private VBox createPlaceholderPane() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("chat-pane");

        Label icon = new Label("💬");
        icon.setStyle("-fx-font-size: 64px; -fx-text-fill: -text-secondary; -fx-opacity: 0.5;");

        Label text = new Label("Select a conversation to start chatting");
        text.setStyle("-fx-font-size: 18px; -fx-text-fill: -text-secondary; -fx-font-weight: bold;");

        box.getChildren().addAll(icon, text);
        return box;
    }

    private VBox createChatPane() {
        VBox pane = new VBox();
        pane.getStyleClass().add("chat-pane");
        VBox.setVgrow(pane, Priority.ALWAYS);

        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-bar");
        topBar.setPrefHeight(70);
        topBar.setMinHeight(70);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 25, 0, 25));

        currentChatAvatarPane = createAvatarPane("?", "#3f3f46", 40, 14, false);

        // --- CHANGED: Revert to VBox to place names below group name ---
        VBox titleBox = new VBox(2);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        currentChatLabel = new Label("Select a conversation");
        currentChatLabel.getStyleClass().add("current-chat-label");

        currentChatSubtitle = new Label("");
        currentChatSubtitle.getStyleClass().add("contact-sub");
        // Optional: Ensure text doesn't wrap weirdly if it gets too long
        currentChatSubtitle.setMinWidth(Control.USE_PREF_SIZE);

        titleBox.getChildren().addAll(currentChatLabel, currentChatSubtitle);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        chatHeaderActions = new HBox(10);
        chatHeaderActions.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().addAll(currentChatAvatarPane, titleBox, spacer, chatHeaderActions);

        chatArea = new VBox(12);
        chatArea.getStyleClass().add("chat-area");
        chatArea.setPadding(new Insets(20));

        chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.getStyleClass().add("scroll-pane-clean");
        chatScrollPane.setFitToWidth(true);
        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        inputArea = createInputArea();
        inputArea.setVisible(true);
        inputArea.setManaged(true);

        pane.getChildren().addAll(topBar, chatScrollPane, inputArea);
        return pane;
    }

    private HBox createInputArea() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.getStyleClass().add("message-input");
        messageInput.setPrefHeight(45);
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("send-btn");
        sendButton.setPrefHeight(45);

        sendButton.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());

        box.getChildren().addAll(messageInput, sendButton);
        return box;
    }

    private void openChat(Contact contact) {
        this.currentConversationId = contact.id;

        updateChatHeader(contact);

        contact.unreadCount = 0;
        renderContactList(allContacts);

        chatArea.getChildren().clear();
        root.setCenter(chatView);

        Platform.runLater(() -> messageInput.requestFocus());
    }

    private void updateChatHeader(Contact contact) {
        currentChatLabel.setText(contact.name);

        StackPane newAvatar = createAvatarPane(contact.name, contact.colorHex, 40, 14, false);
        // Header structure: Avatar, TitleBox (HBox), Spacer, Actions
        // Avatar is at index 0
        HBox topBar = (HBox) currentChatLabel.getParent().getParent();
        topBar.getChildren().set(0, newAvatar);

        chatHeaderActions.getChildren().clear();

        if (contact.isGroup) {
            String names = contact.getParticipantNames();
            // --- CHANGED: Added "You" to the start of the list ---
            currentChatSubtitle.setText(names.isEmpty() ? "You" : "You, " + names);

            Button infoBtn = new Button("⋮");
            infoBtn.getStyleClass().add("icon-btn");
            infoBtn.setTooltip(new Tooltip("Group Info"));
            infoBtn.setOnAction(e -> showGroupInfoDialog(contact));
            chatHeaderActions.getChildren().add(infoBtn);
        } else {
            // --- CHANGED: Removed extra spaces for vertical layout ---
            currentChatSubtitle.setText(contact.isOnline ? "Online" : "Offline");
        }
    }

    private void sendMessage() {
        String text = messageInput.getText().trim();
        if (!text.isEmpty()) {
            addMessage(text, "You", true);
            messageInput.clear();
            messageInput.requestFocus();
        }
    }

    // --- CHANGED: Updated signature to accept sender name ---
    private void addMessage(String text, String senderName, boolean isSentByMe) {
        // 1. Container for the whole row (Alignment)
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(2, 0, 2, 0));

        // 2. Container for the bubble + name (Vertical stack)
        VBox bubbleContainer = new VBox(2);

        // 3. Sender Name Label (Only for received messages in groups)
        Contact currentChat = allContacts.stream()
                .filter(c -> c.id.equals(currentConversationId))
                .findFirst()
                .orElse(null);

        // Only show name if: received, is a group chat, and we have a name
        if (!isSentByMe && currentChat != null && currentChat.isGroup && senderName != null) {
            Label nameLabel = new Label(senderName);
            // WhatsApp style: small, bold, distinct color
            String nameColor = getNameColor(senderName);
            nameLabel.setStyle("-fx-text-fill: " + nameColor + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 5 0 5;");
            bubbleContainer.getChildren().add(nameLabel);
        }

        // 4. Message Bubble
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(500);

        if (isSentByMe) {
            messageLabel.getStyleClass().add("message-sent");
            bubbleContainer.setAlignment(Pos.CENTER_RIGHT); // Content aligns right
            messageBox.setAlignment(Pos.CENTER_RIGHT);      // Box aligns right
        } else {
            messageLabel.getStyleClass().add("message-received");
            bubbleContainer.setAlignment(Pos.CENTER_LEFT);
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            Clipboard.getSystemClipboard().setContent(content);
        });
        messageLabel.setContextMenu(contextMenu);

        bubbleContainer.getChildren().add(messageLabel);
        messageBox.getChildren().add(bubbleContainer);

        chatArea.getChildren().add(messageBox);
        chatArea.heightProperty().addListener((o, old, n) -> chatScrollPane.setVvalue(1.0));
    }

    // --- Helper for consistent name colors ---
    private String getNameColor(String name) {
        if (name == null) return "#8b5cf6";
        int hash = name.hashCode();
        // A simple palette of readable colors
        String[] colors = {
                "#ef4444", // red
                "#3b82f6", // blue
                "#10b981", // green
                "#f59e0b", // amber
                "#8b5cf6", // purple
                "#ec4899", // pink
                "#06b6d4", // cyan
                "#f97316"  // orange
        };
        return colors[Math.abs(hash) % colors.length];
    }

    private void showLogoutDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Sign Out");
        dialog.setHeaderText(null);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        if (root.getStyleClass().contains("light-theme")) {
            dialogPane.getStyleClass().add("light-theme");
        }

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label instruction = new Label("Are you sure you want to sign out?");
        instruction.getStyleClass().add("dialog-text");

        content.getChildren().add(instruction);
        dialogPane.setContent(content);

        ButtonType yesType = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType noType = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(yesType, noType);

        Button yesBtn = (Button) dialogPane.lookupButton(yesType);
        yesBtn.getStyleClass().add("logout-btn"); // Red outline style

        Button noBtn = (Button) dialogPane.lookupButton(noType);
        noBtn.getStyleClass().add("simple-btn");

        dialog.setResultConverter(btn -> btn == yesType);

        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(confirmed -> {
            if (confirmed) {
                Platform.exit();
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    public static void main(String[] args) {
        launch(args);
    }
}