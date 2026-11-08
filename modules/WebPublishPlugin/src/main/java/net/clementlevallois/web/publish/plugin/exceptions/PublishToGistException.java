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
public class PublishToGistException extends RuntimeException {

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

    public PublishToGistException(String errorCode, String errorBody) {

        super(bundle.getString("general.message.error.gist_creation")
                + errorCode
                + "; "
                + bundle.getString("general.message.error_message")
                + errorBody);
    }

    public PublishToGistException(Throwable throwable) {
        super(bundle.getString("general.message.error.gist_creation")
            + ""
            + "; "
            + bundle.getString("general.message.error_message")
            + throwable.getMessage(), throwable);
    }
}
