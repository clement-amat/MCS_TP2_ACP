import fr.enseeiht.danck.voice_analyzer.*;
import fr.enseeiht.danck.voice_analyzer.defaults.DTWHelperDefault;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MatriceConfusionTest {

    static final String FOLDER_APPRENTISSAGE = "/corpus/base_ap/";
    static final String FOLDER_TEST = "/corpus/base_test/";

    static List<String> filesTest = new ArrayList<>();
    static List<String> filesAp = new ArrayList<>();
    static WindowMaker windowMaker;
    static List<Field> fieldsAp = new ArrayList<>();
    static List<Field> fieldsTest = new ArrayList<>();
    static Extractor extractor = Extractor.getExtractor();
    static DTWHelper myDTWHelper = new myDTW();
    static DTWHelper DTWHelperDefault = new DTWHelperDefault();
    static List<String> ordres;

    static int FieldLength(String fileName) throws IOException {
        int counter = 0;
        File file = new File(System.getProperty("user.dir") + fileName);
        for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
            counter++;
        }
        return 2 * Math.floorDiv(counter, 512);
    }

    public static void initCorpus() throws Exception {
        System.out.println("Initialisation du corpus...");
        MatriceConfusionTest.initCorpusAp();
        MatriceConfusionTest.initCorpusTest();
        System.out.println("Done.");
    }

    public static void getData(String folder, List<Field> fields, List<String> pFiles) throws IOException {
        String currentDirectory = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();
        currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 2);

        File dir = new File(currentDirectory + folder);
        File[] directoryListing = dir.listFiles();
        windowMaker = new MultipleFileWindowMaker(pFiles);

        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().contains(".csv")) {
                    try {
                        List<String> files = new ArrayList<>();
                        files.add(folder + child.getName());
                        WindowMaker windowMaker = new MultipleFileWindowMaker(files);
                        int MFCCLength = FieldLength(folder + child.getName());
                        MFCC[] mfcc = new MFCC[MFCCLength];
                        for (int i = 0; i < mfcc.length; i++) mfcc[i] = extractor.nextMFCC(windowMaker);
                        //
                        fields.add(new Field(mfcc));
                        pFiles.add(child.getName());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } else {
            throw new IOException("Problem with directory " + FOLDER_APPRENTISSAGE);
        }
    }

    public static void initCorpusAp() throws Exception {
        getData(FOLDER_APPRENTISSAGE, fieldsAp, filesAp);
    }

    public static void initCorpusTest() throws Exception {
        getData(FOLDER_TEST, fieldsTest, filesTest);
    }

    public static void afficherMatrice(Number[][] matrix) {

        System.out.printf("%25.25s|", " ");
        for (String s : filesTest) {
            System.out.printf("%20.20s | ", s);
        }
        System.out.println("\n");

        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("%20.20s ->\t| ", filesAp.get(i));
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%20d | ", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("\n\n");
    }

    public static void main(String[] args) throws Exception {

        // Initialise du corpus et des fichiers de test
        initCorpus();

        Float[][] matriceDistance = new Float[filesAp.size()][filesTest.size()];
        Float[][] matriceDistanceHelper = new Float[filesAp.size()][filesTest.size()];

        Integer[][] matriceIntermediaire = new Integer[filesAp.size()][filesTest.size()];
        Integer[][] matriceIntermediaireHelper = new Integer[filesAp.size()][filesTest.size()];

        for (int i = 0; i < fieldsAp.size(); i++) {
            for (int j = 0; j < fieldsTest.size(); j++) {
                matriceDistance[i][j] = myDTWHelper.DTWDistance(fieldsAp.get(i), fieldsTest.get(j));
                matriceDistanceHelper[i][j] = DTWHelperDefault.DTWDistance(fieldsAp.get(i), fieldsTest.get(j));
            }
        }

        calculerMatriceIntermediaire(matriceDistance, matriceIntermediaire);
        calculerMatriceIntermediaire(matriceDistanceHelper, matriceIntermediaireHelper);

        //afficherMatrice(matriceDistance);
        //System.out.println("\n");
        //afficherMatrice(matriceDistanceHelper);

        //afficherMatrice(matriceIntermediaire);
        //System.out.println("\n");
        //afficherMatrice(matriceConfusionHelper);

        // matrice de confusion
        int[][] matriceConfusion;
        int[][] matriceConfusionHelper;

        ordres = getListeOrdres(filesAp, filesTest).stream().collect(Collectors.toList());

        matriceConfusion = new int[ordres.size()][ordres.size()];
        matriceConfusionHelper = new int[ordres.size()][ordres.size()];

        calculerMatriceConfusion(matriceConfusion, matriceIntermediaire);
        calculerMatriceConfusion(matriceConfusionHelper, matriceIntermediaireHelper);

        System.out.println("===\nNotre matrice de confusion\n===");
        afficherMatriceConfusion(matriceConfusion);
        System.out.println("===\nMatrice de confusion HELPER\n===");
        afficherMatriceConfusion(matriceConfusionHelper);

        /**
         * Calcul taux erreur
         * Somme elts hors diagonaux / fichiertests.size
         */
        int horsDiagonaux = calculerTauxErreur(matriceConfusion);
        System.out.println("Taux erreur (myDtw) = " + (horsDiagonaux * 1.0) / filesTest.size() * 100 + " %");
        horsDiagonaux = calculerTauxErreur(matriceConfusionHelper);
        System.out.println("Taux erreur HELPER = " + (horsDiagonaux * 1.0) / filesTest.size() * 100 + " %");

    }

    private static int calculerTauxErreur(int[][] matriceConfusion) {
        int horsDiagonaux = 0;
        for (int i = 0; i < matriceConfusion.length; i++) {
            for (int j = 0; j < matriceConfusion[i].length; j++) {
                if (i != j && matriceConfusion[i][j] != 0) {
                    horsDiagonaux++;
                }
            }
        }
        return horsDiagonaux;
    }

    private static void calculerMatriceIntermediaire(Float[][] matriceDistance, Integer[][] matriceIntermediaire) {
        for (int j = 0; j < fieldsTest.size(); j++) {
            float min = matriceDistance[0][j];
            // Recherche min
            for (int i = 0; i < fieldsAp.size(); i++) {
                min = matriceDistance[i][j] < min ? matriceDistance[i][j] : min;
            }
            // Remplacement
            for (int i = 0; i < fieldsAp.size(); i++) {
                matriceIntermediaire[i][j] = matriceDistance[i][j] == min ? 1 : 0;
            }
        }

    }

    private static void calculerMatriceConfusion(int[][] matriceConfusion, Integer[][] matriceIntermediaire) {
        for (int i = 0; i < matriceIntermediaire.length; i++) {
            for (int j = 0; j < matriceIntermediaire[i].length; j++) {
                if (matriceIntermediaire[i][j] > 0) {
                    String ordreAP = isolerNomOdre(filesAp.get(i));
                    String ordreTest = isolerNomOdre(filesTest.get(j));
                    matriceConfusion[ordres.indexOf(ordreAP)][ordres.indexOf(ordreTest)]++;
                }
            }
        }
    }

    private static void afficherMatriceConfusion(int[][] matriceConfusion) {
        System.out.printf("%17.17s|", " ");
        for (String s : ordres) {
            System.out.printf("%10.10s | ", s);
        }
        System.out.println("\n");

        for (int i = 0; i < matriceConfusion.length; i++) {
            System.out.printf("%10.10s ->\t| ", ordres.get(i));
            for (int j = 0; j < matriceConfusion[i].length; j++) {
                System.out.printf("%10d | ", matriceConfusion[i][j]);
            }
            System.out.println();
        }
        System.out.println("\n\n");
    }

    private static String isolerNomOdre(String nomFichier) {
        return nomFichier.split("[_ .]")[1];
    }

    private static Set<String> getListeOrdres(List<String>... files) {
        Set<String> ordres = new HashSet<>();
        for (List<String> f : files) {
            f.forEach(nomFichier -> ordres.add(isolerNomOdre(nomFichier)));
        }
        return ordres;
    }
}
