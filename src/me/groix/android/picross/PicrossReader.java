package me.groix.android.picross;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Une classe permettant de lir les fichiers .pic, repr�sentant un puzzle.
 * <br>Ne contient que des m�thodes statiques
 * @author Corentin
 *
 */
public class PicrossReader {

	/**
	 * Une classe lisant un fichier .pic et cr�ant le Plateau associ�.
	 * @param file l'URL d'un fichier .pic
	 * @return un Plateau, correspopndant au fichier
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Plateau read(InputStream file,String id) throws FileNotFoundException, IOException {
		String titre;
		String auteur;

		int nbLigne;
		int nbColonne;
		boolean[][] pattern;
		

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titreLigne  = picrossStream.readLine();
		String auteurLigne = picrossStream.readLine();
		String nbLigneLigne  = picrossStream.readLine();
		String nbColonneLigne  = picrossStream.readLine();

		if (!titreLigne.substring(0, 6).equals("titre=")) {
			System.err.println("erreur a la lecture du titre dans "+file);
		}
		titre = titreLigne.substring(6);
		if (!auteurLigne.substring(0, 7).equals("auteur=")) {
			System.err.println("erreur a la lecture de l'auteur dans "+file);
		}
		auteur = auteurLigne.substring(7);

		nbLigne = Integer.parseInt(nbLigneLigne.substring(9));
		nbColonne= Integer.parseInt(nbColonneLigne.substring(11));

		pattern = new boolean[nbLigne][nbColonne];
		for (int lig=0;lig<nbLigne;lig++) {
			String ligne = picrossStream.readLine();
			for (int col=0;col<nbColonne;col++) {
				if (ligne.charAt(col)=='0') {
					pattern[lig][col] = false;
				} else {
					pattern[lig][col] = true;
				}
			}

		}
		return new Plateau(nbLigne,nbColonne,pattern,titre,auteur,id);
	}
	
	/**
	 * Une petite classe permettant d'obtenir le titre du puzzle :
	 *  titre, taille
	 * @param file l'Url d'un fichier .pic
	 * @return les infos
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String lireTitre(InputStream file) throws FileNotFoundException, IOException {
		String titre;
		int nbLigne;
		int nbColonne;

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titreLigne  = picrossStream.readLine();
		String auteurLigne = picrossStream.readLine();
		String nbLigneLigne  = picrossStream.readLine();
		String nbColonneLigne  = picrossStream.readLine();

		if (!titreLigne.substring(0, 6).equals("titre=")) {
			System.err.println("erreur a la lecture du titre dans "+file);
		}
		titre = titreLigne.substring(6);
		nbLigne = Integer.parseInt(nbLigneLigne.substring(9));
		nbColonne= Integer.parseInt(nbColonneLigne.substring(11));

		return titre;
	}
	
	/**
	 * Retourne l'auteur du puzzle demandé
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String lireAuteur(InputStream file) throws FileNotFoundException, IOException {
		String auteur;

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titreLigne  = picrossStream.readLine();
		String auteurLigne = picrossStream.readLine();

		if (!titreLigne.substring(0, 6).equals("titre=")) {
			System.err.println("erreur a la lecture du titre dans "+file);
		}
		auteur = auteurLigne.substring(7);

		return auteur;
	}
	
	/**
	 * Retourne les dimensions du puzzle
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String lireDimension(InputStream file) throws FileNotFoundException, IOException {
		String dim;
		int nbLigne;
		int nbColonne;

		InputStreamReader picrossFile = new InputStreamReader(file);
		BufferedReader picrossStream = new BufferedReader(picrossFile);

		String titreLigne  = picrossStream.readLine();
		String auteurLigne = picrossStream.readLine();
		String nbLigneLigne  = picrossStream.readLine();
		String nbColonneLigne  = picrossStream.readLine();

		if (!titreLigne.substring(0, 6).equals("titre=")) {
			System.err.println("erreur a la lecture du titre dans "+file);
		}
		nbLigne = Integer.parseInt(nbLigneLigne.substring(9));
		nbColonne= Integer.parseInt(nbColonneLigne.substring(11));
		dim = nbLigne +"x"+nbColonne;
		return dim;
	}

	



	/**
	 * Un test
	 * @param args
	 */
//	public static void main(String[] args) {
//		String fichier = "puzzle10x10-1.pic";
//		try {
//			picrossLecteur.lire(fichier);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
