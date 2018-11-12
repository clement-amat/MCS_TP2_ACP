import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import fr.enseeiht.danck.voice_analyzer.DTWHelper;
import fr.enseeiht.danck.voice_analyzer.Extractor;
import fr.enseeiht.danck.voice_analyzer.Field;
import fr.enseeiht.danck.voice_analyzer.MFCC;
import fr.enseeiht.danck.voice_analyzer.WindowMaker;
import fr.enseeiht.danck.voice_analyzer.defaults.DTWHelperDefault;

public class myDTWtest {

		//protected static final int MFCCLength = 13;
		
		// Fonction permettant de calculer la taille des Fields 
		//c'est-à-dire le nombre de MFCC du Field 
			static int FieldLength(String fileName) throws IOException {
			int counter= 0;
			File file = new File(System.getProperty("user.dir") + fileName);
            for (String line : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
            	counter++;
            }
            return 2*Math.floorDiv(counter, 512);
		}
		
		
		public static void main(String[] args) throws IOException, InterruptedException {
			
			int MFCCLength;
			DTWHelper myDTWHelper= new myDTW();
			DTWHelper DTWHelperDefault= new DTWHelperDefault();
			 
			// Chemin de recherche des fichiers sons
		    String base = "/test_res/audio/";
		    
		    // Appel a l'extracteur par defaut (calcul des MFCC)
		    Extractor extractor = Extractor.getExtractor();
		    
			// Etape 1. Lecture de Alpha
		    List<String> files = new ArrayList<>();
		    files.add(base + "Alpha.csv");
		    WindowMaker windowMaker = new MultipleFileWindowMaker(files);
		    
		    // Etape 2. Recuperation des MFCC du mot Alpha
		    MFCCLength= FieldLength(base+"Alpha.csv");
		    MFCC[] mfccsAlpha = new MFCC[MFCCLength];
		    
	        for (int i = 0; i < mfccsAlpha.length; i++) {
	            mfccsAlpha[i] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) de alpha
	        Field alphaField= new Field(mfccsAlpha);
	        
	        // Affichage du Field et de chaque MFCC
	        System.out.println(alphaField.toString());
	        for (int i =0; i<alphaField.getLength(); i++ )
	        	System.out.println(i+": "+alphaField.getMFCC(i).toString());
	        
	        // Si on veut rajouter de nouveaux mots, il suffit de repeter les etapes 1 a 3
	        
	        // Par ex., on peut tester que la distance entre alpha et alpha c'est 0
	        float mydistanceAlphaAlpha= myDTWHelper.DTWDistance(alphaField, alphaField);
	        float distanceAlphaAlphadefault= DTWHelperDefault.DTWDistance(alphaField, alphaField);
	        
	        System.out.println("myDTW - valeur distance Alpha-Alpha calculee : "+mydistanceAlphaAlpha);
	        System.out.println("DTWHelperDefault - valeur distance Alpha-Alpha calculee : "+distanceAlphaAlphadefault);
		
	        // La distance entre Alpha et Bravo
	        
	        // Etape 1. Lecture de Bravo
	        files= new ArrayList<>();
		    files.add(base + "Bravo.csv");
		    MFCCLength= FieldLength(base+"Bravo.csv");
		    windowMaker = new MultipleFileWindowMaker(files);
	
		    // Etape 2. Recuperation des MFCC du mot Bravo
		    MFCC[] mfccsBravo= new MFCC[MFCCLength];
	        for (int i = 0; i < mfccsBravo.length; i++) {
	            mfccsBravo[i] = extractor.nextMFCC(windowMaker);
	        }
	        
	        // Etape 3. Construction du Field (ensemble de MFCC) de Bravo
	        Field bravoField= new Field(mfccsBravo);
	        
	        float mydistanceAlphaBravo= myDTWHelper.DTWDistance(alphaField, bravoField);
	        float distanceAlphaBravodefault= DTWHelperDefault.DTWDistance(alphaField, bravoField);
	        
	        System.out.println("myDTW - valeur distance Alpha-Bravo calculee : "+mydistanceAlphaBravo);
	        System.out.println("DTWHelperDefault - valeur distance Alpha-Bravo calculee : "+distanceAlphaBravodefault);
	        
	        
		}
}


