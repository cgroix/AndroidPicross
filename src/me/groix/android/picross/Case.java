package me.groix.android.picross;

import java.io.Serializable;

import android.content.Context;

public class Case implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3873923163669617419L;
	State state;
	Type type;
	
	
	private Plateau plateau;

	private int x;
	private int y;


	public Case(Type type, int X, int Y,Plateau plateau) {
		this.state = State.INCONNU;
		this.type = type;
		this.x = X;
		this.y = Y;
		this.plateau = plateau;
	}

	/**
	 * 
	 * @return true si le joueur à fait une erreur, false sinon
	 */
	public boolean noircir() {
		if (type==Type.BLANC) {
			if (state==State.INCONNU) {
				setState(State.BARRE);
				plateau.erreur();
				return true;
			} else { if (state==State.BARRE) {
				setState(State.INCONNU);
				return false;
			} else {
				System.err.println("Erreur: etat illegal");
				return false;
			}
			}
		} else {
			if (state==State.INCONNU) {
				setState(State.DECOUVERT);
				return false;
			} else { if (state==State.BARRE) {
				setState(State.INCONNU);
				return false;
			} else {
				//rien
				return false;
			}
			}
		}
	}
	
	public boolean barrer() {
		if (type==Type.BLANC) { //La case doit rester blanche/barrée
			if (state==State.INCONNU) {
				setState(State.BARRE);
				return false;
			} else { if (state==State.BARRE) {
				setState(State.INCONNU);
				return false;
			} else {
				System.err.println("Erreur: etat illegal");
				return false;
			}
			}
		} else { //la case est à noircir
			if (state==State.INCONNU) {
				setState(State.BARRE);
				return false;
			} else { if (state==State.BARRE) {
				setState(State.INCONNU);
				return false;
			} else { //la Case est déja noircie
				//rien
				return false;
			}
			}
		}
	}


	/**
	 * Méthode permettant de savoir si une case est correctement devinée par le joueur	
	 * @return
	 */
	public boolean estDecouverte() {
		return (this.type==Type.BLANC || this.state==State.DECOUVERT);
	}
	
	/**
	 * méthode permettant de savoir si une case est de typeNoir (quelque soit son
	 * apparence sur le plateau)
	 * @return
	 */
	public boolean estNoire() {
		return (this.type==Type.NOIR);
	}


	public void setState(State state) {
		this.state = state;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public Plateau getPlateau() {
		return plateau;
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}
	
	

}

enum State {
	INCONNU, BARRE, DECOUVERT
}

enum Type {
	BLANC, NOIR
}
