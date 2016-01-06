/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.semantictweet;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author edemairy
 */
public class SemanticTweetDriverTest {

    private final static Logger logger = Logger.getLogger(SemanticTweetDriverTest.class.getName());
    private final static InputStream inriaLongFile = SemanticTweetDriverTest.class.getResourceAsStream("/test_files/inriaSemanticTweetLong.rdf");

    public SemanticTweetDriverTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    HashSet<String> expectedResultTestGetChild = new HashSet<String>(Arrays.asList("http://semantictweet.com/Maud_Charaf",
            "http://semantictweet.com/CandiceBachelet",
            "http://semantictweet.com/paslap",
            "http://semantictweet.com/Hugobiwan",
            "http://semantictweet.com/Powedia",
            "http://semantictweet.com/Jocelyne_Dias",
            "http://semantictweet.com/epietriga",
            "http://semantictweet.com/tantignac",
            "http://semantictweet.com/charlesruelle",
            "http://semantictweet.com/emeline_carre",
            "http://semantictweet.com/mathemagie",
            "http://semantictweet.com/DelphineCuny",
            "http://semantictweet.com/Sandreene",
            "http://semantictweet.com/jlmissika",
            "http://semantictweet.com/FR3Aquitaine",
            "http://semantictweet.com/strategies1",
            "http://semantictweet.com/fleurpellerin",
            "http://semantictweet.com/lsaccavini",
            "http://semantictweet.com/_lecollectif",
            "http://semantictweet.com/Anar_line",
            "http://semantictweet.com/ChGenest",
            "http://semantictweet.com/LaGuirlande",
            "http://semantictweet.com/z3zone",
            "http://semantictweet.com/lcalderan",
            "http://semantictweet.com/centraleparis",
            "http://semantictweet.com/dominiqueleglu",
            "http://semantictweet.com/mathildeD_V",
            "http://semantictweet.com/PasseurSciences",
            "http://semantictweet.com/nicolaspatte",
            "http://semantictweet.com/LaCasemate",
            "http://semantictweet.com/dimtate",
            "http://semantictweet.com/aureliedarnaud",
            "http://semantictweet.com/oseo",
            "http://semantictweet.com/jossgervais",
            "http://semantictweet.com/mecsci",
            "http://semantictweet.com/joinoin",
            "http://semantictweet.com/KTombre",
            "http://semantictweet.com/Julma",
            "http://semantictweet.com/pipomolo42",
            "http://semantictweet.com/sarveshnikumbh",
            "http://semantictweet.com/strngch",
            "http://semantictweet.com/ncmarie",
            "http://semantictweet.com/mattam_",
            "http://semantictweet.com/yangeorget",
            "http://semantictweet.com/pauline_PDP",
            "http://semantictweet.com/geomark",
            "http://semantictweet.com/nkrislock",
            "http://semantictweet.com/jjcadavid",
            "http://semantictweet.com/baghdadi_r",
            "http://semantictweet.com/laurentromary",
            "http://semantictweet.com/duportet",
            "http://semantictweet.com/hoaproject",
            "http://semantictweet.com/lukostaz",
            "http://semantictweet.com/JeanFred",
            "http://semantictweet.com/Forge_INRIA",
            "http://semantictweet.com/rmod_inria",
            "http://semantictweet.com/gcharrie",
            "http://semantictweet.com/cybunk",
            "http://semantictweet.com/bubbleT2012",
            "http://semantictweet.com/koutaous",
            "http://semantictweet.com/isabellemathieu",
            "http://semantictweet.com/yannguegan",
            "http://semantictweet.com/BatonBoys",
            "http://semantictweet.com/LaboBnF",
            "http://semantictweet.com/CIGREF",
            "http://semantictweet.com/miladus",
            "http://semantictweet.com/Sciences_Avenir",
            "http://semantictweet.com/BFMTV",
            "http://semantictweet.com/ParisDiderot",
            "http://semantictweet.com/MichelAlbergant",
            "http://semantictweet.com/Paris",
            "http://semantictweet.com/smartplanetfr",
            "http://semantictweet.com/timberners_lee",
            "http://semantictweet.com/pierrecap",
            "http://semantictweet.com/lesinfosdejo",
            "http://semantictweet.com/Xavier75",
            "http://semantictweet.com/groupeTraces",
            "http://semantictweet.com/Sciencefestiva1",
            "http://semantictweet.com/i_montaigne",
            "http://semantictweet.com/bluetouff",
            "http://semantictweet.com/nlaffont",
            "http://semantictweet.com/nitot",
            "http://semantictweet.com/epelboin",
            "http://semantictweet.com/univbordeaux",
            "http://semantictweet.com/UAntillesGuyane",
            "http://semantictweet.com/Yale",
            "http://semantictweet.com/Harvard",
            "http://semantictweet.com/Princeton",
            "http://semantictweet.com/Cambridge_Uni",
            "http://semantictweet.com/UniofOxford",
            "http://semantictweet.com/Stanford",
            "http://semantictweet.com/SorbonneParis1",
            "http://semantictweet.com/AssasTwits",
            "http://semantictweet.com/Paris_Sorbonne",
            "http://semantictweet.com/gdelalande",
            "http://semantictweet.com/pierremerckle",
            "http://semantictweet.com/maxplanckpress",
            "http://semantictweet.com/FradiFrad",
            "http://semantictweet.com/cinemathequefr",
            "http://semantictweet.com/lesinrocks",
            "http://semantictweet.com/patrick9clement",
            "http://semantictweet.com/technologiecb",
            "http://semantictweet.com/OlivierChapuis",
            "http://semantictweet.com/VidalLaurent",
            "http://semantictweet.com/HeloBouillard",
            "http://semantictweet.com/framaka",
            "http://semantictweet.com/DNArchi",
            "http://semantictweet.com/nicolasbronsard",
            "http://semantictweet.com/VTSMPCetim",
            "http://semantictweet.com/openKMQ3D",
            "http://semantictweet.com/institut_eduter",
            "http://semantictweet.com/AntidotNet",
            "http://semantictweet.com/Netva_fr",
            "http://semantictweet.com/Cfr",
            "http://semantictweet.com/Sandra_Pereira_",
            "http://semantictweet.com/FTA_P",
            "http://semantictweet.com/blogueur",
            "http://semantictweet.com/carolecabreton",
            "http://semantictweet.com/chubillau",
            "http://semantictweet.com/jenetwittepas",
            "http://semantictweet.com/vdumontier",
            "http://semantictweet.com/MauriceCynthia",
            "http://semantictweet.com/plncfc",
            "http://semantictweet.com/test_esr",
            "http://semantictweet.com/jjvie",
            "http://semantictweet.com/ximad",
            "http://semantictweet.com/Jo7peron",
            "http://semantictweet.com/Smyrnh1922",
            "http://semantictweet.com/lemonway",
            "http://semantictweet.com/Just3Dit",
            "http://semantictweet.com/NicolasCom3D",
            "http://semantictweet.com/Thomas_Pilorin",
            "http://semantictweet.com/niclabscl",
            "http://semantictweet.com/JaquesGrobler1",
            "http://semantictweet.com/phbridon",
            "http://semantictweet.com/Intelcia_Group",
            "http://semantictweet.com/LesBonsTuiss",
            "http://semantictweet.com/zox9999",
            "http://semantictweet.com/myleneprieur",
            "http://semantictweet.com/leftrightmirror",
            "http://semantictweet.com/PascalNogaro",
            "http://semantictweet.com/rdesjours",
            "http://semantictweet.com/FLecellier",
            "http://semantictweet.com/AntoineCervezas",
            "http://semantictweet.com/MarjorieLequet",
            "http://semantictweet.com/olfa_h",
            "http://semantictweet.com/synestheorie",
            "http://semantictweet.com/WhynotWallas",
            "http://semantictweet.com/Chaaarliiine",
            "http://semantictweet.com/FlowersINRIA",
            "http://semantictweet.com/Sciences_nat",
            "http://semantictweet.com/casimedia",
            "http://semantictweet.com/CentroScienza",
            "http://semantictweet.com/GofFranckyGo",
            "http://semantictweet.com/Soufianze",
            "http://semantictweet.com/Abou_Yaareb",
            "http://semantictweet.com/maryy33",
            "http://semantictweet.com/LaunayCharlotte",
            "http://semantictweet.com/EcologieI",
            "http://semantictweet.com/ecolemultimedia",
            "http://semantictweet.com/FrancoRedBlue",
            "http://semantictweet.com/quesh_twit",
            "http://semantictweet.com/Sevegui",
            "http://semantictweet.com/jyr9438",
            "http://semantictweet.com/rinocerose",
            "http://semantictweet.com/Capucinenet1",
            "http://semantictweet.com/moscoup",
            "http://semantictweet.com/onrasleaw",
            "http://semantictweet.com/CelineSpanna",
            "http://semantictweet.com/MBouilhol",
            "http://semantictweet.com/DonFerdie",
            "http://semantictweet.com/dicodufutur",
            "http://semantictweet.com/Seieff",
            "http://semantictweet.com/DukhonLaloy",
            "http://semantictweet.com/Foup",
            "http://semantictweet.com/JPH_R",
            "http://semantictweet.com/martinlessard",
            "http://semantictweet.com/stevenfayen25",
            "http://semantictweet.com/peuplequimanque",
            "http://semantictweet.com/FLVacances",
            "http://semantictweet.com/tappof",
            "http://semantictweet.com/FASEGERONTO",
            "http://semantictweet.com/cyplec",
            "http://semantictweet.com/RonanChupaChups",
            "http://semantictweet.com/anemoff",
            "http://semantictweet.com/Pequena_Bruja",
            "http://semantictweet.com/mamewotoko",
            "http://semantictweet.com/GettySales",
            "http://semantictweet.com/Cultnum",
            "http://semantictweet.com/Mohammad_RMAYTI",
            "http://semantictweet.com/TERA_Env",
            "http://semantictweet.com/primevere22",
            "http://semantictweet.com/ValerieMPascual",
            "http://semantictweet.com/uju_",
            "http://semantictweet.com/___Mc__",
            "http://semantictweet.com/map93200"));

    @Test
    public void testGetChild() throws IOException {
        SemanticTweetDriver driver = new SemanticTweetDriver();
        HashSet<String> childs = new HashSet<String>( driver.getChilds(buildString(inriaLongFile)) );
        HashSet<String> hs = new HashSet<String>(expectedResultTestGetChild);
        hs.removeAll(childs);
        logger.log(Level.INFO, "Remaining nodes = {0}", hs);
        assertEquals(expectedResultTestGetChild, childs);
    }

    private String buildString(final InputStream input) throws IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader bis = new BufferedReader(new InputStreamReader(input));
        String currentLine = null;
        while ((currentLine = bis.readLine()) != null) {
            result.append(currentLine + '\n');
        }
        return result.toString();
    }
}
