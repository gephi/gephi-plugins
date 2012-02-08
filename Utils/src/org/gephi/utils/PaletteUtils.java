/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.utils;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Mathieu Bastian
 */
public class PaletteUtils {

    /**
     * Return different colors
     * @param num
     * @return
     */
    public static List<Color> getSequenceColors(int num) {
        List<Color> colors = new LinkedList<Color>();

        //On choisit H et S au random
        Random random = new Random();
        float B = random.nextFloat() * 2 / 5f + 0.6f;		//		0.6 <=   B   < 1
        float S = random.nextFloat() * 2 / 5f + 0.6f;		//		0.6 <=   S   < 1
        //System.out.println("B : "+B+"  S : "+S);

        for (int i = 1; i <= num; i++) {
            float H = i / (float) num;
            //System.out.println(H);
            Color c = Color.getHSBColor(H, S, B);
            colors.add(c);
        }

        Collections.shuffle(colors);

        return colors;
    }

    public static Palette[] getSequencialPalettes() {
        Palette p1 = new Palette(new Color(0xEDF8FB), new Color(0xB2E2E2), new Color(0x66C2A4), new Color(0x2CA25F), new Color(0x006D2C));
        Palette p2 = new Palette(new Color(0xEDF8FB), new Color(0xB3CDE3), new Color(0x8C96C6), new Color(0x8856A7), new Color(0x810F7C));
        Palette p3 = new Palette(new Color(0xF0F9E8), new Color(0xBAE4BC), new Color(0x7BCCC4), new Color(0x43A2CA), new Color(0x0868AC));
        Palette p4 = new Palette(new Color(0xFEF0D9), new Color(0xFDCC8A), new Color(0xFC8D59), new Color(0xE34A33), new Color(0xB30000));
        Palette p5 = new Palette(new Color(0xFEEBE2), new Color(0xFBB4B9), new Color(0xF768A1), new Color(0xC51B8A), new Color(0x7A0177));
        Palette p6 = new Palette(new Color(0xF1EEF6), new Color(0xBDC9E1), new Color(0x74A9CF), new Color(0x2B8CBE), new Color(0x045A8D));
        Palette p7 = new Palette(new Color(0xFFFFCC), new Color(0xA1DAB4), new Color(0x41B6C4), new Color(0x2C7FB8), new Color(0x253494));
        Palette p8 = new Palette(new Color(0xFFFFD4), new Color(0xFED98E), new Color(0xFE9929), new Color(0xD95F0E), new Color(0x993404));
        return new Palette[]{p1, p2, p3, p4, p5, p6, p7, p8};
    }

    public static Palette[] getDivergingPalettes() {
        Palette p1 = new Palette(new Color(0xA6611A), new Color(0xDFC27D), new Color(0xF5F5F5), new Color(0x80CDC1), new Color(0x018571));
        Palette p2 = new Palette(new Color(0xD01C8B), new Color(0xF1B6DA), new Color(0xF7F7F7), new Color(0xB8E186), new Color(0x4DAC26));
        Palette p3 = new Palette(new Color(0xE66101), new Color(0xFDB863), new Color(0xF7F7F7), new Color(0xB2ABD2), new Color(0x5E3C99));
        Palette p4 = new Palette(new Color(0xCA0020), new Color(0xF4A582), new Color(0xFFFFFF), new Color(0xBABABA), new Color(0x404040));
        Palette p5 = new Palette(new Color(0xD7191C), new Color(0xFDAE61), new Color(0xFFFFBF), new Color(0xABD9E9), new Color(0x2C7BB6));
        return new Palette[]{p1, p2, p3, p4, p5};
    }

    public static Palette[] getQualitativePalettes() {
        Palette p1 = new Palette(new Color(0xA6CEE3), new Color(0x1F78B4), new Color(0xB2DF8A), new Color(0x33A02C), new Color(0xFB9A99), new Color(0xE31A1C), new Color(0xFDBF6F), new Color(0xFF7F00), new Color(0xCAB2D6));
        Palette p2 = new Palette(new Color(0xFBB4AE), new Color(0xB3CDE3), new Color(0xCCEBC5), new Color(0xDECBE4), new Color(0xFED9A6), new Color(0xFFFFCC), new Color(0xE5D8BD), new Color(0xFDDAEC), new Color(0xF2F2F2));
        Palette p3 = new Palette(new Color(0xE41A1C), new Color(0x377EB8), new Color(0x4DAF4A), new Color(0x984EA3), new Color(0xFF7F00), new Color(0xFFFF33), new Color(0xA65628), new Color(0xF781BF), new Color(0x999999));
        Palette p4 = new Palette(new Color(0x8DD3C7), new Color(0xFFFFB3), new Color(0xBEBADA), new Color(0xFB8072), new Color(0x80B1D3), new Color(0xFDB462), new Color(0xB3DE69), new Color(0xFCCDE5), new Color(0xD9D9D9));
        return new Palette[]{p1, p2, p3, p4};
    }

    public static Palette get3ClassPalette(Palette palette) {
        if (palette.colors.length == 5) {
            return new Palette(new Color[]{palette.colors[0], palette.colors[2], palette.colors[4]});
        }
        return palette;

    }

    public static Palette reversePalette(Palette palette) {
        Color[] c = new Color[palette.colors.length];
        for (int i = 0; i < palette.getColors().length; i++) {
            c[c.length - 1 - i] = palette.colors[i];
        }
        return new Palette(c);
    }

    public static class Palette {

        private Color colors[];

        public Palette(Color... colors) {
            this.colors = colors;
        }

        public Color[] getColors() {
            return colors;
        }

        public float[] getPositions() {
            float[] pos = new float[colors.length];
            for (int i = 0; i < pos.length; i++) {
                pos[i] = i / (float) (pos.length - 1);
            }
            return pos;
        }
    }
}
