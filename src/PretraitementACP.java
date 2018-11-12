import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;

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


}
