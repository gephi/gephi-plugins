/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.streaming;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class ConnectToStreamAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {

//        DesktopStreamingControllerUI controller = new DesktopStreamingControllerUI();
//        controller.connectToStream();
        
        DesktopStreamingController controller = new DesktopStreamingController();
        controller.exposeWorkspaceAsStream();

    }
}
