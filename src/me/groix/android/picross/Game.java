package me.groix.android.picross;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

enum action {
	RIEN,NOIRCIR,BLANCHIR
}

public class Game extends Activity {

	private Plateau plateau;
	private GamesDB gamesDB;


	//UI components
	FrameLayout mainDisplay;
	RelativeLayout plateauView;
	RelativeLayout labelsColView;
	RelativeLayout labelsLigView;
	private TextView[][] colonneLabels;
	private TextView[][] ligneLabels;
	private ImageView[][] listCase; 
	Button IBBlanc;
	Button IBCroix;
	Button IBNoir;
	
	TextView cptErreur;
	Chronometer chrono;
	TextView bravo;


	private int lastClickedCaseX;
	private int lastClickedCaseY;

	private float lastTouchX;
	private float lastTouchY;
	private float absolutePosX;
	private float absolutePosY;
	
	private action lastAction;

	//quelques constantes
	private static final int LONGCASE = 50;
	private static final int LONGLABELLIG = 60;
	private static final int LONGLABELCOL = 80;
	private static final int MARGIN = 20;
	private static final int MARGINCOL = 30;
	private static final int LONGCHARLIG = 15;
	private static final int LONGCHARCOL = 20;
	private static final int CHARSIZE = 20;
	private static final int CHARSIZELITE = 15;

	Options option;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		Bundle extras;
		if (savedInstanceState!=null) {
			extras = savedInstanceState;
		} else {
			extras = getIntent().getExtras();
		}
		//TODO a modifier pr une meilleur lifecycle management
		plateau = (Plateau) extras.getSerializable("plateau");
        gamesDB = new GamesDB(this);


		View mainLayout = ((View) findViewById(R.id.mainLayout));
		mainLayout.setBackgroundColor(Color.WHITE);
		mainDisplay = (FrameLayout) findViewById(R.id.mainDisplay);
		IBBlanc = (Button) findViewById(R.id.BoutonCurseurBlanc);
		IBNoir = (Button) findViewById(R.id.BoutonCurseurNoir);
		IBCroix = (Button) findViewById(R.id.BoutonCurseurCroix);
		switch (plateau.getStateCurseur()) {
			case CURSEURNOIR:
				IBNoir.setSelected(true);
				break;
			case CURSEURBLANC:
				IBBlanc.setSelected(true);
				break;
			case CURSEURCROIX:
				IBCroix.setSelected(true);
				break;
		}




		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LONGLABELLIG+plateau.getNbColonne()*LONGCASE+MARGIN,
				LONGLABELCOL+plateau.getNbLigne()*LONGCASE+MARGIN);
		plateauView = new RelativeLayout(this);
		mainDisplay.addView(plateauView, params);


		labelsColView = new RelativeLayout(this);
		labelsLigView = new RelativeLayout(this);
		mainDisplay.addView(labelsColView);
		mainDisplay.addView(labelsLigView);


		listCase = new ImageView[plateau.getNbLigne()][plateau.getNbColonne()];
		//TODO mémoire gaspillée et risque d'erreurs: a voir si on doit modifier
		ligneLabels = new TextView[plateau.getNbLigne()][plateau.getNbColonne()];
		colonneLabels = new TextView[plateau.getNbColonne()][plateau.getNbLigne()];

		option = new Options();
		option.inScaled = false;

		cptErreur = (TextView) findViewById(R.id.cptErreur);
		cptErreur.setText("Sans faute");

		chrono = (Chronometer) findViewById(R.id.chrono);
		chrono.setBase(SystemClock.elapsedRealtime()-extras.getLong("chrono"));
		bravo = (TextView) findViewById(R.id.bravo);

		if (plateau.estTermine()) {
			bravo.setText(R.string.bravo);
		} else {
			chrono.start();
		}
		
		fillCases();
		fillLabels();
		updateLabels();
		labelsColView.bringToFront();
		labelsLigView.bringToFront();


		lastClickedCaseX = plateau.getNbLigne();
		lastClickedCaseY = plateau.getNbColonne();
		absolutePosX = 0;
		absolutePosY = 0;

		plateauView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				System.out.println("MO:"+event.getAction()+" "+event.getX()+event.getY()
						+" "+((int) (event.getX() - LONGLABELLIG)/LONGCASE)
						+":"+((int) (event.getY() - LONGLABELCOL)/LONGCASE));
				if (plateau.getStateCurseur()==StateCurseur.CURSEURBLANC ||
						((event.getX()+absolutePosX) <LONGLABELLIG) ||
						((event.getY()+absolutePosY)<LONGLABELCOL) ||
						((event.getX()+absolutePosX) > (LONGLABELLIG+LONGCASE*plateau.getNbColonne()) ) ||
						((event.getY()+absolutePosY) > (LONGLABELCOL+LONGCASE*plateau.getNbLigne()) )   ){
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						lastTouchX = event.getX();
						lastTouchY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						absolutePosX +=(lastTouchX - event.getX());
						absolutePosY +=(lastTouchY - event.getY());

						plateauView.scrollTo((int)(absolutePosX),
								(int)(absolutePosY));
						labelsLigView.scrollTo((int) absolutePosX,(int)(absolutePosY));
						labelsColView.scrollTo((int)(absolutePosX),(int) absolutePosY);
						lastTouchX = event.getX();
						lastTouchY = event.getY();
						
					}
					return true;
				} else {
					if (event.getAction()==MotionEvent.ACTION_DOWN 
							|| event.getAction()==MotionEvent.ACTION_MOVE) {

						for (int h = 0; h < event.getHistorySize(); h++) {
							float absolutTouchX = absolutePosX + event.getHistoricalX(h);
							float absolutTouchY = absolutePosY + event.getHistoricalY(h);
							if (absolutTouchX>LONGLABELLIG && absolutTouchX<(LONGLABELLIG+LONGCASE*plateau.getNbColonne())
									&& absolutTouchY>LONGLABELCOL && absolutTouchY<(LONGLABELCOL+LONGCASE*plateau.getNbLigne())) {
								int touchedCaseX = (int) (absolutTouchX - LONGLABELLIG)/LONGCASE;
								int touchedCaseY = (int) (absolutTouchY - LONGLABELCOL)/LONGCASE;

								if(touchedCaseX != lastClickedCaseX || touchedCaseY != lastClickedCaseY) {
									auToucher(plateau.getGrille()[touchedCaseY][touchedCaseX]);
									plateau.updateIndices();
									listCase[touchedCaseY][touchedCaseX].setImageBitmap(getBitMap(plateau.getGrille()[touchedCaseY][touchedCaseX]));
									lastClickedCaseX = touchedCaseX;
									lastClickedCaseY = touchedCaseY;
								}
							}
						}
						float relativePosX = absolutePosX + event.getX();
						float relativePosY = absolutePosY + event.getY();
						if (relativePosX>LONGLABELLIG && relativePosX<(LONGLABELLIG+LONGCASE*plateau.getNbColonne())
								&& relativePosY>LONGLABELCOL && relativePosY<(LONGLABELCOL+LONGCASE*plateau.getNbLigne())) {
							int touchedCaseX = (int) (relativePosX - LONGLABELLIG)/LONGCASE;
							int touchedCaseY = (int) (relativePosY - LONGLABELCOL)/LONGCASE;

							if(touchedCaseX != lastClickedCaseX || touchedCaseY != lastClickedCaseY) {
								//On applique le tour:
								auToucher(plateau.getGrille()[touchedCaseY][touchedCaseX]);
								plateau.updateIndices();
								lastClickedCaseX = touchedCaseX;
								lastClickedCaseY = touchedCaseY;
								//UI:
								listCase[touchedCaseY][touchedCaseX].setImageBitmap(getBitMap(plateau.getGrille()[touchedCaseY][touchedCaseX]));
								cptErreur.setText(plateau.getCptErreur()==0?"Sans Faute":
									(plateau.getCptErreur()+ " erreur"+((plateau.getCptErreur()==1)?"":"s")));
								updateLabels();

								if (plateau.estTermine()) {
									chrono.stop();
									gamesDB.open();
									gamesDB.newGame(plateau.getID(),plateau.getCptErreur(),
											(int) ((SystemClock.elapsedRealtime()-chrono.getBase())/1000));
									bravo.setText(R.string.bravo);
									gamesDB.close();
								}
							}
						}
					} else {
						lastAction = action.RIEN;
						lastClickedCaseX = plateau.getNbLigne();
						lastClickedCaseY = plateau.getNbColonne();
					}
					return true;
				}
			}

		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("plateau", plateau);
		outState.putLong("chrono",SystemClock.elapsedRealtime()-chrono.getBase());
	}

	/**
	 * Crée les ImageVies représentant les cases, les met dans le 
	 * tableau listCase et dans le relativeLayout
	 */
	private void fillCases() {
		for (int lig = 0;lig<plateau.getNbLigne();lig++) {
			for (int col=0;col<plateau.getNbColonne();col++) {
				ImageView iv = new ImageView(this);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LONGCASE, LONGCASE);
				params.leftMargin = LONGLABELLIG+col*LONGCASE;
				params.topMargin = LONGLABELCOL+lig*LONGCASE;
				iv.setImageBitmap(getBitMap(plateau.getGrille()[lig][col]));
				iv.setAdjustViewBounds(false);
				iv.setScaleType(ScaleType.FIT_CENTER);
				listCase[lig][col] = iv;
				plateauView.addView(iv, params);
			}
		}
	}

	/**
	 * Crée et remplit les label "indice"
	 */
	public void fillLabels() {
		for(int col=0;col<plateau.getNbColonne();col++) {
			for (int mot=0;mot<plateau.getColonnes().get(col).size();mot++) {
				TextView tv = new TextView(this);
				tv.setText(" "+plateau.getColonnes().get(col).get(plateau.getColonnes().get(col).size()-1-mot));
				tv.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
				tv.setTextSize(CHARSIZE);
				tv.setTextColor(Color.BLACK);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30,30);
				params.leftMargin = LONGLABELLIG+col*LONGCASE+MARGIN;
				params.topMargin = LONGLABELCOL-LONGCHARCOL*(mot)-MARGINCOL;
				labelsColView.addView(tv, params);
				colonneLabels[col][mot] = tv;
			}
		}
		for (int lig=0;lig<plateau.getNbLigne();lig++) {
			for (int mot=0;mot<plateau.getLignes().get(lig).size();mot++) {
				TextView tv = new TextView(this);
				tv.setText(" "+plateau.getLignes().get(lig).get(plateau.getLignes().get(lig).size()-1-mot));
				tv.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
				tv.setTextSize(CHARSIZE);
				tv.setTextColor(Color.BLACK);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30,30);
				params.leftMargin = LONGLABELLIG-LONGCHARLIG*(mot+1);
				params.topMargin = LONGLABELCOL+LONGCASE*lig+MARGIN;
				labelsLigView.addView(tv, params);
				ligneLabels[lig][mot] = tv;
			}
		}
	}



	/**
	 * Met à jour les labels "indice"
	 */
	public void updateLabels() {
		for(int col=0;col<plateau.getNbColonne();col++) {
			for (int mot=0;mot<plateau.getColonnes().get(col).size();mot++) {
				colonneLabels[col][mot].setText(""+plateau.getColonnes().get(col).get(plateau.getColonnes().get(col).size()-1-mot));
				if (!(plateau.getColonnesBool().get(col).get(plateau.getColonnes().get(col).size()-1-mot))) {
					colonneLabels[col][mot].setTypeface(Typeface.MONOSPACE,Typeface.ITALIC);
					colonneLabels[col][mot].setTextSize(CHARSIZELITE);
				}
			}
		}
		for (int lig=0;lig<plateau.getNbLigne();lig++) {
			for (int mot=0;mot<plateau.getLignes().get(lig).size();mot++) {
				ligneLabels[lig][mot].setText(""+plateau.getLignes().get(lig).get(plateau.getLignes().get(lig).size()-1-mot));
				if (!(plateau.getLignesBool().get(lig).get(plateau.getLignes().get(lig).size()-1-mot))) {
					ligneLabels[lig][mot].setTypeface(Typeface.MONOSPACE,Typeface.ITALIC);
					ligneLabels[lig][mot].setTextSize(CHARSIZELITE);
				}
			}
		}
	}

	/**
	 * Une méthode qui renvoie le bitMap correspondant à la Case entrée, en
	 * fonction de son état et de son type
	 * @return Le bitmap correspondant
	 */
	private Bitmap getBitMap(Case Case) {
		if (Case.getType()==Type.BLANC) {
			if (Case.getState()==State.INCONNU) {
				//return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanc);
				return getBitMapBlanc(Case);
			} else {
				return getBitMapCroix(Case);
			}
		} else {
			if (Case.getState()==State.INCONNU) {
				//return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanc);
				return getBitMapBlanc(Case);
			} else { if (Case.getState()==State.BARRE) {
				return getBitMapCroix(Case);
			} else {
				return getBitMapNoir(Case);
			}
			}
		}
	}

	private Bitmap getBitMapBlanc(Case Case) {
		if ((Case.getX() %5)==0) {
			if ((Case.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancgh,option);
			}
			if((Case.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancdh,option);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanch,option);
		}
		if ((Case.getX()%5)==4) {
			if ((Case.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancgb,option);
			}
			if((Case.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancdb,option);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancb,option);
		}
		if ((Case.getY()%5)==0) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancg,option);
		}
		if((Case.getY()%5)==4) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancd,option);
		}
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanc,option);
	}

	private Bitmap getBitMapNoir(Case Case) {
		if ((Case.getX() %5)==0) {
			if ((Case.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirgh);
			}
			if((Case.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirdh);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirh);
		}
		if ((Case.getX()%5)==4) {
			if ((Case.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirgb);
			}
			if((Case.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirdb);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirb);
		}
		if ((Case.getY()%5)==0) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirg);
		}
		if((Case.getY()%5)==4) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noird);
		}
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.noir);
	}

	private Bitmap getBitMapCroix(Case Case) {
		if ((Case.getX() %5)==0) {
			if ((Case.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixgh);
			}
			if((Case.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixdh);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixh);
		}
		if ((Case.getX()%5)==4) {
			if ((Case.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixgb);
			}
			if((Case.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixdb);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixb);
		}
		if ((Case.getY()%5)==0) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixg);
		}
		if((Case.getY()%5)==4) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixd);
		}
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.croix);
	}

	/**
	 * applique la méthode correspondante à la case donnée, en fonction du curseur séléctionné
	 * @param Case
	 */
	public void auToucher(Case Case) {
		switch (Case.getPlateau().getStateCurseur()) {
		case CURSEURNOIR:
			Case.noircir();
			break;
		case CURSEURBLANC:
			break; //on ne fait rien
		case CURSEURCROIX:
			Case.barrer(); break;
		}
	}



	public void boutonCurseurBlanc(View view) {
		plateau.setStateCurseur(StateCurseur.CURSEURBLANC);
		IBBlanc.setSelected(true);
		IBNoir.setSelected(false);
		IBCroix.setSelected(false);
		
	}

	public void boutonCurseurNoir(View view) {
		plateau.setStateCurseur(StateCurseur.CURSEURNOIR);
		IBBlanc.setSelected(false);
		IBNoir.setSelected(true);
		IBCroix.setSelected(false);

	}

	public void boutonCurseurCroix(View view) {
		plateau.setStateCurseur(StateCurseur.CURSEURCROIX);
		IBBlanc.setSelected(false);
		IBNoir.setSelected(false);
		IBCroix.setSelected(true);
	}



}

