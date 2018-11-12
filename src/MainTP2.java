import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

public class MainTP2 {

    static final String FOLDER_APPRENTISSAGE = "/corpus/base_ap/";
    static final String FOLDER_TEST = "/corpus/base_test/";

    static List<String> filesTest = new ArrayList<>();
    static List<String> filesAp = new ArrayList<>();

    static Extractor extractor = Extractor.getExtractor();

    static List<Field> fieldsAp = new ArrayList<>();
    static List<Field> fieldsTest = new ArrayList<>();

    static List<float[]> moyennesBaseAP = new ArrayList<>();
    static List<float[]> moyennesBaseTest = new ArrayList<>();


    public static void getData(String folder, List<Field> fields, List<String> pFiles) throws IOException {
        String currentDirectory = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();
        currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 2);

        File dir = new File(currentDirectory + folder);
        File[] directoryListing = dir.listFiles();

        if (directoryListing == null) {
            throw new IllegalArgumentException("Dossier inexistant : " + folder);
        }

        for (File child : directoryListing) {
            if (child.getName().contains(".csv")) {
                try {
                    List<String> files = new ArrayList<>();
                    files.add(folder + child.getName());
                    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
                    int MFCCLength = 13; //FieldLength(folder + child.getName());
                    MFCC[] mfcc = new MFCC[MFCCLength];
                    for (int i = 0; i < mfcc.length; i++) {
                        mfcc[i] = extractor.nextMFCC(windowMaker);
                    }

                    fields.add(new Field(mfcc));
                    pFiles.add(child.getName());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void initCorpusAp() throws Exception {
        getData(FOLDER_APPRENTISSAGE, fieldsAp, filesAp);
    }

    public static void initCorpusTest() throws Exception {
        getData(FOLDER_TEST, fieldsTest, filesTest);
    }

    public static void main(String[] args) throws Exception {
        initCorpusAp();
        System.out.println(filesAp.size() + " fichiers dans base ap");
        initCorpusTest();
        System.out.println(filesTest.size() + " fichiers dans base test ");

        double[][] matAP = PretraitementACP.calculerVecteursMoyenne(fieldsAp);
        double[][] matTest = PretraitementACP.calculerVecteursMoyenne(fieldsTest);

        // Covariance
        double[][] matriceCovariance = PretraitementACP.matriceCovariance(matAP);

        // 3 Plus grands vecteurs propres
        double[] biggestEigenValues = PretraitementACP.getBiggestEigensValues(matriceCovariance, 3);
        RealMatrix matrix = MatrixUtils.createRealMatrix(matriceCovariance);
        EigenDecomposition decomposition = new EigenDecomposition(matrix);
        RealMatrix rm = decomposition.getV();
        double [][] EigenVecors=rm.getData();


    }

    }
}
