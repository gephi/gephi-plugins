/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hypercities.exporttoearth;

import org.openide.util.NbBundle;

/**
 *
 * @author daveshepard
 */
public class BundleAccessor {

    private final Class class_;

    private BundleAccessor(Class c) {
        this.class_ = c;
    }

    public static BundleAccessor forClass(Class c) {
        return new BundleAccessor(c);
    }

    public String get(String name) {
        return NbBundle.getMessage(class_, name);
    }
    
}
