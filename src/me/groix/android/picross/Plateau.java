package me.groix.android.picross;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;

enum StateCurseur {
	CURSEURNOIR,CURSEURBLANC,CURSEURCROIX
}
public class Plateau implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean[][] casesBool;
	private int nbLigne;
	private int nbColonne;
	private String titre;
	private String auteur;
	private String id;
	
	private Case[][] grille;
	private List<List<Integer>> colonnes;
	private List<List<Integer>> lignes;
	private List<List<Boolean>> colonnesBool;
	private List<List<Boolean>> lignesBool;

	private StateCurseur stateCurseur;
	private int cptErreur;
	
	public static boolean[][] defaultPattern = {{true,false,false,false,true},
		{false,false,true,false,false},
		{false,false,true,false,false},
		{true,false,false,false,true},
		{false,true,true,true,false}
	};


	/**
	 * Crée un Plateau par défault
	 */
	public Plateau() {
		this(5,5,defaultPattern,"Test","Coco","null");
		stateCurseur = StateCurseur.CURSEURNOIR;
		cptErreur = 0;
	}

	/**
	 * Constructeur complet
	 * @param nbLigne2
	 * @param nbColonne2
	 * @param pattern
	 * @param titre
	 */
	public Plateau(int nbLigne, int nbColonne, boolean[][] caseBool,
			String titre, String auteur,String id) {
		this.nbLigne = nbLigne;
		this.nbColonne = nbColonne;
		this.casesBool = caseBool;
		this.titre = titre;
		this.auteur = auteur;
		this.id = id;
		
		colonnes = new ArrayList<List<Integer>>();
		lignes = new ArrayList<List<Integer>>();;
		colonnesBool = new ArrayList<List<Boolean>>();;
		lignesBool = new ArrayList<List<Boolean>>();
	
		
		grille = new Case[getNbLigne()][getNbColonne()];
		for (int lig=0;lig<nbLigne;lig++) {
			for (int col=0;col<nbColonne;col++) {
				grille[lig][col] = new Case(casesBool[lig][col]?Type.NOIR:Type.BLANC,lig,col,this);
			}
		}

		//remplit les Listes d'indice, entiers et booleens, par défaut: true
		int cpt=0;
		for(int lig=0;lig<getNbLigne();lig++) {
			lignes.add(new ArrayList<Integer>());
			lignesBool.add(new ArrayList<Boolean>());
			if (cpt>0) {
				lignes.get(lig-1).add(cpt);
				lignesBool.get(lig-1).add(true);
				cpt=0;
			}
			for (int col=0;col<getNbColonne();col++) {
				if (casesBool[lig][col]) {
					cpt++;
				} else {
					if (cpt>0) {
						lignes.get(lig).add(cpt);
						lignesBool.get(lig).add(true);
						cpt=0;
					}
				}
			}
		}
		if (cpt>0) {
			lignes.get(getNbLigne() -1).add(cpt);
			lignesBool.get(getNbLigne() -1).add(true);
			cpt=0;
		}
		for(int col=0;col<getNbColonne();col++) {
			colonnes.add(new ArrayList<Integer>());
			colonnesBool.add(new ArrayList<Boolean>());
			if (cpt>0) {
				colonnes.get(col-1).add(cpt);
				colonnesBool.get(col-1).add(true);
				cpt=0;
			}
			for (int lig=0;lig<getNbLigne();lig++) {
				if (casesBool[lig][col]) {
					cpt++;
				} else {
					if (cpt>0) {
						colonnes.get(col).add(cpt);
						colonnesBool.get(col).add(true);
						cpt=0;
					}
				}
			}
		}
		if (cpt>0) {
			colonnes.get(getNbColonne() -1).add(cpt);
			colonnesBool.get(getNbColonne() -1).add(true);
		}

		stateCurseur = StateCurseur.CURSEURNOIR;
		cptErreur = 0;
	}


	/**
	 * Met à jour les tableaux colonnesBool et lignesBool en fonction des cases déjà 
	 * découvertes
	 */
	public void updateIndices() {
		for (int lig=0;lig<getNbLigne();lig++) {
			int col=0;
			int mot=0;
			int cpt=0;
			while (mot<lignes.get(lig).size() && grille[lig][col].estDecouverte()) {
				if (grille[lig][col].estNoire()) {
					cpt++;
				}
				if (lignes.get(lig).get(mot)==cpt) {
					lignesBool.get(lig).set(mot,false);
					mot++;
					cpt=0;
				}
				col++;
				if (col>=getNbColonne()) {
					break;
				}
			}
			mot = lignes.get(lig).size()-1;
			cpt = 0;
			col = getNbColonne() -1;
			while (mot>=0 && grille[lig][col].estDecouverte()) {
				if (grille[lig][col].estNoire()) {
					cpt++;
				}
				if (lignes.get(lig).get(mot)==cpt) {
					lignesBool.get(lig).set(mot,false);
					mot--;
					cpt = 0;
				}
				col--;
				if (col==0) {
					break;
				}
			}
		}
		for (int col=0;col<getNbColonne();col++) {
			int lig=0;
			int mot=0;
			int cpt=0;
			while (mot<colonnes.get(col).size() && grille[lig][col].estDecouverte()) {
				if (grille[lig][col].estNoire()) {
					cpt++;
				}
				if (colonnes.get(col).get(mot)==cpt) {
					colonnesBool.get(col).set(mot,false);
					mot++;
					cpt=0;
				}
				lig++;
				if (lig>=getNbLigne()) {
					break;
				}
			}
			mot = colonnes.get(col).size()-1;
			cpt = 0;
			lig = getNbLigne() -1;
			while (mot>=0 && grille[lig][col].estDecouverte()) {
				if (grille[lig][col].estNoire()) {
					cpt++;
				}
				if (colonnes.get(col).get(mot)==cpt) {
					colonnesBool.get(col).set(mot,false);
					mot--;
					cpt=0;
				}
				lig--;
				if (lig==0) {
					break;
				}
			}
		}
	}
	
	/**
	 * Méthode permettant de savoir si le joueur à découvert
	 * le motif
	 * @return true si le jeu est terminé
	 */
	public boolean estTermine() {
		for (int lig=0;lig<this.getNbLigne();lig++) {
			for (int col=0;col<this.getNbColonne();col++) {
				if (!grille[lig][col].estDecouverte())
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Incrémente le compteur
	 */
	public void erreur() {
		cptErreur++;
	}

	/**
	 * retourne le nombre erreur
	 */
	public int getCptErreur() {
		return cptErreur;
	}
	
	/**
	 * Retourne le tableau des cases du jeu
	 */
	public Case[][] getGrille() {
		return grille;
	}
	
	public int getNbLigne() {
		return nbLigne;
	}
	
	public int getNbColonne() {
		return nbColonne;
	}
	
	public void setStateCurseur(StateCurseur stateCurseur) {
		this.stateCurseur = stateCurseur;
	}

	public StateCurseur getStateCurseur() {
		return stateCurseur;
	}

	/**
	 * @return the colonnes
	 */
	public List<List<Integer>> getColonnes() {
		return colonnes;
	}

	/**
	 * @return the lignes
	 */
	public List<List<Integer>> getLignes() {
		return lignes;
	}

	/**
	 * @return the colonnesBool
	 */
	public List<List<Boolean>> getColonnesBool() {
		return colonnesBool;
	}

	/**
	 * @return the lignesBool
	 */
	public List<List<Boolean>> getLignesBool() {
		return lignesBool;
	}

	/**
	 * @return the titre
	 */
	public String getTitre() {
		return titre;
	}

	/**
	 * @return the auteur
	 */
	public String getAuteur() {
		return auteur;
	}
	
	/**
	 * Retourne l'ID
	 */
	public String getID() {
		return id;
	}
}