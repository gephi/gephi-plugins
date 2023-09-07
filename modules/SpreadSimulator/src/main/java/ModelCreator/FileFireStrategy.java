package ModelCreator;


import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class FileFireStrategy{
    private File file;
    private double coverage;
    private int minCoverage;
}
