/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic;

/**
 *
 * @author edemairy
 */
public enum PluginProperties {
    RESET_WORKSPACE("semanticwebimport.reset_workspace"),
    POST_PROCESSING("semanticwebimport.post_processing"),
    IGNORE_BLANK_PROPERTIES("semanticwebimport.ignore_blank_properties"),
    SAVE_SPARQL_RESULT("semanticwebimport.save_sparql_result"),
    FYN_LEVEL("semanticwebimport.fyn_level");

    private PluginProperties(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    private String value;
}
