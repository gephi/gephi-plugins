/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.web.publish.plugin.exceptions;

import java.util.ResourceBundle;
import net.clementlevallois.web.publish.plugin.controller.WebPublishExporterUI;
import org.openide.util.NbBundle;

/**
 *
 * @author LEVALLOIS
 */
public class EmptyGraphException extends RuntimeException {

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

    public EmptyGraphException() {
        super(bundle.getString("general.message.error.empty_network"));
    }

}
