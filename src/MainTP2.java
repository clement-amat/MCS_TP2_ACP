import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

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

    static List<String> ordres;

    static RealMatrix projectedAP;
    static RealMatrix projectedTest;

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

    private static double calculerDistance(int indiceTest, int indiceAP) {
        return Math.sqrt(
                Math.pow(projectedAP.getEntry(indiceAP, 0) - projectedTest.getEntry(indiceTest, 0)   , 2)
                 + Math.pow(projectedAP.getEntry(indiceAP, 1) - projectedTest.getEntry(indiceTest, 1), 2)
                 + Math.pow(projectedAP.getEntry(indiceAP, 2) - projectedTest.getEntry(indiceTest,2) , 2)
                );
    }

    private static String isolerNomOdre(String nomFichier) {
        return nomFichier.split("[_ .]")[1];
    }

    private static String ordreCorrespondantAuFichierDIndice(int i) {
        return isolerNomOdre(filesAp.get(i));
    }

    private static Set<String> getListeOrdres(List<String>... files) {
        Set<String> ordres = new HashSet<>();
        for (List<String> f : files) {
            f.forEach(nomFichier -> ordres.add(isolerNomOdre(nomFichier)));
        }
        return ordres;
    }

    public static void main(String[] args) throws Exception {
        initCorpusAp();
        System.out.println(filesAp.size() + " fichiers dans base ap");
        initCorpusTest();
        System.out.println(filesTest.size() + " fichiers dans base test ");

        // Récupérer la liste des ordres
        ordres = getListeOrdres(filesAp, filesTest).stream().collect(Collectors.toList());
        System.out.println("Ordres pris en compte : \n" + ordres.stream().collect(Collectors.joining(",")));

        double[][] matAP = PretraitementACP.calculerVecteursMoyenne(fieldsAp);
        double[][] matTest = PretraitementACP.calculerVecteursMoyenne(fieldsTest);

        RealMatrix originalMatrix = MatrixUtils.createRealMatrix(matAP);
        RealMatrix testMatrix     = MatrixUtils.createRealMatrix(matTest);

        // Covariance
        double[][] matriceCovariance = PretraitementACP.matriceCovariance(matAP);

        // Indices des trois plus grandes valeurs propres
        int[] biggestEigenValuesIndex = PretraitementACP.getBiggestEigensValuesIndex(matriceCovariance, 3);
        RealMatrix matrix = MatrixUtils.createRealMatrix(matriceCovariance);
        EigenDecomposition decomposition = new EigenDecomposition(matrix);


        // Extraire les eigen vectors associés aux trois plus grande valeurs propres
        double[][] kPlusGrandsVP = new double[3][]; // k = 3
        for (int i = 0 ; i < biggestEigenValuesIndex.length ; i++) {
            kPlusGrandsVP[i] = decomposition.getEigenvector(biggestEigenValuesIndex[i]).toArray();
        }

        RealMatrix kPlusGrandVPMatrix = MatrixUtils.createRealMatrix(kPlusGrandsVP);

        // Projeter les données dans la nouvelle base
        projectedAP = originalMatrix.multiply(kPlusGrandVPMatrix.transpose()); //kPlusGrandVPMatrix.multiply(originalMatrix.transpose());
        projectedTest = testMatrix.multiply(kPlusGrandVPMatrix.transpose()); //kPlusGrandVPMatrix.multiply(testMatrix.transpose());

        // Calcul de la distance
        // pour chaque fichier de la base de test faire
        TreeSet<KPPVResult> results;
        double currentDistance = -1;
        float foundOrders        = 0;
        for (int i = 0 ; i < filesTest.size() ; i++) {
            // pour chaque fichier de la base d'AP calculer distance
            results = new TreeSet<>();
            for (int j = 0 ; j < filesAp.size() ; j++) {
                // => distance dans les trois inférieurs la prendre et enregistrer l'ordre correspondant
                currentDistance = calculerDistance(i, j);
                if (results.size() < 3) {
                    results.add(new KPPVResult(currentDistance, j));
                } else {
                    Utils.insertInSetIfLowerThanOne(results, new KPPVResult(currentDistance, j));
                }
            }

            // tous les fichiers de la base d'AP ont étés comparés => qu'a ton dit ??
            System.out.println();
            String foundOrder = findMostReccurentOrder(results);
            System.out.println("Fichier " + filesTest.get(i));
            for (KPPVResult r : results) {
                System.out.print(ordreCorrespondantAuFichierDIndice(r.indiceFichierAP) + " ");
            }
            System.out.println(" -> " + foundOrder);
            if (isolerNomOdre(filesTest.get(i)).equals(foundOrder)) {
                foundOrders += 1;
                System.out.println("^^ Ok ^^");
            } else {
                System.out.println("^^ Erreur ^^");
            }
        }
        System.out.println("\n\n>> Pourcentage de reussite : " + (foundOrders / filesTest.size() * 100) + "%");
    }

    private static String findMostReccurentOrder(TreeSet<KPPVResult> results) {
        LinkedHashMap<String, Integer> count = new LinkedHashMap<>();
        int maxCount = 0;
        String foundOrder = "NIL";
        for (KPPVResult r : results) {
            if (count.containsKey(ordreCorrespondantAuFichierDIndice(r.indiceFichierAP))) {
                count.put(
                        ordreCorrespondantAuFichierDIndice(r.indiceFichierAP),
                        count.get(ordreCorrespondantAuFichierDIndice(r.indiceFichierAP)) + 1
                );
            } else {
                count.put(ordreCorrespondantAuFichierDIndice(r.indiceFichierAP), 1);
            }
        }
        for (String order : count.keySet()) {
            if (count.get(order) > maxCount) {
                maxCount = count.get(order);
            }
        }
        for (String order : count.keySet()) {
            if (count.get(order) == maxCount) {
                foundOrder = order;
                break;
            }
        }
        return foundOrder;
    }

}
