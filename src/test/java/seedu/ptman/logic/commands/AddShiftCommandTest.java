package seedu.ptman.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static seedu.ptman.testutil.TypicalShifts.MONDAY_AM;
import static seedu.ptman.testutil.TypicalShifts.MONDAY_PM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javafx.collections.ObservableList;
import seedu.ptman.logic.CommandHistory;
import seedu.ptman.logic.UndoRedoStack;
import seedu.ptman.logic.commands.exceptions.CommandException;
import seedu.ptman.model.Model;
import seedu.ptman.model.PartTimeManager;
import seedu.ptman.model.ReadOnlyPartTimeManager;
import seedu.ptman.model.employee.Employee;
import seedu.ptman.model.employee.exceptions.DuplicateEmployeeException;
import seedu.ptman.model.employee.exceptions.EmployeeNotFoundException;
import seedu.ptman.model.outlet.Shift;
import seedu.ptman.model.outlet.exceptions.DuplicateShiftException;
import seedu.ptman.model.tag.Tag;
import seedu.ptman.testutil.ShiftBuilder;

public class AddShiftCommandTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructor_nullShift_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new AddShiftCommand(null);
    }

    @Test
    public void execute_shiftAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingShiftAdded modelStub = new ModelStubAcceptingShiftAdded();
        Shift validShift = new ShiftBuilder().build();

        CommandResult commandResult = getAddShiftCommandForShift(validShift, modelStub).execute();

        assertEquals(String.format(AddShiftCommand.MESSAGE_SUCCESS, validShift), commandResult.feedbackToUser);
        assertEquals(Arrays.asList(validShift), modelStub.shiftsAdded);
    }

    @Test
    public void execute_duplicateShift_throwsCommandException() throws Exception {
        ModelStub modelStub = new ModelStubThrowingDuplicateShiftException();
        Shift validShift = new ShiftBuilder().build();

        thrown.expect(CommandException.class);
        thrown.expectMessage(AddShiftCommand.MESSAGE_DUPLICATE_SHIFT);

        getAddShiftCommandForShift(validShift, modelStub).execute();
    }

    @Test
    public void equals() {
        AddShiftCommand addMondayAmCommand = new AddShiftCommand(MONDAY_AM);
        AddShiftCommand addMondayPmCommand = new AddShiftCommand(MONDAY_PM);

        // same object -> returns true
        assertTrue(addMondayAmCommand.equals(addMondayAmCommand));

        // same values -> returns true
        AddShiftCommand addMondayAmCommandCopy = new AddShiftCommand(MONDAY_AM);
        assertTrue(addMondayAmCommand.equals(addMondayAmCommandCopy));

        // different types -> returns false
        assertFalse(addMondayAmCommand.equals(1));

        // null -> returns false
        assertFalse(addMondayAmCommand.equals(null));

        // different employee -> returns false
        assertFalse(addMondayAmCommand.equals(addMondayPmCommand));
    }

    /**
     * Generates a new AddCommand with the details of the given employee.
     */
    private AddShiftCommand getAddShiftCommandForShift(Shift shift, Model model) {
        AddShiftCommand command = new AddShiftCommand(shift);
        command.setData(model, new CommandHistory(), new UndoRedoStack());
        return command;
    }

    /**
     * A default model stub that have all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public void addEmployee(Employee employee) throws DuplicateEmployeeException {
            fail("This method should not be called.");
        }

        @Override
        public void addShift(Shift shift) throws DuplicateShiftException {
            fail("This method should not be called.");
        }

        public boolean isAdmin(String password) {
            fail("This method should not be called.");
            return true;
        }

        @Override
        public void deleteTagFromAllEmployee(Tag tag) {
            fail("This method should not be called");
        }

        @Override
        public void resetData(ReadOnlyPartTimeManager newData) {
            fail("This method should not be called.");
        }

        @Override
        public ReadOnlyPartTimeManager getPartTimeManager() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void deleteEmployee(Employee target) throws EmployeeNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void updateEmployee(Employee target, Employee editedEmployee)
                throws DuplicateEmployeeException {
            fail("This method should not be called.");
        }

        @Override
        public ObservableList<Employee> getFilteredEmployeeList() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void updateFilteredEmployeeList(Predicate<Employee> predicate) {
            fail("This method should not be called.");
        }
    }

    /**
     * A Model stub that always throw a DuplicateEmployeeException when trying to add an employee.
     */
    private class ModelStubThrowingDuplicateShiftException extends ModelStub {
        @Override
        public void addShift(Shift shift) throws DuplicateShiftException {
            throw new DuplicateShiftException();
        }

        @Override
        public ReadOnlyPartTimeManager getPartTimeManager() {
            return new PartTimeManager();
        }
    }

    /**
     * A Model stub that always accept the employee being added.
     */
    private class ModelStubAcceptingShiftAdded extends ModelStub {
        final ArrayList<Shift> shiftsAdded = new ArrayList<>();

        @Override
        public void addShift(Shift shift) throws DuplicateShiftException {
            requireNonNull(shift);
            shiftsAdded.add(shift);
        }

        @Override
        public ReadOnlyPartTimeManager getPartTimeManager() {
            return new PartTimeManager();
        }
    }

}