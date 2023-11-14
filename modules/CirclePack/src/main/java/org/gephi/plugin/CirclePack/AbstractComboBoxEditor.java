package org.gephi.plugin.CirclePack;/*
 * Copyright (c) 2010, Matt Groeninger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.beans.PropertyEditorSupport;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Matt
 */
public abstract class AbstractComboBoxEditor extends PropertyEditorSupport {

    public Map<String, String> comboValues;

    @Override
    public String[] getTags() {
        return comboValues.values().toArray(new String[0]);
    }

    @Override
    public String getAsText() {
        if (getValue() == null) {
            return "No Selection";
        }
        return comboValues.get(getValue().toString());
    }

    @Override
    public void setAsText(String s) {
        Set<Map.Entry<String, String>> Entries = comboValues.entrySet();
        for (Map.Entry<String, String> Entry : Entries) {
            if ((Entry.getValue() == null) ? (s == null) : Entry.getValue().equals(s)) {
                setValue(Entry.getKey());
            }
        }
    }
};
