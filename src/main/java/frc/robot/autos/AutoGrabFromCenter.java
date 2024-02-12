package frc.robot.autos;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.lib.bluecrew.util.FieldState;
import frc.lib.bluecrew.util.RobotState;
import frc.robot.commands.IntakeNote;

public class AutoGrabFromCenter extends SequentialCommandGroup {

    private final int[] orderOfCenterNotes;
    private final String autoLane;
    private final String comingFrom;

    public AutoGrabFromCenter(int[] orderOfCenterNotes, String comingFrom,  String autoLane) {
        // Save the inputs for later
        this.orderOfCenterNotes = orderOfCenterNotes;
        this.comingFrom = comingFrom;
        this.autoLane = autoLane;
    }

    @Override
    public void initialize() {
        // When this command is scheduled add the commands we want to do.
        // This must be done when the command is scheduled, and is only possible
        // because this class overrides the custom SequentialCommandGroup class in this package
        addCommands(
                // Look for a note until we see that one is available
                new FindCenterPiece(orderOfCenterNotes, comingFrom, autoLane).until(() -> RobotState.getInstance().isAutoPieceIsAvailable()),
                // Follow the path to the note we are in front of until the path ends, or we pick up a note,
                // but only if AutoPieceIsAvailable is true
                (new AutoFollowNumberedNotePath("CN", () -> FieldState.getInstance().getCenterNoteIndex(), "Intake")
                        // Race with IntakeNote command
                        .raceWith(new IntakeNote())
                        // At the same time, set the piece availability to false
                        .alongWith(new InstantCommand(() -> RobotState.getInstance().setAutoPieceIsAvailable(false)))
                        // And set that the note we are in front of no longer exists (because we are picking it up
                        .alongWith(new InstantCommand(() -> FieldState.getInstance().setCenterNoteExists(
                                FieldState.getInstance().getCenterNoteIndex()-1, false)))
                // Only if a piece is available
                ).onlyIf(() -> RobotState.getInstance().isAutoPieceIsAvailable())
        );

        // THIS IS SUPER IMPORTANT, this code is needed to start the commands going,
        // this is normally done automatically with WPILib's SequentialCommandGroup,
        // but since we are using the custom one, we have to manually do this
        this.m_currentCommandIndex = 0;
        if (!this.m_commands.isEmpty()) {
            ((Command)this.m_commands.get(0)).initialize();
        }
    }
}
