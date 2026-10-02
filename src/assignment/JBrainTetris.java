package assignment;

import javax.swing.*;
import java.awt.*;
import java.awt.Container;
import javax.swing.Box;
import javax.swing.JCheckBox;
import assignment.Board.Action;

public class JBrainTetris extends JTetris {
    private Brain brain;
    private JCheckBox brainMode;

    public JBrainTetris() {
        super();
        this.brain = new TetrisBrain();
    }

    @Override
    public void tick(Action verb) {
        if (brainMode.isSelected()&& board.getCurrentPiece() != null) {
            Action nextMove = brain.nextMove(board);
            if (nextMove != null && nextMove != Action.NOTHING){
                super.tick(nextMove);
            }
            else{
                super.tick(verb);
            }
        }
        else {
            super.tick(verb);
        }
    }

    @Override
    public Container createControlPanel() {
        Container panel = super.createControlPanel();
        panel.add(Box.createVerticalStrut(12));
        brainMode = new JCheckBox("Brain Active");
        panel.add(brainMode);
        return panel;
    }

    public static void main(String[] args) {
        createGUI(new JBrainTetris());
    }

    
    
}

