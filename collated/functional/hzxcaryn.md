# hzxcaryn
###### \java\seedu\ptman\commons\events\model\UserModeChangedEvent.java
``` java
/**
 * Indicates that the user mode has changed. (Admin mode or not)
 */
public class UserModeChangedEvent extends BaseEvent {

    public final boolean isAdminMode;

    public UserModeChangedEvent(boolean isAdminMode) {
        this.isAdminMode = isAdminMode;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
```
###### \java\seedu\ptman\commons\events\ui\ExportTimetableAsImageAndEmailRequestEvent.java
``` java
/**
 * An event requesting to export the timetable view as an image and email it to the given email
 */
public class ExportTimetableAsImageAndEmailRequestEvent extends BaseEvent {

    public final String filename;
    public final Email email;

    public ExportTimetableAsImageAndEmailRequestEvent(String filename, Email email) {
        this.filename = filename;
        this.email = email;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
```
###### \java\seedu\ptman\commons\events\ui\ExportTimetableAsImageRequestEvent.java
``` java
/**
 * An event requesting to export the timetable view as an image locally
 */
public class ExportTimetableAsImageRequestEvent extends BaseEvent {

    public final String filename;

    public ExportTimetableAsImageRequestEvent(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
```
###### \java\seedu\ptman\commons\events\ui\OutletInformationChangedEvent.java
``` java
/**
 * Represents an information change in the Outlet Information
 */
public class OutletInformationChangedEvent extends BaseEvent {

    public final String operatingHours;
    public final String outletContact;
    public final String outletEmail;

    public OutletInformationChangedEvent(String operatingHours, String outletContact, String outletEmail) {
        this.operatingHours = operatingHours;
        this.outletContact = outletContact;
        this.outletEmail = outletEmail;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
```
###### \java\seedu\ptman\commons\events\ui\OutletNameChangedEvent.java
``` java
/**
 * Represents an information change in the Outlet Information
 */
public class OutletNameChangedEvent extends BaseEvent {

    public final String message;

    public OutletNameChangedEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
```
###### \java\seedu\ptman\commons\services\EmailService.java
``` java
    /**
     * Send exported timetable image as an attachment to user
     * @param email
     * @param filename
     * @throws MessagingException
     */
    public void sendTimetableAttachment(String email, String filename) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmailTimetable));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject("[PTMan] My Timetable");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText("Dear Valued PTMan user,\n\nAttached is your exported timetable.\n"
                + "Thank you for using PTMan and have a nice day!\n\nBest Regards,\nThe PTMan Team");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }

}
```
###### \java\seedu\ptman\logic\commands\ExportCommand.java
``` java
/**
 * Exports current timetable view as an image locally
 */
public class ExportCommand extends Command {

    public static final String COMMAND_WORD = "export";
    public static final String COMMAND_ALIAS = "exp";

    public static final String COMMAND_FORMAT = "[" + PREFIX_EMAIL + "EMAIL]";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Exports timetable as image. "
            + "If email is stated, timetable image will be sent as an attachment to the stated email. "
            + "Else, timetable image will be saved locally.\n"
            + "Parameters: "
            + COMMAND_FORMAT
            + "\nExample: " + COMMAND_WORD + " "
            + PREFIX_EMAIL + "email@example.com";

    public static final String MESSAGE_SAVE_SUCCESS = "Timetable is successfully exported!";
    public static final String MESSAGE_EMAIL_SUCCESS = "Timetable is successfully sent to your email!";

    private final Email emailToSendImageTo;

    /**
     * Creates an ExportCommand to save the timetable as image locally
     */
    public ExportCommand() {
        emailToSendImageTo = null;
    }

    /**
     * Creates an ExportCommand to send the timetable image to the user's email
     * @param email
     */
    public ExportCommand(Email email) {
        requireNonNull(email);
        emailToSendImageTo = email;
    }

    @Override
    public CommandResult execute() {
        if (emailToSendImageTo != null) {
            EventsCenter.getInstance().post(new ExportTimetableAsImageAndEmailRequestEvent(
                    TIMETABLE_IMAGE_FILE_NAME_DEFAULT + LocalDateTime.now().toString(), emailToSendImageTo));
            return new CommandResult(MESSAGE_EMAIL_SUCCESS);
        } else {
            EventsCenter.getInstance().post(new ExportTimetableAsImageRequestEvent(TIMETABLE_IMAGE_FILE_NAME_DEFAULT));
            return new CommandResult(MESSAGE_SAVE_SUCCESS);
        }
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ExportCommand)) {
            return false;
        }

        // state check
        ExportCommand e = (ExportCommand) other;
        return emailToSendImageTo.equals(e.emailToSendImageTo);
    }

}
```
###### \java\seedu\ptman\logic\parser\ExportCommandParser.java
``` java
/**
 * Parses input arguments and creates a new ExportCommand object
 */
public class ExportCommandParser implements Parser<ExportCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ExportCommand
     * and returns an ExportCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public ExportCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_EMAIL);

        if (!arePrefixesPresent(argMultimap, PREFIX_EMAIL) && argMultimap.getPreamble().isEmpty()) {
            return new ExportCommand();
        } else if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
        }

        try {
            Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL)).get();
            return new ExportCommand(email);
        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
    }
}
```
###### \java\seedu\ptman\ui\AdminModeDisplay.java
``` java
/**
 * Admin mode display of the app. Displays whether the user is in admin mode or not.
 */
public class AdminModeDisplay extends UiPart<Region> {

    public static final String LABEL_STYLE_CLASS_ADMIN = "label-admin-mode";
    public static final String LABEL_STYLE_CLASS_NON_ADMIN = "label-non-admin-mode";

    private static final String adminModeText = "Admin Mode";
    private static final Logger logger = LogsCenter.getLogger(AdminModeDisplay.class);
    private static final String FXML = "AdminModeDisplay.fxml";

    @FXML
    private Label adminModeDisplay;

    public AdminModeDisplay(boolean isAdminMode) {
        super(FXML);

        adminModeDisplay.setText(adminModeText);
        setLabelStyle(isAdminMode);

        registerAsAnEventHandler(this);
    }

    private void setLabelStyle(boolean isAdminMode) {
        ObservableList<String> styleClass = this.adminModeDisplay.getStyleClass();
        if (isAdminMode) {
            styleClass.remove(LABEL_STYLE_CLASS_NON_ADMIN);
            styleClass.add(LABEL_STYLE_CLASS_ADMIN);
        } else {
            styleClass.remove(LABEL_STYLE_CLASS_ADMIN);
            styleClass.add(LABEL_STYLE_CLASS_NON_ADMIN);
        }
    }

    @Subscribe
    private void handleUserModeChangedEvent(UserModeChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        Platform.runLater(() -> setLabelStyle(event.isAdminMode));
    }

}
```
###### \java\seedu\ptman\ui\EmployeeListPanel.java
``` java
    /**
     * Scrools to the top of the {@code EmployeeListPanel} and deselect any current selection.
     */
    private void scrollToTopAndDeselect() {
        Platform.runLater(() -> {
            employeeListView.scrollTo(0);
            employeeListView.getSelectionModel().clearSelection();
        });
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        if (event.isNewSelection) {
            scrollToAndSelect(event.targetIndex);
        } else {
            scrollToTopAndDeselect();
        }
    }

```
###### \java\seedu\ptman\ui\OutletDetailsPanel.java
``` java
/**
 * The Outlet Panel of the App, which displays the Outlet name and details
 */
public class OutletDetailsPanel extends UiPart<Region> {

    private static final String FXML = "OutletDetailsPanel.fxml";

    public final OutletInformation outlet;

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    @FXML
    private Label outletNamePanelHeader;

    @FXML
    private Label operatingHours;

    @FXML
    private Label outletContact;

    @FXML
    private Label outletEmail;

    @FXML
    private Label announcement;


    public OutletDetailsPanel(OutletInformation outlet) {
        super(FXML);
        this.outlet = outlet;
        //outletInformation.setWrapText(true);
        setOutletInformation(outlet.getOperatingHours().getDisplayedMessage(),
                outlet.getOutletContact().toString(),
                outlet.getOutletEmail().toString());
        setOutletName(outlet.getName().toString());
        setAnnouncement(outlet.getAnnouncement().toString());

        registerAsAnEventHandler(this);
    }

    private void setOutletName(String name) {
        outletNamePanelHeader.setText(name);
    }

    private void setOutletInformation(String operatingHours, String outletContact, String outletEmail) {
        this.operatingHours.setText(operatingHours + "    ");
        this.outletContact.setText(outletContact + "    ");
        this.outletEmail.setText(outletEmail);
    }

```
###### \java\seedu\ptman\ui\OutletDetailsPanel.java
``` java
    @Subscribe
    private void handleOutletInformationChangedEvent(OutletInformationChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        Platform.runLater(() -> setOutletInformation(event.operatingHours, event.outletContact, event.outletEmail));
    }

    @Subscribe
    private void handleOutletNameChangedEvent(OutletNameChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        Platform.runLater(() -> setOutletName(event.message));
    }

```
###### \java\seedu\ptman\ui\PtmanLogoDisplay.java
``` java
/**
 * Displays the PTMan logo at the left side of the command box.
 */
public class PtmanLogoDisplay extends UiPart<Region> {

    private static final String imagePath = "/icons/ptman_logo_icon.png";
    private static final String FXML = "PtmanLogoDisplay.fxml";

    @FXML
    private ImageView ptmanLogoView;

    public PtmanLogoDisplay() {
        super(FXML);

        Image ptmanLogo = new Image(imagePath);
        ptmanLogoView.setImage(ptmanLogo);
        setLogoSize();
    }

    /**
     * Scale the logo image to the desired size
     */
    private void setLogoSize() {
        ptmanLogoView.setFitWidth(35);
        ptmanLogoView.setPreserveRatio(true);
    }
}
```
###### \java\seedu\ptman\ui\TimetablePanel.java
``` java
/**
 * Displays the GUI Timetable.
 */
public class TimetablePanel extends UiPart<Region> {

    public static final String TIMETABLE_IMAGE_FILE_NAME_DEFAULT = "MyTimetable";
    public static final String TIMETABLE_IMAGE_FILE_FORMAT = "png";

    private static final int TIMETABLE_IMAGE_PIXEL_SCALE = 2;
    private static final String FXML = "TimetableView.fxml";
    private static final int MAX_SLOTS_LEFT_RUNNING_OUT = 3;

    private static final Style ENTRY_GREEN_STYLE = Style.STYLE1;
    private static final Style ENTRY_BLUE_STYLE = Style.STYLE2;
    private static final Style ENTRY_YELLOW_STYLE = Style.STYLE3;
    private static final Style ENTRY_RED_STYLE = Style.STYLE5;
    private static final Style ENTRY_BROWN_STYLE = Style.STYLE7;

    private static Calendar timetableAvail;
    private static Calendar timetableEmployee;
    private static Calendar timetableFull;
    private static Calendar timetableOthers;
    private static Calendar timetableRunningOut;

    private final Logger logger = LogsCenter.getLogger(this.getClass());

    @FXML
    private CalendarView timetableView;

    private ObservableList<Shift> shiftObservableList;
    private OutletInformation outletInformation;


    private Employee currentEmployee = null;

    protected TimetablePanel(ObservableList<Shift> shiftObservableList, OutletInformation outletInformation) {
        super(FXML);

        this.shiftObservableList = shiftObservableList;
        this.outletInformation = outletInformation;

        timetableView = new CalendarView();
        setTimetableViewStyle();
        showRelevantViewsOnly();

        // disable clicks on timetable view
        timetableView.getWeekPage().setMouseTransparent(true);

        updateTimetableView();

        registerAsAnEventHandler(this);
    }

    public CalendarView getRoot() {
        return this.timetableView;
    }

    public static Calendar getTimetableAvail() {
        return timetableAvail;
    }

    public static Calendar getTimetableRunningOut() {
        return timetableRunningOut;
    }

    public static Calendar getTimetableFull() {
        return timetableFull;
    }

    public static Calendar getTimetableEmployee() {
        return timetableEmployee;
    }

    public static Calendar getTimetableOthers() {
        return timetableOthers;
    }

    /**
     * Sets the style of the timetable view
     */
    private void setTimetableViewStyle() {
        ObservableList<String> styleClass = timetableView.getStyleClass();
        styleClass.add("timetable-container");
    }

    /**
     * Only show the parts of CalendarFX that we need.
     */
    private void showRelevantViewsOnly() {
        timetableView.showWeekPage();

        timetableView.getWeekPage().setShowNavigation(false);
        timetableView.getWeekPage().setShowDate(false);
        timetableView.weekFieldsProperty().setValue(WeekFields.of(Locale.FRANCE)); // Start week from Monday
        timetableView.setShowToday(true);
        timetableView.setShowPrintButton(true);
        timetableView.setShowAddCalendarButton(false);
        timetableView.setShowSearchField(false);
        timetableView.setShowToolBar(false);
        timetableView.setShowPageSwitcher(false);
        timetableView.setShowPageToolBarControls(false);
        timetableView.setShowSearchResultsTray(false);
        timetableView.setShowSourceTray(false);
        timetableView.setShowSourceTrayButton(false);
        timetableView.getWeekPage().getDetailedWeekView().setShowAllDayView(false);
    }

    /**
     * This ensures that the range of the times shown by the timetable view is constrained to the
     * operating hours of the outlet.
     * Also ensures that no scrolling is required to view the entire timetable.
     */
    private void setTimetableRange() {
        LocalTime startTime = outletInformation.getOperatingHours().getStartTime();
        LocalTime endTime = outletInformation.getOperatingHours().getEndTime();
        timetableView.setStartTime(startTime);
        timetableView.setEndTime(endTime);

        DetailedWeekView detailedWeekView = timetableView.getWeekPage().getDetailedWeekView();
        detailedWeekView.setEarlyLateHoursStrategy(DayViewBase.EarlyLateHoursStrategy.HIDE);
        detailedWeekView.setHoursLayoutStrategy(DayViewBase.HoursLayoutStrategy.FIXED_HOUR_COUNT);
        detailedWeekView.setVisibleHours((int) ChronoUnit.HOURS.between(startTime, endTime));
        detailedWeekView.setShowScrollBar(false);
        detailedWeekView.setEnableCurrentTimeMarker(false);
    }

    private void setCurrentTime() {
        timetableView.setToday(LocalDate.now());
    }

    /**
     * Takes default outlet shifts and set timetable entries based on these shifts.
     */
    private void setShifts() {
        int index = 1;
        for (Shift shift: shiftObservableList) {
            LocalDate date = shift.getDate().getLocalDate();
            Interval timeInterval = new Interval(date, shift.getStartTime().getLocalTime(),
                    date, shift.getEndTime().getLocalTime());
            Entry<String> shiftEntry = new Entry<>("SHIFT " + index + "\nSlots left: " + shift.getSlotsLeft()
                    + "/" + shift.getCapacity().getCapacity(), timeInterval);
            setEntryType(shift, shiftEntry);
            index++;
        }
    }

    /**
     * Sets the entry type (aka the color) of the shift in the timetable
     * @param shift
     * @param shiftEntry
     */
    private void setEntryType(Shift shift, Entry<String> shiftEntry) {
        Calendar entryType;
        if (currentEmployee != null) {
            entryType = getEntryTypeEmployee(shift);
        } else {
            entryType = getEntryTypeMain(shift);
        }
        entryType.addEntry(shiftEntry);
    }

    /**
     * Checks if currentEmployee is in input shift
     * @param shift
     * @return true if currentEmployee is in input shift, false if not.
     */
    private boolean isCurrentEmployeeInShift(Shift shift) {
        UniqueEmployeeList employees = shift.getUniqueEmployeeList();
        for (Employee employee : employees) {
            if (employee.equals(currentEmployee)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the entryType (a Calendar object) for the shift in the main timetable view, which reflects
     * the color of the shift in the timetableView.
     */
    private Calendar getEntryTypeMain(Shift shift) {
        float ratio = (float) shift.getSlotsLeft() / (float) shift.getCapacity().getCapacity();
        if (ratio <= 0) {
            return timetableFull;
        } else if (ratio <= 0.5 || shift.getCapacity().getCapacity() < MAX_SLOTS_LEFT_RUNNING_OUT) {
            return timetableRunningOut;
        } else {
            return timetableAvail;
        }
    }

    /**
     * @return the entryType (a Calendar object) for the shift in the employee timetable view, which reflects
     * the color of the shift in the timetableView.
     */
    private Calendar getEntryTypeEmployee(Shift shift) {
        if (isCurrentEmployeeInShift(shift)) {
            return timetableEmployee;
        } else {
            return timetableOthers;
        }
    }

    /**
     * Replaces the timetable view with a new timetable, with shifts taken by the employee being highlighted
     * @param employee
     */
    private void loadEmployeeTimetable(Employee employee) {
        currentEmployee = employee;
        updateTimetableView();
    }

    private void loadMainTimetable() {
        currentEmployee = null;
        updateTimetableView();
    }

    /**
     * Replaces timetableView with a new timetable with updated shift and outlet information
     */
    private void updateTimetableView() {
        setCurrentTime();
        timetableView.getCalendarSources().clear();
        CalendarSource calendarSource = new CalendarSource("Shifts");
        addCalendars(calendarSource);

        setShifts();
        timetableView.getCalendarSources().add(calendarSource);

        setTimetableRange();
    }

    /**
     * Initialises all the Calendar objects
     */
    private void initialiseEntries() {
        timetableAvail = new Calendar("Available");
        timetableRunningOut = new Calendar("Running Out");
        timetableFull = new Calendar("Full");
        timetableEmployee = new Calendar("Employee's shift");
        timetableOthers = new Calendar("Other shifts");
    }

    /**
     * Sets the color styles of the entries
     */
    private void setEntryStyles() {
        timetableAvail.setStyle(ENTRY_GREEN_STYLE);
        timetableRunningOut.setStyle(ENTRY_YELLOW_STYLE);
        timetableFull.setStyle(ENTRY_RED_STYLE);
        timetableEmployee.setStyle(ENTRY_BLUE_STYLE);
        timetableOthers.setStyle(ENTRY_BROWN_STYLE);
    }

    /**
     * Adds all relevant Calendars (entryTypes) to its source
     */
    private void addCalendars(CalendarSource calendarSource) {
        initialiseEntries();
        setEntryStyles();
        calendarSource.getCalendars().addAll(timetableAvail, timetableRunningOut, timetableFull,
                timetableEmployee, timetableOthers);
    }

    /**
     * Takes a snapshot of the timetable view
     */
    private WritableImage takeSnapshot() {
        WritableImage timetableWritableImage = new WritableImage(
                (int) (TIMETABLE_IMAGE_PIXEL_SCALE * timetableView.getWidth()),
                (int) (TIMETABLE_IMAGE_PIXEL_SCALE * timetableView.getHeight()));
        SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(Transform.scale(TIMETABLE_IMAGE_PIXEL_SCALE, TIMETABLE_IMAGE_PIXEL_SCALE));
        WritableImage snapshot = timetableView.snapshot(spa, timetableWritableImage);
        return snapshot;
    }

    /**
     * Exports timetable as image and save it locally
     */
    private void exportTimetableAsImage(String filename) {
        File imageFile = new File("." + File.separator + filename + "." + TIMETABLE_IMAGE_FILE_FORMAT);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(takeSnapshot(), null), TIMETABLE_IMAGE_FILE_FORMAT, imageFile);
        } catch (IOException e) {
            logger.warning("Error taking snapshot of timetable.");
        }
    }

    /**
     * Exports timetable as image and email it
     * @param email
     */
    private void exportTimetableAsImageAndEmail(String filename, Email email) {
        String pathName = "." + File.separator + filename + "." + TIMETABLE_IMAGE_FILE_FORMAT;
        File imageFile = new File(pathName);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(takeSnapshot(), null), TIMETABLE_IMAGE_FILE_FORMAT, imageFile);
            EmailService emailService = EmailService.getInstance();
            emailService.sendTimetableAttachment(email.toString(), pathName);
        } catch (IOException e) {
            logger.warning("Error taking snapshot of timetable.");
        } catch (MessagingException e) {
            logger.warning("Error sending timetable as email.");
        }

        try {
            Files.deleteIfExists(Paths.get(pathName));
        } catch (IOException e) {
            logger.warning("Error deleting exported and emailed timetable image.");
        }
    }

    @Subscribe
    private void handleShiftChangedEvent(PartTimeManagerChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event) + ": Updating timetable view....");
        Platform.runLater(() -> updateTimetableView());
    }

    @Subscribe
    private void handleEmployeePanelSelectionChangedEvent(EmployeePanelSelectionChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        Platform.runLater(() -> {
            if (event.hasNewSelection()) {
                loadEmployeeTimetable(event.getNewSelection().employee);
            } else {
                loadMainTimetable();
            }
        });
    }

    @Subscribe
    private void handleExportTimetableAsImageRequestEvent(ExportTimetableAsImageRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event) + ": Exporting timetable as image....");
        Platform.runLater(() -> exportTimetableAsImage(event.filename));
    }

    @Subscribe
    private void handleExportTimetableAsImageAndEmailRequestEvent(ExportTimetableAsImageAndEmailRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event)
                + ": Exporting timetable as image to send email....");
        Platform.runLater(() -> exportTimetableAsImageAndEmail(event.filename, event.email));
    }

}
```
###### \resources\view\AdminModeDisplay.fxml
``` fxml
<StackPane styleClass="anchor-pane" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
    <Label fx:id="adminModeDisplay" />
</StackPane>
```
###### \resources\view\EmployeeListCard.fxml
``` fxml
      <StackPane minWidth="45" prefWidth="45" maxWidth="45">
        <Circle fx:id="idCircle" radius="18" fill="transparent" stroke="#5AC2BC"/>
        <Label fx:id="id" styleClass="cell_big_label"/>
      </StackPane>
```
###### \resources\view\MainWindow.fxml
``` fxml
        <HBox styleClass="pane-with-border">
          <StackPane VBox.vgrow="NEVER" fx:id="ptmanLogoDisplayPlaceholder" maxWidth="50">
            <padding>
              <Insets top="5" right="10" bottom="5" left="10" />
            </padding>
          </StackPane>

          <StackPane VBox.vgrow="NEVER" HBox.hgrow="ALWAYS" fx:id="commandBoxPlaceholder">
            <padding>
              <Insets top="5" right="0" bottom="5" left="0" />
            </padding>
          </StackPane>

          <StackPane VBox.vgrow="NEVER" HBox.hgrow="NEVER" fx:id="adminModeDisplayPlaceholder"
                      minWidth="115" prefWidth="115" maxWidth="115">
            <padding>
              <Insets top="5" right="10" bottom="5" left="10" />
            </padding>
          </StackPane>
        </HBox>

        <SplitPane id="splitPane" fx:id="splitPane" dividerPositions="0.8" VBox.vgrow="ALWAYS">
          <VBox>
            <StackPane VBox.vgrow="NEVER" fx:id="resultDisplayPlaceholder" styleClass="pane-with-dark-background"
                       minHeight="100" prefHeight="100" maxHeight="100">
              <padding>
                <Insets top="10" right="15" bottom="10" left="15" />
              </padding>
            </StackPane>

            <StackPane VBox.vgrow="NEVER" fx:id="outletDetailsPanelPlaceholder" >
              <padding>
                <Insets top="5" right="15" bottom="5" left="15" />
              </padding>
            </StackPane>

            <StackPane fx:id="timetableViewPlaceholder" prefWidth="340">
              <padding>
                <Insets top="5" right="15" bottom="10" left="15" />
              </padding>
            </StackPane>
          </VBox>

          <VBox fx:id="employeeList" minWidth="260" SplitPane.resizableWithParent="false">
            <StackPane fx:id="employeeListPanelPlaceholder" VBox.vgrow="ALWAYS"/>
          </VBox>

        </SplitPane>

        <StackPane fx:id="statusbarPlaceholder" VBox.vgrow="NEVER" />
      </VBox>
    </Scene>
  </scene>
</fx:root>
```
###### \resources\view\OutletDetailsPanel.fxml
``` fxml
<StackPane xmlns="http://javafx.com/javafx/8.0.121" xmlns:fx="http://javafx.com/fxml/1">
    <VBox>
        <Label fx:id="outletNamePanelHeader" styleClass="panel_large_label" />
        <HBox fx:id="outletInformation">
            <Label fx:id="operatingHours"><graphic><FontAwesomeIconView fill="#7A8492" glyphName="CALENDAR" /></graphic></Label>
            <Label fx:id="outletContact"><graphic><FontAwesomeIconView fill="#7A8492" glyphName="PHONE" /></graphic></Label>
            <Label fx:id="outletEmail"><graphic><FontAwesomeIconView fill="#7A8492" glyphName="ENVELOPE" /></graphic></Label>
        </HBox>
        <Label fx:id="announcement"><graphic><FontAwesomeIconView fill="#7A8492" glyphName="BULLHORN" /></graphic></Label>
    </VBox>
</StackPane>
```
###### \resources\view\PtmanLogoDisplay.fxml
``` fxml
<StackPane xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1">
    <ImageView fx:id="ptmanLogoView" />
</StackPane>
```
###### \resources\view\PtmanTheme.css
``` css
/* Fonts */
@font-face {
    src: url("/fonts/Gotham-Rounded-Book.ttf");
}

@font-face {
    src: url("/fonts/Gotham-Rounded-Medium.ttf");
}

@font-face {
    font-family: "Proxima Nova Alt";
    src: url("/fonts/Proxima-Nova-Alt-Regular.ttf");
    font-weight: normal;
    font-style: normal;
}

@font-face {
    font-family: "Proxima Nova Alt";
    src: url("/fonts/Proxima-Nova-Alt-Bold.ttf");
    font-weight: bold;
    font-style: normal;
}


/* Classes */
.root {
    -fx-accent: #8E9AAA;
    -fx-focus-color: #8E9AAA;
}

.background {
    -fx-background-color: #FFFFFF;
    background-color: #FFFFFF; /* Used in the default.html file */
}

.label {
    -fx-font-size: 11pt;
    -fx-font-family: "Gotham Rounded Book";
    -fx-text-fill: #7A8492;
    -fx-opacity: 0.9;
}

.label-bright {
    -fx-font-size: 11pt;
    -fx-font-family: "Gotham Rounded Book";
    -fx-text-fill: white;
    -fx-opacity: 1;
}

.label-header {
    -fx-font-size: 32pt;
    -fx-font-family: "Gotham Rounded Book";
    -fx-text-fill: white;
    -fx-opacity: 1;
}

.label-admin-mode {
    -fx-hgap: 7;
    -fx-vgap: 3;
    -fx-background-color: #E16A66;
    -fx-text-fill: white;
    -fx-padding: 5 5 5 5;
    -fx-border-radius: 2;
    -fx-background-radius: 2;
    -fx-font-size: 12;
}

.label-non-admin-mode {
    -fx-hgap: 7;
    -fx-vgap: 3;
    -fx-text-fill: transparent;
    -fx-padding: 2 4 2 4;
    -fx-border-radius: 2;
    -fx-background-radius: 2;
    -fx-font-size: 12;
}

.text-field {
    -fx-font-size: 12pt;
    -fx-font-family: "Gotham Rounded Book";
}

.tab-pane {
    -fx-padding: 0 0 0 1;
}

.tab-pane .tab-header-area {
    -fx-padding: 0 0 0 0;
    -fx-min-height: 0;
    -fx-max-height: 0;
}

.table-view {
    -fx-base: #FFFFFF;
    -fx-control-inner-background: #FFFFFF;
    -fx-background-color: #FFFFFF;
    -fx-table-cell-border-color: transparent;
    -fx-table-header-border-color: transparent;
    -fx-padding: 5;
}

.table-view .column-header-background {
    -fx-background-color: transparent;
}

.table-view .column-header, .table-view .filler {
    -fx-size: 35;
    -fx-border-width: 0 0 1 0;
    -fx-background-color: transparent;
    -fx-border-color:
        transparent
        transparent
        #FFFFFF
        transparent;
    -fx-border-insets: 0 10 1 0;
}

.table-view .column-header .label {
    -fx-font-size: 20pt;
    -fx-font-family: "Proxima Nova Alt";
    -fx-font-weight: bold;
    -fx-text-fill: white;
    -fx-alignment: center-left;
}

.table-view:focused .table-row-cell:filled:focused:selected {
    -fx-background-color: -fx-focus-color;
}

.split-pane:horizontal .split-pane-divider {
    -fx-background-color: #DDDEE4;
    -fx-padding: 1;
}

.split-pane {
    -fx-background-color: #FFFFFF;
    -fx-background-insets: 0;
    -fx-padding: 0;
}

.list-view {
    -fx-background-insets: 0;
    -fx-padding: 0;
    -fx-background-color: #FFFFFF;
}

.list-cell {
    -fx-label-padding: 12 6 12 6;
    -fx-graphic-text-gap : 0;
    -fx-padding: 0 0 0 0;
    -fx-background-color: #FFFFFF;
}

.list-cell:filled {
    -fx-background-color: #FFFFFF;
    -fx-border-color: transparent transparent #DDDEE4 transparent;
    -fx-border-width: 1px;
}

.list-cell:filled:selected {
    -fx-background-color: #F5F8FA;
}

.list-cell:filled:selected #idCircle {
    -fx-fill: #5AC2BC;
}

.list-cell:filled:selected #id {
    -fx-text-fill: white;
    -fx-font-weight: bold;
}

.list-cell .label {
    -fx-text-fill: #7A8492;
}

.panel_medium_text {
    -fx-font-family: "Proxima Nova Alt";
    -fx-font-size: 13px;
    -fx-text-fill: white;
}

.panel_big_label {
    -fx-font-family: "Proxima Nova Alt";
    -fx-font-weight: bold;
    -fx-font-size: 16px;
    -fx-text-fill: white;
    -fx-padding: 10 15 10 20;
}

.panel_large_label {
    -fx-font-family: "Proxima Nova Alt";
    -fx-font-weight: bold;
    -fx-font-size: 26px;
    -fx-font-weight: bold;
    -fx-text-fill: #5AC2BC;
    -fx-padding: 18 5 5 15;
}

.cell_big_label {
    -fx-font-family: "Gotham Rounded Medium";
    -fx-font-size: 16px;
    -fx-text-fill: #010504;
}

.cell_small_label {
    -fx-font-family: "Gotham Rounded Book";
    -fx-font-size: 12px;
    -fx-padding: 2 0 0 0;
    -fx-text-fill: #010504;
}

.anchor-pane {
     -fx-background-color: #FFFFFF;
}

.pane-with-border {
     -fx-background-color: #FFFFFF;
     -fx-border-color: transparent transparent #DDDEE4 transparent;
     -fx-border-top-width: 1px;
}

.pane-with-dark-background {
     -fx-background-color: #F5F8FA;
}

.pane-with-mint-background {
    -fx-background-color: #5AC2BC;
}

.status-bar {
    -fx-background-color: #EFF2F5;
}

.result-display {
    -fx-background-color: transparent;
    -fx-font-family: "Gotham Rounded Book";
    -fx-font-size: 15px;
    -fx-text-fill: #7A8492;
}

.status-bar .label {
    -fx-font-family: "Proxima Nova Alt";
    -fx-text-fill: #7A8492;
}

.status-bar-with-border {
    -fx-background-color: #EFF2F5;
    -fx-border-color: #EFF2F5;
    -fx-border-width: 1px;
}

.status-bar-with-border .label {
    -fx-text-fill: white;
}

.grid-pane {
    -fx-background-color: #EFF2F5;
}

.grid-pane .anchor-pane {
    -fx-padding: 5 10 5 10;
}

.context-menu {
    -fx-background-color: #EFF2F5;
}

.context-menu .label {
    -fx-text-fill: #7A8492;
}

.menu-bar {
    -fx-background-color: #EFF2F5;
}

.menu-bar .label {
    -fx-font-size: 15px;
    -fx-font-family: "Gotham Rounded Book";
    -fx-text-fill: #7A8492;
}

.menu-button:hover,
.menu-button:focused,
.menu-button:showing {
    -fx-background-color: #5AC2BC;
}

.menu-button:hover > .label,
.menu-button:focused > .label,
.menu-button:showing > .label {
    -fx-text-fill: white;
}

.menu-item:hover,
.menu-item:focused {
    -fx-background-color: #EFF2F5;
    -fx-cursor: hand;
}

.menu-item:hover > .label,
.menu-item:focused > .label {
    -fx-text-fill: #7A8492;
}

.menu .left-container {
    -fx-background-color: black;
}

/*
 * Metro style Push Button
 * Author: Pedro Duque Vieira
 * http://pixelduke.wordpress.com/2012/10/23/jmetro-windows-8-controls-on-java/
 */
.button {
    -fx-padding: 5 22 5 22;
    -fx-border-color: #e2e2e2;
    -fx-border-width: 2;
    -fx-background-radius: 0;
    -fx-background-color: #FFFFFF;
    -fx-font-family: "Proxima Nova Alt", "Segoe UI", Helvetica, Arial, sans-serif;
    -fx-font-size: 11pt;
    -fx-text-fill: #d8d8d8;
    -fx-background-insets: 0 0 0 0, 0, 1, 2;
}

.button:hover {
    -fx-background-color: #3a3a3a;
}

.button:pressed, .button:default:hover:pressed {
  -fx-background-color: white;
  -fx-text-fill: #FFFFFF;
}

.button:focused {
    -fx-border-color: white, white;
    -fx-border-width: 1, 1;
    -fx-border-style: solid, segments(1, 1);
    -fx-border-radius: 0, 0;
    -fx-border-insets: 1 1 1 1, 0;
}

.button:disabled, .button:default:disabled {
    -fx-opacity: 0.4;
    -fx-background-color: #FFFFFF;
    -fx-text-fill: white;
}

.button:default {
    -fx-background-color: -fx-focus-color;
    -fx-text-fill: #ffffff;
}

.button:default:hover {
    -fx-background-color: #EFF2F5;
}

.dialog-pane {
    -fx-background-color: #FFFFFF;
}

.dialog-pane > *.button-bar > *.container {
    -fx-background-color: #FFFFFF;
}

.dialog-pane > *.label.content {
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-text-fill: white;
}

.dialog-pane:header *.header-panel {
    -fx-background-color: #FFFFFF;
}

.dialog-pane:header *.header-panel *.label {
    -fx-font-size: 18px;
    -fx-font-style: italic;
    -fx-fill: white;
    -fx-text-fill: white;
}

.scroll-bar {
    -fx-background-color: #EFF2F5;
}

.scroll-bar .thumb {
    -fx-background-color: #CDCDCD;
    -fx-background-insets: 3;
}

.scroll-bar .increment-button, .scroll-bar .decrement-button {
    -fx-background-color: transparent;
    -fx-padding: 0 0 0 0;
}

.scroll-bar .increment-arrow, .scroll-bar .decrement-arrow {
    -fx-shape: " ";
}

.scroll-bar:vertical .increment-arrow, .scroll-bar:vertical .decrement-arrow {
    -fx-padding: 1 8 1 8;
}

.scroll-bar:horizontal .increment-arrow, .scroll-bar:horizontal .decrement-arrow {
    -fx-padding: 8 1 8 1;
}

.timetable-container {
    -fx-opacity: 0.8;
}


/* Identities */
#outletInformation {
    -fx-padding: 5 5 3 20;
}

#outletInformation .label {
    -fx-font-family: "Gotham Rounded Book";
    -fx-font-size: 14px;
    -fx-text-fill: #7A8492;
}

#announcement {
    -fx-font-family: "Gotham Rounded Book";
    -fx-font-size: 14px;
    -fx-text-fill: #7A8492;
    -fx-padding: 0 5 10 20;
}

#cardPane {
    -fx-background-color: transparent;
    -fx-border-width: 0;
}

#id {
    -fx-font-family: "Proxima Nova Alt";
    -fx-text-fill: #5AC2BC;
}

#commandTypeLabel {
    -fx-font-size: 11px;
    -fx-text-fill: #7A8492;
}


#commandTextField {
    -fx-background-color: transparent #FFFFFF transparent #FFFFFF;
    -fx-background-insets: 0;
    -fx-border-insets: 0;
    -fx-font-family: "Gotham Rounded Book";
    -fx-font-size: 16px;
    -fx-text-fill: #7A8492;
}

#resultDisplay .content {
    -fx-background-color: #F5F8FA;
    -fx-background-radius: 0;
}

#tags {
    -fx-hgap: 8;
    -fx-vgap: 3;
    -fx-padding: 5 0 3 0;
}

#tags .label {
    -fx-text-fill: white;
    -fx-padding: 3 4 3 4;
    -fx-border-radius: 2;
    -fx-background-radius: 2;
    -fx-font-size: 11;
}

#tags .salmon {
    -fx-background-color: #E57373;
}

#tags .mint {
    -fx-background-color: #80CBC4;
}

#tags .teal {
    -fx-background-color: #26A69A;
}

#tags .turquoise {
    -fx-background-color: #4DD0E1;
}

#tags .pink {
    -fx-background-color: #F06292;
}

#tags .blue {
    -fx-background-color: #42A5F5;
}

#tags .purple {
    -fx-background-color: #9575CD;
}

#tags .pale-blue {
    -fx-background-color: #82B1FF;
}

#tags .indigo {
    -fx-background-color: #5C6BC0;
}

#tags .yellow {
    -fx-background-color: #FFC107;
}
```
###### \resources\view\TimetableView.fxml
``` fxml
<StackPane xmlns="http://javafx.com/javafx/8.0.111">
    <children>
        <BorderPane prefHeight="200.0" prefWidth="200.0" />
    </children>
</StackPane>
```
