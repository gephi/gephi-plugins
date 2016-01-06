/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author edemairy
 */
public class SimpleTypeStatisticsTest {

    @Test
    public void testIsURILegal() {
        assertTrue(SemanticWebStatistics.isURILegal("http://dbpedia.org/resource/Category:Game_&amp;_Watch_games"));
        assertTrue(SemanticWebStatistics.isURILegal("http://dbpedia.org/resource/Category:Game_&_Watch_games"));
        assertFalse(SemanticWebStatistics.isURILegal("fxqn:/us/va/reston/cnri/ietf/24/asdf%*.fred"));
        assertFalse(SemanticWebStatistics.isURILegal("news:12345667123%asdghfh@info.cern.ch"));
    }
}
