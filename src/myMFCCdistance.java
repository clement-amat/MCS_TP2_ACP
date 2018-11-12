import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.MFCCHelper;

public class myMFCCdistance extends MFCCHelper {

	/**
	 * Calcule la distance entre 2 MFCC
	 */
	@Override
	public float distance(MFCC mfcc1, MFCC mfcc2) {

		float distance  = 0;

		for (int index = 0; index < mfcc1.getLength() && index < mfcc2.getLength(); index++) {
			distance += Math.pow(mfcc1.getCoef(index) - mfcc2.getCoef(index), 2);
		}

		return (float) Math.sqrt(distance);
	}

	/**
	 * retourne la valeur de mesure de la MFCC (coef d'indice 0 dans la MFCC)
	 * cette mesure permet de determiner s'il s'agit d'un mot ou d'un silence
	 */
	@Override
	public float norm(MFCC mfcc) {
		return (mfcc.getCoef(0));
	}

	/**
	 * supprime le bruit de la MFCC passee en parametre
	 * soustrait chaque coef du bruit a chaque coef du la MFCC
	 * passee en parametre
	 */
	@Override
	public MFCC unnoise(MFCC mfcc, MFCC noise) {
		 float tab[] = new float[mfcc.getLength()];
		 float result;
		 for (int i =0;i<mfcc.getLength(); i++) {
			 result = mfcc.getCoef(i) - noise.getCoef(i);
			 tab[i] = result < 0 ? 0 : result;
		 }
	     return new MFCC(tab, mfcc.getSignal());
	}

}
