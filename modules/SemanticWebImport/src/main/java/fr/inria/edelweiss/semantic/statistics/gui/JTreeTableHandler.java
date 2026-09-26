/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
class JTreeTableHandler extends TransferHandler {

    public JTreeTableHandler() {
    }

    public boolean canImport(TransferHandler.TransferSupport info) {
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }
        return true;
    }

    public Transferable createTransferable(JComponent c) {
        JTreeTable table = (JTreeTable) c;
        int numRow = table.getSelectedRow();
        String cellName = table.getCell(numRow, 0);
        return new StringSelection(cellName);
    }


     public int getSourceActions(JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }
}
