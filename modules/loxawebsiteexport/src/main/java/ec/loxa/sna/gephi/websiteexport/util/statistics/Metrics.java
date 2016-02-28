/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.loxa.sna.gephi.websiteexport.util.statistics;

/**
 *
 * @author jorgaf
 */
public class Metrics {
    private String name;
    private String value;
    private String description;
    
    public Metrics(){
        name = "";
        value = "";
        description = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    
}