/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.blueskygephi.atproto.response;

import fr.totetmatt.blueskygephi.atproto.response.common.Subject;
import java.util.List;

/**
 *
 * @author totetmatt
 */
public class AppBskyGraphGetList {
    private List<Subject> items;
    private String cursor;
    
    
    public List<Subject> getItems() {
        return items;
    }

    public void setItems(List<Subject> items) {
        this.items = items;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
    
    
    
    
}
