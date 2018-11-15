import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PretraitementACP {

    /**
     * Calcule le vecteur R¹³ égal à la moyenne sur toutes les fenetres de MFCC
     */
    private static double[] calculerVecteurMoyenne(Field field) {
        double[] res = new double[field.getLength()];

        for (int i = 0 ; i < res.length ; i ++) {
            float avg = 0;
            for (int j = 0 ; j < field.getMFCC(i).getLength() ; j++) {
                avg += field.getMFCC(i).getCoef(j);
            }
            avg /= field.getMFCC(i).getLength();
            res[i] = avg;
        }

        return res;
    }

    /**
     * Pour chaque field, calculer le vecteur moyenne
     */
    public static double[][] calculerVecteursMoyenne(List<Field> fields) {
        double[][] result = new double[fields.size()][];

        for (int i = 0 ; i < fields.size() ; i ++) {
            result[i] = calculerVecteurMoyenne(fields.get(i));
        }

        return result;
    }

    /**
     *
     */
    public static double[][] matriceCovariance(double[][] m) {

        double[] colAverage = new double[m[0].length];
        double[][] matricCentree = new double[m.length][m[0].length];
        double[][] matricCentreeT = new double[m[0].length][m.length];

        // Calcul de la moyenne de chaque colonne
        for (int i = 0 ; i < m.length ; i++) {
            for (int j = 0 ; j < m[i].length ; j++) {
                colAverage[j] += m[i][j];
            }
        }
        for (int i = 0 ; i < colAverage.length ; i++) colAverage[i] /= m.length;

        // Calcul de la matrice centree
        for (int i = 0 ; i < m.length ; i++) {
            for (int j = 0 ; j < m[i].length ; j++) {
                matricCentree[i][j] = (m[i][j] - colAverage[j]);
            }
        }

        // Transposition de la matrice centree
        for (int i = 0 ; i < m.length ; i++) {
            for (int j = 0 ; j < m[i].length ; j++) {
                matricCentreeT[j][i] = matricCentree[i][j];
            }
        }

        // Calcul de la matrice co-variance
        double[][] matriceIntermediaire = multiply(matricCentreeT, matricCentree);
        for (int i = 0 ; i < matriceIntermediaire.length ; i++) {
            for (int j = 0 ; j < matriceIntermediaire[i].length ; j++) {
                matriceIntermediaire[i][j] /= m.length;
            }
        }

        return matriceIntermediaire;

    }

    public static double[][] multiply(double[][] a, double[][] b) {
        int m1 = a.length;
        int n1 = a[0].length;
        int m2 = b.length;
        int n2 = b[0].length;
        if (n1 != m2) throw new RuntimeException("Illegal matrix dimensions.");
        double[][] c = new double[m1][n2];
        for (int i = 0; i < m1; i++)
            for (int j = 0; j < n2; j++)
                for (int k = 0; k < n1; k++)
                    c[i][j] += a[i][k] * b[k][j];
        return c;
    }


    /**
     * Retourne <b>l'indice</b> des n plus grandes eigen values
     * @param matriceCovariance
     * @param count
     * @return
     */
    public static int[] getBiggestEigensValuesIndex(double[][] matriceCovariance, int count) {
        int[] biggest = new int[count];
        RealMatrix matrix = MatrixUtils.createRealMatrix(matriceCovariance);
        EigenDecomposition decomposition = new EigenDecomposition(matrix);
        double[] eaganValues = decomposition.getRealEigenvalues();
        Arrays.sort(eaganValues);
        for (int i = eaganValues.length - 1, j = 0; i >= (eaganValues.length - count); i--, j++) {
            biggest[j] = i;
        }
        return biggest;
    }
}
