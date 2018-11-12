import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;

import java.util.Arrays;

public class myDTW extends DTWHelper {

	/**
	 * Methode qui calcule le score de la DTW 
	 * entre 2 ensembles de MFCC
	 */
	@Override
	public float DTWDistance(Field unknown, Field known) {
		return DTWDistance(unknown, known, 1, 1, 1);
	}
	
	/**
	 * Methode qui calcule le score de la DTW 
	 * entre 2 ensembles de MFCC avec des poids
	 */
	public float DTWDistance(Field unknown, Field known, int w1, int w2, int w3) {
		
		myMFCCdistance distanceCalculator = new myMFCCdistance();
		float[][] matrix                  = new float[unknown.getLength() + 1][known.getLength() + 1]; 
		int i, j                          = 0;
		matrix[0][0]                      = 0;
		
		for (i = 1; i < matrix[0].length; i++) matrix[0][i] = Float.MAX_VALUE / 2;
		for (i = 1; i < matrix.length;    i++) matrix[i][0] = Float.MAX_VALUE / 2;
		
		for (i = 1; i < matrix.length; i++) {
			for (j = 1; j < matrix[i].length; j++) {
				float distance = distanceCalculator.distance(unknown.getMFCC(i - 1), known.getMFCC(j - 1));
				matrix[i][j] = Math.min(Math.min(
					matrix[i-1][j]   + (w1 * distance),
					matrix[i-1][j-1] + (w2 * distance)),
					matrix[i][j-1]   + (w3 * distance)
				);
			}
		}
		
 		return matrix[i - 1][j - 1] / (i + j - 2);
 		
	}

}
