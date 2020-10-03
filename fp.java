
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.util.*;

import java.awt.geom.*;

import javazoom.jl.player.Player;

public class fp extends JFrame{
	boolean fullSrc = false;
	public static void main(String [] args){
		new fp();
	}
	public fp(){
		//////initialize
		super("fp");
		//setLayout(null);
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int srcW = (int)screenSize.getWidth(), srcH = (int)screenSize.getHeight();
		int centerX =  srcW/ 2, centerY = srcH/2;
		setBounds(centerX/3,centerY/3, srcW*2/3, srcH*2/3);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
		setResizable(false);
		panel p = new panel(this);
		p.setFocusable(true);
		p.requestFocusInWindow();
		p.addKeyListener(p);
		p.addMouseListener(p);
		p.addMouseMotionListener(p);
		setContentPane(p);
		setVisible(true);
		//////initialize
	}
	
	public class SongData{
		public String songFilePath;
		public String songName;
		public String diff;
		public String songFileName;
		//public String songImageName;
		public Image songImage;
		//public FileInputStream audioFile;
		public String audioName;
		public int x, y;
		public SongData(SongData s){
			this.songFilePath = s.songFilePath;
			this.songName = s.songName;
			this.diff = s.diff;
			this.songFileName = s.songFileName;
			//this.songImageName = s.songImageName;
			this.songImage = s.songImage;
			//this.audioFile = s.audioFile;
			this.audioName = s.audioName;
			x = y = -10000;
		}
		public SongData(){
		}
	}
	/*
	public class SongParameter{
		public ArrayList<double> beatLength;
		public ArrayList<int> meter;				//節拍數
		public double SliderMultiplier;
		public TimingPoints(){
			beatLength = new ArrayList< double >();
			meter = new ArrayList< int >();
		}
	}
	*/
	

	public class panel extends JPanel implements MouseListener,MouseMotionListener,KeyListener {
		private int y , rectx, recty, hover, score, runScore, runResScore, fireCnt = 0, combo_opa = 0, colorR = 5, colorG = 87, colorB = 94, combo, Maxcombo, perfect, great, good, miss, run_combo, run_perfect, run_great, run_good, run_miss;
		private int setting_acr_radius, run_setting_acr_radius;
		private int exit_opa;
		private int scrH, scrW, maxDis_arc, minDis_arc, rangeDis_arc;
		private long result_latency;
		private boolean exit_opaUp, hover_color;
		final private String str_Exit = "ESC to exit";
		private String current_songName = "";
		private Image ibuffer;
		private Graphics gbuffer;
		private boolean click, fps_refresh, result_score;
		private ArrayList< SongData > songList;
		//private Player tempPlayer;
		private PlayerThread tempPlayer = null;
		private SelecMoveThread selecMove;
		private boolean isplaying;
		private final int songBarHeight = 100, songBarDistance = 110, songBarWidth = 500 , songBarListPosX = 300;
		private ArrayList< Circle > circleList;
		private ArrayList< Slide > slideList;
		private PlayingThread playingThread = null; 
		private MousePos mousePos;
		private fp jf;
		
		public panel(fp f){
			jf = f;
			scrW = jf.getWidth();
			scrH = jf.getHeight();
			click = isplaying = result_score = hover_color = false;
			rectx = songBarListPosX;
			run_setting_acr_radius = scrW / 10;
			hover = -1;
			songList = new ArrayList< SongData >();
			circleList = new ArrayList< Circle >();
			slideList = new ArrayList< Slide >();
			SongData temp = new SongData();
			//songPar = new SongParameter();
			
			mousePos = new MousePos();
			
			try{
				File file = new File("./songs/");
				String tempstr;
				for (File subfile : file.listFiles()){
					for (File ssubfile : subfile.listFiles()){
						if (ssubfile.getName().indexOf(".osu") > 0){
							System.out.println(ssubfile.getName());
							temp.songFilePath = "./songs/" + subfile.getName() ;
							Scanner reader = new Scanner(ssubfile);
							temp.songFileName = ssubfile.getName();
							try {
							
								temp.songName = ssubfile.getName().substring(0,ssubfile.getName().indexOf("(")) + ssubfile.getName().substring(ssubfile.getName().indexOf("["), ssubfile.getName().indexOf("]")+1);
							}catch(Exception ex){
								temp.songName = ssubfile.getName().substring(0,ssubfile.getName().indexOf("."));
							
							};
							while (reader.hasNextLine()){
								tempstr = reader.nextLine();
								if (tempstr.indexOf("AudioFilename: ") == 0){
									tempstr = tempstr.substring(15);
									temp.audioName = tempstr;
									/*
									try {
										temp.audioFile = new FileInputStream(new File(temp.songFilePath + "/" + tempstr));
									} catch (IOException e){};*/
								}
								if (tempstr.indexOf("OverallDifficulty") == 0){
									temp.diff = tempstr.substring(18);
								}
								if (tempstr.indexOf(".jpg") >= 0 || tempstr.indexOf(".png") >= 0 || tempstr.indexOf(".PNG")>= 0){
									tempstr = tempstr.substring(tempstr.indexOf("\"")+1);
									tempstr = tempstr.substring(0,tempstr.indexOf("\""));
									try{
										temp.songImage = ImageIO.read(new File(temp.songFilePath + "/" + tempstr));
									} catch (IOException e){};
									break;
								}
								
							}
							//System.out.print("File Path: "+temp.songFileName+"\nSong Name: "+temp.songName+"\nDiff: "+temp.diff+"\nSong File Name: "+temp.songFileName+"\nAudio Name: "+temp.audioName+"\nx :"+temp.x+" y: "+temp.y+"\n\n\n");
							songList.add(new SongData(temp));
						}
					}
				}
			} catch (IOException e){}
			recty = ((int)(Math.random()*songList.size())) * -songBarDistance + 600/2 - songBarDistance/2;
			try{
				if(playingThread != null){
					playingThread.interrupt();
					playingThread = null;
				}
				if(tempPlayer != null){
					try{
						tempPlayer.close();
					}catch(Exception ex){
						System.out.println(ex.toString());
					}
					tempPlayer = null;
				}
				tempPlayer = new PlayerThread(songList.get((recty - 600/2 )/ -songBarDistance ).songFilePath + "/" + songList.get((recty - 600/2 )/ -songBarDistance ).audioName, 
				songList.get((recty - 600/2 )/ -songBarDistance ).audioName);
				tempPlayer.start();
			}catch(Exception ex){};
		}
		
		@Override
		public void paintComponent(Graphics g){
			//System.out.println("paint");
			scrW = getWidth();
			scrH = getHeight();
			g.setColor(getBackground());
			g.fillRect(0,0,this.getSize().width,this.getSize().height);
			g.drawString("No Image is found!", this.getSize().width - 50, this.getSize().height - 50);
			g.drawImage(songList.get((recty - this.getSize().height/2 ) / -songBarDistance).songImage,0,0,this.getSize().width, this.getSize().height,this);
			if (!isplaying){
				Font font = new Font("Arial", Font.PLAIN, 20);
				g.setFont(font);

				setting_acr_radius = scrW / 10;
				minDis_arc = setting_acr_radius/2;
				rangeDis_arc = setting_acr_radius;
				maxDis_arc = setting_acr_radius * 3 / 2;
				
				int i = 0;
				for (SongData d : songList){
					if (recty + songBarDistance*i < -songBarHeight || recty + songBarDistance*i > this.getSize().height){
						//System.out.print("1");
						i++;
						continue;
					}
					d.x = (int)Math.pow(recty + songBarDistance*i+55 - this.getSize().height/2,2)/(this.getSize().width/2) + this.getSize().width/3;
					d.y = recty + songBarDistance*i;
					
					
					g.setColor(new Color(colorR,colorG,colorB,100));
					if (i == hover){
						g.setColor(new Color(100,100,0,100));
					}
					g.fillRect(d.x , d.y, songBarWidth, songBarHeight);
					g.setColor(new Color(255,255,255,255));
					g.drawString(d.songName,d.x+20, d.y +30);
					g.drawString(d.diff,d.x+20 , d.y +70);
					i++;
				}
				if (i == 0){
					font = new Font("Arial", Font.BOLD, 50);
					g.setFont(font);
					g.drawString("No Song", this.getSize().width/2 -100 ,this.getSize().height/2);
				}
				
				//setting_acr_radius = getWidth() / 10;
				Graphics2D g2d = (Graphics2D)g;
				
				Shape outer = new Arc2D.Double(-run_setting_acr_radius, scrH - run_setting_acr_radius, 2*run_setting_acr_radius, 2*run_setting_acr_radius, 0D, 90D, Arc2D.PIE);
				Shape inner = new Arc2D.Double(-run_setting_acr_radius/2, scrH - run_setting_acr_radius/2, run_setting_acr_radius, run_setting_acr_radius, 0D, 90D, Arc2D.PIE);
				Area arc = new Area( outer );
				arc.subtract( new Area(inner) );
				g2d.translate(0,0);
				if(hover_color){
					g2d.setColor(new Color(100, 100, 0, 150));
				}
				else{
					g2d.setColor(new Color(colorR, colorG, colorB, 150));
				}
				g2d.fill(arc);
			}
			else {
				//result_score && score == runScore
				if(result_score && score == runScore && System.currentTimeMillis() > result_latency){
					runResScore += ((int)((double)(score - runResScore) * 0.01));
					runResScore += 1;
					if(runResScore > score){
						runResScore = score;
					}
					if(run_combo < Maxcombo){
						run_combo++;
					}
					if(run_perfect < perfect){
						run_perfect++;
					}
					if(run_great < great){
						run_great++;
					}
					if(run_good < good){
						run_good++;
					}
					if(run_miss < miss){
						run_miss++;
					}
					String tmpScoreStr = "Score";
					String tmpScore = String.valueOf(runResScore);
					String tmpCombo = "Combo   " + String.valueOf(run_combo);
					String tmpPerfect = "Perfect   " + String.valueOf(run_perfect);
					String tmpGreat = "Great      " + String.valueOf(run_great);
					String tmpGood = "Good      " + String.valueOf(run_good);
					String tmpMiss = "Miss       " + String.valueOf(run_miss);
					
					int allWidbase_len = scrW * 1 / 10, allHeibase_len = scrH * 1 / 10;
					int cnt_baseHei = scrH * 2 / 10;
					int cnt_len = scrW * 4 / 10, cnt_hei = scrH * 6 / 10;
					
					g.setColor(new Color(12,21,15,150));
					g.fillRoundRect(allWidbase_len,allHeibase_len,scrW-allWidbase_len * 2,scrH-allHeibase_len * 2,200,200);
					
					//Song Name
					g.setColor(new Color(255,255,255,200));
					g.setFont(new Font("Arial", Font.BOLD, 80));
					g.drawString(current_songName, scrW/2-(String.valueOf(current_songName)).length()*20,allHeibase_len + 100);
					
					//PerfectW
					//g.setColor(new Color(colorR,colorG,colorB,250));
					g.setFont(new Font("Arial", Font.BOLD, 50));
					g.drawString(tmpPerfect, allWidbase_len + 100, allHeibase_len + cnt_baseHei + cnt_hei * 1 / 6);
					
					//Great
					//g.setColor(new Color(colorR,colorG,colorB,250));
					g.setFont(new Font("Arial", Font.BOLD, 50));
					g.drawString(tmpGreat, allWidbase_len + 100, allHeibase_len + cnt_baseHei + cnt_hei * 2 / 6);
					
					//Good
					//g.setColor(new Color(colorR,colorG,colorB,250));
					g.setFont(new Font("Arial", Font.BOLD, 50));
					g.drawString(tmpGood, allWidbase_len + 100, allHeibase_len + cnt_baseHei + cnt_hei * 3 / 6);
					
					//Miss
					//g.setColor(new Color(colorR,colorG,colorB,250));
					g.setFont(new Font("Arial", Font.BOLD, 50));
					g.drawString(tmpMiss, allWidbase_len + 100, allHeibase_len + cnt_baseHei + cnt_hei * 4 / 6);
					
					//Combo
					//g.setColor(new Color(colorR,colorG,colorB,250));
					g.setFont(new Font("Arial", Font.BOLD, 50));
					g.drawString(tmpCombo, allWidbase_len + 100, allHeibase_len + cnt_baseHei + cnt_hei * 5 / 6);
					
					//Score
					//g.setColor(new Color(colorR,colorG,colorB,200));
					g.setFont(new Font("Arial", Font.BOLD, 60));
					g.drawString(tmpScoreStr, allWidbase_len + cnt_len, allHeibase_len + cnt_baseHei + 50);
					
					//Your Score
					//g.setColor(new Color(colorR,colorG,colorB,200));
					g.setFont(new Font("Arial", Font.BOLD, 80));
					g.drawString(tmpScore, scrW * 7 / 10 - tmpScore.length() * 10, allHeibase_len + cnt_baseHei + cnt_hei / 2);
					
					//Exit Hint
					if(exit_opaUp){
						exit_opa+=1;
					}
					else{
						exit_opa-=1;
					}
					if(exit_opa > 255){
						exit_opa = 255;
						exit_opaUp = false;
					}
					else if(exit_opa < 0){
						exit_opa = 0;
						exit_opaUp = true;
					}
					g.setColor(new Color(colorR,colorG,colorB,exit_opa));
					g.setFont(new Font("Arial", Font.BOLD, 40));
					g.drawString(str_Exit, scrW / 2 - str_Exit.length() * 10, scrH - allHeibase_len / 2);
					//System.out.println("Result: "+runResScore);
					return;
				}
				int value = 0;
				boolean circle_end = false, slide_end = false;
				g.setColor(new Color(0,0,0,100));
				g.fillRect(0 , 0, this.getSize().width, this.getSize().height);
				mousePos.isClick = false;
				for (int i = circleList.size() - 1; i >= 0; i--){
					value = (circleList.get(i)).drawCircle(g);
					/*
					-3表示還沒顯示 :- currTime < perfectTime - 15 * accTime
					-2表示是顯示狀態 :- -15 * accTime ~ +6 * accTime + duringEndTime(EndTime)
					-1表示顯示完circle+圈+分數orMISS :- currTime > EndTime
					>= 0 顯示 + 回傳分數
					*/
					if(value > 0){
						if(value >= 300){
							perfect++;
						}
						else if(value >= 100){
							great++;
						}
						else if(value >= 50){
							good++;
						}
						combo_opa = 200;
						combo++;
						score += value;
						fireCnt+=1;
					}
					else if(value == 0){
						fireCnt-=5;
						if(combo > Maxcombo){
							Maxcombo = combo;
						}
						combo = 0;
						miss++;
					}
					if(value == -1 && i == circleList.size() - 1){
						//System.out.println("Circle -1");
						circle_end = true;
					}
				}
				mousePos.isClick = false;
				for (int i = slideList.size() - 1; i >= 0; i--){
					value = (slideList.get(i)).drawSlide(g);
					if(value > 0){
						if(value >= 300){
							perfect++;
						}
						else if(value >= 100){
							great++;
						}
						else if(value >= 50){
							good++;
						}
						combo_opa = 200;
						combo++;
						score += value;
						fireCnt+=1;
					}
					else if(value == 0){
						fireCnt-=5;
						if(combo > Maxcombo){
							Maxcombo = combo;
						}
						combo = 0;
						miss++;
					}
					if(i == slideList.size() - 1 && value == -1){
						//System.out.println("Slide -1");
						slide_end = true;
					}
				}
				if(circle_end && slide_end && !result_score){
					//System.out.println("Both -1");
					result_score = true;
					result_latency = System.currentTimeMillis() + 5000;
				}
				runScore += ((int)((double)(score - runScore) * 0.1));
				runScore += 1;
				if(runScore > score){
					runScore = score;
				}
				if(fireCnt > 100){
					fireCnt = 100;
				}
				else if(fireCnt < 0){
					fireCnt = 0;
				}
				//System.out.print(scrH - (scrH * fireCnt / 100)+" "+scrH +" " +fireCnt+"\n");
				combo_opa -= 1;
				if(combo_opa < 0){
					combo_opa = 0;
				}
				g.setColor(new Color(fireCnt*colorR/100,fireCnt*colorG/100,fireCnt*colorB/100,200));
				g.fillRect(0, scrH - (scrH * fireCnt / 100), 50, scrH * fireCnt / 100);
				g.fillRect(scrW-50, scrH - (scrH * fireCnt / 100), 50, scrH * fireCnt / 100);

				g.setColor(new Color(255,255,255,combo_opa));
				g.setFont(new Font("Arial", Font.BOLD, 50));
				g.drawString(String.valueOf(combo), scrW/2-(String.valueOf(combo)).length()*8,100);
				
				g.setColor(new Color(255,255,255,200));
				g.setFont(new Font("Arial", Font.BOLD, 70));
				g.drawString(String.valueOf(runScore), scrW-(String.valueOf(runScore)).length()*50,70);
			}
		}
		
		public class PlayingThread extends Thread{
			private boolean interr;
			public PlayingThread(){
				interr = true;
				this.start();
			}
			public void run(){
				while (interr){
					repaint();
					try {
						Thread.sleep(10);
						//System.out.println(System.currentTimeMillis());
					} catch(Exception ex){};
				}
			}
			public void interrupt(){
				interr = false;
			}
		}
		
		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int tmpDis = (int)Math.sqrt((x)*(x)+(y-scrH)*(y-scrH));
			if(tmpDis > minDis_arc && tmpDis < rangeDis_arc){
				Color c = JColorChooser.showDialog(jf, "Choose Color", jf.getContentPane().getBackground());
				colorR = c.getRed();
				colorG = c.getGreen();
				colorB = c.getBlue();
				repaint();
			}
		}
		
		public void mousePressed(MouseEvent e) {
			y = e.getY();
			click = true;
			try {
				selecMove.interrupt();
			}catch(Exception ex){};
		}
	 
		public void mouseReleased(MouseEvent e) {
			//recty = (( recty - this.getSize().height/2 ) / 110 ) * 110 + this.getSize().height/2 - 55;
			if (!isplaying){
				selecMove = new SelecMoveThread((( recty - this.getSize().height/2 ) / songBarDistance ) * songBarDistance + this.getSize().height/2 - songBarDistance/2);
				if (click){
					int i = 0;
					for (SongData d : songList){
						if (d.y < -songBarHeight || d.y  > this.getSize().height){
						i++;
						continue;
						}
						else {
							if (e.getX() > d.x && e.getX() < d.x + songBarWidth && e.getY() > d.y && e.getY() < d.y + songBarHeight){
								if (i == ( this.getSize().height/2 - recty ) / songBarDistance ){
									//System.out.println(i);
									isplaying = true;
									repaint();
									
									//init------------------------
									score = 0;
									runScore = 0;
									runResScore = 0;
									fireCnt = 100;
									Maxcombo = combo = perfect = great = good = 0;
									run_combo = run_perfect = run_great = run_good = run_miss = 0;
									exit_opa = 0;
									exit_opaUp = true;
									mousePos.isClick = result_score = false;
									while(this.getKeyListeners().length > 1) {
										this.removeKeyListener((this.getKeyListeners())[(this.getKeyListeners().length - 1)]);
									}
									while(this.getMouseListeners().length > 1) {
										this.removeMouseListener((this.getMouseListeners())[(this.getMouseListeners().length - 1)]);
									}
									while(this.getMouseMotionListeners().length > 1) {
										this.removeMouseMotionListener((this.getMouseMotionListeners())[(this.getMouseMotionListeners().length - 1)]);
									}
									//座標放大比率
									double radioX = (double)getWidth() / 600;
									double radioY = (double)getHeight() / 600;
									//radioX = radioY = 1;
									//System.out.print(radioX+" "+radioY);
									//----------------------------
									try{
										tempPlayer.close();
									}catch(Exception ex){}
									
									if(tempPlayer != null){
										try{
											tempPlayer.close();
										}catch(Exception ex){
											System.out.println(ex.toString());
										}
										tempPlayer = null;
									}
									try{
										tempPlayer = new PlayerThread(d.songFilePath + "/" + d.audioName, d.audioName, 5000);
										current_songName = d.audioName.substring(0,(d.audioName).indexOf("."));
									}catch(Exception ex){}
									
									/////////*
									circleList.clear();
									long currTime = System.currentTimeMillis();
									Scanner reader = null;
									try {
										reader = new Scanner(new File(d.songFilePath + "/" + d.songFileName));
									}catch(Exception ex){}
									boolean isHitObjects = false, isTimingPoints = false;
									
									ArrayList<String> timingPoints = new ArrayList<String>();
									double accTime = 0,orgAccTime = 0,SliderMultiplier = 0,beatLength = 0;
									String tempstr;
									while (reader.hasNextLine()){
										tempstr = reader.nextLine();
										if (tempstr.indexOf("[HitObjects]") >= 0){
											isHitObjects = true;
											isTimingPoints = false;
										}
										else if (tempstr.indexOf("SliderMultiplier:") >= 0){
											SliderMultiplier = Double.parseDouble(tempstr.substring(17));
										}
										else if (tempstr.indexOf("[TimingPoints]") >= 0){
											isTimingPoints = true;
											isHitObjects = false;
										}
										else if ("".equals(tempstr)){
											isTimingPoints = false;
											isHitObjects = false;
										}
										else if (isTimingPoints) {
											timingPoints.add(tempstr);
										}
										else if (isHitObjects) {
											String [] tempstrarr = tempstr.split(",");
											beatLength = 0;
											for (String timep : timingPoints){
												//System.out.println(timep);
												String [] timeparray = timep.split(",");
												if (beatLength == 0){
													beatLength = Double.parseDouble(timeparray[1]);
													try {
														orgAccTime = beatLength / Double.parseDouble(timeparray[2]);
													}catch(Exception ex){
														orgAccTime = beatLength / 4;
													}
													accTime = orgAccTime;
												}
												if (Integer.parseInt(tempstrarr[2]) >= Double.parseDouble(timeparray[0])){
													if (accTime != 0){
														try {
															if (Double.parseDouble(timeparray[1]) > 0){
																beatLength = Double.parseDouble(timeparray[1]);
																try {
																	orgAccTime = beatLength / Double.parseDouble(timeparray[2]);
																}catch(Exception ex){
																	orgAccTime = beatLength / 4;
																}
																accTime = orgAccTime;
															}
															else {
																accTime = (beatLength * Double.parseDouble(timeparray[1])/-100) / Double.parseDouble(timeparray[2]);
															}
														}catch(Exception ex){
															accTime = (beatLength * Double.parseDouble(timeparray[1])/-100) / 4;
														}
														//System.out.println(beatLength + " * " + Double.parseDouble(timeparray[1]) + " = " + (beatLength * Double.parseDouble(timeparray[1])));
														
													}
												}
											}
											//System.out.println(beatLength + " " + accTime);
											ArrayList < BzPoint > sliderPointList = new ArrayList< BzPoint >();
											if (tempstrarr.length > 6){
												String [] sliderPoint = tempstrarr[5].split("B|C|L|P|\\||:");
												sliderPointList.add(new BzPoint(Integer.parseInt(tempstrarr[0]),Integer.parseInt(tempstrarr[0])));
												for (int j = 2; j < sliderPoint.length; j += 2){
													sliderPointList.add(new BzPoint(Integer.parseInt(sliderPoint[j]), Integer.parseInt(sliderPoint[j+1])));
												}
												slideList.add(new Slide(sliderPointList, currTime + Integer.parseInt(tempstrarr[2]) + 5000, Double.parseDouble(tempstrarr[7]) / (SliderMultiplier * 100) * beatLength,accTime, colorR, colorG, colorB, mousePos, this));
											}
											else {
												circleList.add(new Circle(((int)(Double.parseDouble(tempstrarr[0])*radioX))+50, ((int)(Double.parseDouble(tempstrarr[1])*radioY))+50, currTime + Integer.parseInt(tempstrarr[2]) + 5000,
												accTime, colorR, colorG, colorB, mousePos, this));
											}
											
										}
									}
									
									playingThread = new PlayingThread();
									/////////*/
								}
								else {
									try{
										selecMove.interrupt();
									}catch(Exception ex){
										System.out.println(ex.toString());
									};
									click = false;
									selecMove = new SelecMoveThread(i * -songBarDistance + this.getSize().height/2 - songBarDistance/2);
									if (!tempPlayer.audioName.equals(d.audioName)){
										if(tempPlayer != null){
											try{
												tempPlayer.close();
											}catch(Exception ex){
												System.out.println(ex.toString());
											}
											tempPlayer = null;
										}
										try{
											tempPlayer = new PlayerThread(d.songFilePath + "/" + d.audioName, d.audioName);
										}catch(Exception ex){}
									}
								}
								break;
							}
						}
						i++;
					}
				}
				else if (! tempPlayer.audioName.equals(songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).audioName)){
					try{
						tempPlayer.close();
					}catch(Exception ex){}
					try{
						tempPlayer = new PlayerThread(songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).songFilePath + "/" + songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).audioName, 
						songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).audioName);
					}catch(Exception ex){}
				}
			}
		}
		class PlayerThread extends Thread{
			private Player threadplayer;
			public String audioName;
			private String audioPath;
			private int delayTime;
			public PlayerThread(String audioPath, String audioName){
				this(audioPath, audioName, 0);
			}
			public PlayerThread(String audioPath, String audioName, int delayTime){
				this.delayTime = delayTime;
				this.audioName = audioName;
				this.audioPath = audioPath;
				this.start();
			}
			public void run(){
				try{
					Thread.sleep(delayTime);
				}catch(Exception ex){};
				try{
					threadplayer = new Player(new FileInputStream(new File(audioPath)));
					threadplayer.play();
				}catch(Exception ex){};
			}
			public void close(){
				audioPath = "";
				audioName = "";
				try{
					threadplayer.close();
				}catch(Exception ex){};
			}
			public int getPosition(){
				return threadplayer.getPosition();
			}
		}
		class SelecMoveThread extends Thread{
			private int y;
			private double dy;
			private boolean interr;
			public SelecMoveThread(int y){
				this.y = y;
				dy = recty;
				interr = true;
				this.start();
			}
			public void run(){
				while (interr && click != true && y != recty){;
					dy += (y - dy)/20;
					recty = (int)dy;
					repaint();
					try {
						Thread.sleep(10);
					} catch(Exception ex){}
				}
			}
			public void interrupt(){
				interr = false;
			}
		}
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
			hover = -1;
			repaint();
		}
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			mousePos.x = x;
			mousePos.y = y;
			
			
			int tmpDis = (int)Math.sqrt((x)*(x)+(y-scrH)*(y-scrH));
			
			//System.out.println(x+" "+(scrH-y));
			//System.out.println(minDis_arc+" "+tmpDis+" "+rangeDis_arc+" "+setting_acr_radius+" "+run_setting_acr_radius);
			
			if(tmpDis > minDis_arc && tmpDis < rangeDis_arc){
				//System.out.println("In range");
				hover_color = true;
			}
			else{
				hover_color = false;
			}
			if (!isplaying){
				int i = 0;
				for (SongData d : songList){
					if (d.y < -songBarHeight || d.y  > this.getSize().height){
					i++;
					continue;
					}
					else {
						if (e.getX() > d.x && e.getX() < d.x + songBarWidth && e.getY() > d.y && e.getY() < d.y + songBarHeight){
							hover = i;
							break;
						}
					}
					i++;
				}
				if (i == songList.size()){
					hover = -1;
				}
				repaint();
			}
		}
		public void mouseDragged(MouseEvent e) {
			if (!isplaying){
				recty += e.getY() - y;
				if (recty > this.getSize().height/2){
					recty = this.getSize().height/2;
				}
				if (recty < songList.size() * -songBarDistance + this.getSize().height/2+1){
					recty = songList.size() * -songBarDistance + this.getSize().height/2+1;
				}
				y = e.getY();
				repaint();
				click = false;
			}
		}
		
		public void keyTyped(KeyEvent e){
		}
		public void keyPressed(KeyEvent e){
			//System.out.println(e.getKeyCode());
			if (e.getKeyCode() == 27 && isplaying == true){
				isplaying = false;
				current_songName = "";
				playingThread.interrupt();
				circleList.clear();
				slideList.clear();
				try{
					tempPlayer.close();
				}catch(Exception ex){
					System.out.println(ex.toString());
				}
				try{
					tempPlayer = new PlayerThread(songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).songFilePath + "/" + songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).audioName, 
					songList.get((recty - this.getSize().height/2 )/ -songBarDistance ).audioName);
				}catch(Exception ex){
					System.out.println(ex.toString());
				}
				repaint();
			}
			else if (e.getKeyCode() == 27 && isplaying == false){
				System.exit(0);
			}
			else if (e.getKeyCode() == 122 && isplaying == false){//全螢幕
				Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
				int srcW = (int)screenSize.getWidth(), srcH = (int)screenSize.getHeight();
				int centerX =  srcW/ 2, centerY = srcH/2;
				if(jf.fullSrc){
					jf.setBounds(centerX/3,centerY/3, srcW*2/3, srcH*2/3);
				}
				else{
					jf.setBounds(0,0, srcW, srcH);
				}
				jf.fullSrc = !jf.fullSrc;
			}
		}
		public void keyReleased(KeyEvent e){
		}
	}
}
class MousePos{
	int x, y;
	boolean isClick;
	public MousePos(){
		x = y = 0;
		isClick = false;
	}
}

class Circle {
	private int x, y, r, g, b, circle_transparent, ringSize, radius;
	MousePos mousePos = null;
	private int score = 0;
	private double accTime;
	private long perfectTime, ct;
	private String msg = "";
	private int ring_thickness = 10;
	private int ring_transparency = 0;
	private final int circleSize = 120;
	private long EndTime = -1;
	private final long duringEndTime = 1000;
	public boolean isClick = false;
	private JPanel jp = null;
	private CircleMouseEvent CME = null;
	private CircleKeyEvent CKE = null;
	public Circle(int x, int y, long perfectTime, double accTime, int r, int g, int b, MousePos mousePos, JPanel p){
		this.x = x;
		this.y = y;
		this.accTime = accTime;
		this.r = r;
		this.g = g;
		this.b = b;
		this.mousePos = mousePos;
		this.perfectTime = perfectTime;
		//ringSize = circleSize*3;
		jp = p;
		EndTime = duringEndTime + perfectTime + ((long)(6 * accTime));
		ct = System.currentTimeMillis();
		//System.out.println(x + " " + y + " " + perfectTime + " " + ct);
	}
	public double getAccTime(){
		return accTime;
	}
	public int drawCircle(Graphics gra){
		ct = System.currentTimeMillis();
		//System.out.print("x: "+x+" y: "+y+"\n");
		Graphics2D g2d = (Graphics2D) gra;
		int opa = 0;
		boolean gotScore = false;
		//System.out.println(x + " " + y + " " + perfectTime + " " + accTime);
		if(ct < perfectTime - accTime * 15){
			//System.out.print("Score: "+score+"\n");
			return -3;
		}
		else if (ct > EndTime){
			return -1;
		}
		else if(ct > EndTime - duringEndTime){
			if(msg == ""){
				msg = "MISS";
				score = 0;
				gotScore = true;
			}
			isClick = true;
			opa = (int)( ((EndTime - ct) * 255 / duringEndTime ) );
			//System.out.print(opa+"--------------------------------------------------------------------------------------\n");
			if(opa>255){
				opa=255;
			}
		}
		else if(ct < perfectTime){
			//System.out.print("in\n");
			if(CME == null){
				CME = new CircleMouseEvent();
				jp.addMouseListener(CME);
			}
			if(CKE == null){
				CKE = new CircleKeyEvent();
				jp.addKeyListener(CKE);
			}
			radius = (int)(((perfectTime-ct)/((double)accTime*15))*(circleSize)) + circleSize/2;
			ring_transparency = (int)(255 - ((perfectTime-ct)/((double)accTime*15))*255);
			ring_thickness = (int)( 5.0 + ((double)(perfectTime-ct)/((double)accTime*15))* 10.0 );
			if(perfectTime - ct > accTime*8){
				//System.out.println(255 - (int)(((perfectTime-ct-accTime*8)/(double)accTime*7)*255));
				opa = 255 - (int)(((perfectTime-ct-accTime*8)/((double)accTime*7))*200);
				score = -2;
				//System.out.println(radius);
				//System.out.print("2"+" ");
			}
			else if(perfectTime > ct){
				opa = 200;
				//System.out.print("3"+" ");
			}
		}
		else if(ct >= perfectTime){
			if(isClick){
				opa = 0;
			}
			else{
				opa = (int)( ((perfectTime + 6 * accTime - ct) * 255 / (6 * accTime) ) );
			}
			radius = circleSize/2 + 2;
			
		}
		if(!isClick){
			g2d.setColor(new Color(255,255,255, opa));
			g2d.fillOval(x-circleSize/2, y-circleSize/2, circleSize, circleSize);
			g2d.setColor(new Color(169,169,169, opa));
			g2d.fillOval(x-circleSize/2+5, y-circleSize/2+5, circleSize-10, circleSize-10);
			g2d.setColor(new Color(r,g,b, opa));
			g2d.fillOval(x-circleSize/2+6, y-circleSize/2+6, circleSize-12, circleSize-12);
			g2d.setColor(new Color(r,g,b, ring_transparency));
			Shape outer = new Ellipse2D.Double(x - radius-ring_thickness, y - radius-ring_thickness, 2*(radius + ring_thickness), 2*(radius + ring_thickness));
			Shape inner = new Ellipse2D.Double(x - radius, y - radius, 2*radius , 2*radius);
			Area circle = new Area( outer );
			circle.subtract( new Area(inner) );
			g2d.translate(0,0);
			g2d.fill(circle);
		}
		g2d.setColor(new Color(255,255,255, opa));
		g2d.setFont(new Font("Arial", Font.PLAIN, 30));
		g2d.drawString(msg, x-(8*msg.length()),y+8);
		//System.out.print("EET: "+EndTime+"\n");
		//System.out.print("ENDT: "+System.currentTimeMillis()+"\n\n");
		if(score >= 0){
			int tmp = score;
			score = -1;
			return tmp;
		}
		return score;
	}
	public class CircleMouseEvent extends MouseAdapter{
		public void mousePressed(MouseEvent e) {
			if (isClick || mousePos.isClick){
				return;
			}
			//System.out.printf("456\n");
			int disX = (x - mousePos.x) * (x - mousePos.x), disY = (y - mousePos.y) * (y - mousePos.y);
			if(disX + disY <= circleSize*circleSize/4){
				refreshScore();
			}
		}
	}
	public class CircleKeyEvent extends KeyAdapter {
		boolean release = true;
		public void keyPressed(KeyEvent e) {
			if (isClick || mousePos.isClick){
				return;
			}
			if(release){
				//System.out.print(mousePos.x + " " + mousePos.y);
				int disX = ( mousePos.x - x) * (mousePos.x - x), disY = (mousePos.y - y) * (mousePos.y - y);
				if(disX + disY <= circleSize*circleSize/4){
					refreshScore();
				}
			}
		}
		public void keyReleased(KeyEvent e) {
			//System.out.print(4);
			release = true;
		}
	}
	
	public void refreshScore(){
		ct = System.currentTimeMillis();
		
		if(perfectTime - ct > accTime*8){
			//System.out.print(accTime + " ");
			return;
		}
		else if(perfectTime - ct > accTime*6){
			msg = "MISS";
			score = 0;
		}
		else if(perfectTime - ct > accTime*4){
			msg = "50";
			score = 50;
		}
		else if(perfectTime - ct > accTime*2){
			msg = "100";
			score = 100;
			
		}
		else if(perfectTime - ct > -accTime*2){
			msg = "300";
			score = 300;
		}
		else if(perfectTime - ct > -accTime*4){
			msg = "100";
			score = 100;
		}
		else if(perfectTime - ct > -accTime*6){
			msg = "50";
			score = 50;
		}
		else if(perfectTime - ct > -accTime*8){
			msg = "MISS";
			score = 0;
		}
		//System.out.print("Clicked");
		if(CME != null){
			jp.removeMouseListener(CME);
			CME = null;
		}
		if(CKE != null){
			jp.removeKeyListener(CKE);
			CKE = null;
		}
		EndTime = System.currentTimeMillis() + duringEndTime;
		mousePos.isClick = true;
		isClick = true;
	}
	
}



class Slide{
	double time_accuracy = 0.005;
	int remain_ring;
	int R, G, B;
	int transparency = 0, currentArea_transparency = 0, score_transparency = 0;
	int ring_thickness = 10;
	int runLast = 0;
	int runLisLast = 0;
	int score = 0;
	long during_time, time_start, time_perfect, time_finsh, time_end, time_score_end;
	private final int dis_extend_ring = 240;
	private final long duringTime_end = 500;
	private final long duringTime_score = 1000;
	double accTime;
	final private double time_check_point = 0.25;
	
	
	Area slideArea = null;
	Area CurrentSlideArea = null;
	private JPanel jp = null;
	private MousePos mousePos = null;
	private SlideMouseMotionEvent SMME = null;
	private SlideKeyEvent SKE = null;
	private boolean[] isCheck = null;
	
	
	ArrayList<BzPoint> BzPointResult;
	
	
	
	final int circle_diameter = 120;
	
	Slide(ArrayList<BzPoint> BzRefPoints, long pt, double dt, double acc, int r, int g, int b, MousePos mp, JPanel p){
		time_perfect = pt;
		during_time = (long)dt;
		accTime = acc;
		time_start = (long)(time_perfect - 15 * accTime);
		time_finsh = time_perfect + during_time;
		time_end = time_finsh + duringTime_end;
		time_score_end = time_end + duringTime_score;
		BzPointResult = new ArrayList<BzPoint>();
		R=r;
		G=g;
		B=b;
		transparency = 10;
		currentArea_transparency = 200;
		jp = p;
		mousePos = mp;
		//currAreaLast = 0;
		
		double radioX = (double)(p.getWidth()) / 600;
		double radioY = (double)(p.getHeight()) / 600;
		int n = BzRefPoints.size() - 1;
		double tmpx = 0, tmpy = 0, t = 0;
		while(t <= 1){
			tmpx = 0;
			tmpy = 0;
			for(int i=0;i<=n;i++){
				tmpx += ((Cnk(n,i)*BzRefPoints.get(i).x*(Math.pow(t,i)*Math.pow(1-t,n-i))));
				tmpy += ((Cnk(n,i)*BzRefPoints.get(i).y*(Math.pow(t,i)*Math.pow(1-t,n-i))));
			}
			BzPointResult.add(new BzPoint((int)(tmpx*radioX)+50,(int)(tmpy*radioY)+50));
			t+=time_accuracy;
		}
			//System.out.print("Size:  "+BzPointResult.size()+"------------\n");
			/*int innerSize = (circle_diameter) - (2 * ring_thickness);
			Shape inner;
			Shape outer;
			slideArea = new Area();
			Area inArea = new Area();
			Area outArea = new Area();
			for(int i = 0;i < BzPointResult.size();i++){
				outer = new Ellipse2D.Double(BzPointResult.get(i).x-(circle_diameter+10)/2, BzPointResult.get(i).y-(circle_diameter+10)/2, circle_diameter+10, circle_diameter+10);
				slideArea.add(new Area(outer));
			}
			for(int i = 0;i < BzPointResult.size();i++){
				inner = new Ellipse2D.Double(BzPointResult.get(i).x-circle_diameter/2, BzPointResult.get(i).y-circle_diameter/2, circle_diameter, circle_diameter);
				inArea.add(new Area(inner));
			}
			slideArea.subtract( inArea );*/
			
			
		if(slideArea == null){
			Thread thread = new Thread(new Runnable(){
				@Override
				public void run() {
					try {//////////////////
						int innerSize = (circle_diameter) - (2 * ring_thickness);
						Shape inner;
						Shape outer;
						slideArea = new Area();
						Area inArea = new Area();
						Area outArea = new Area();
						for(int i = 0;i < BzPointResult.size();i++){
							outer = new Ellipse2D.Double(BzPointResult.get(i).x-(circle_diameter+10)/2, BzPointResult.get(i).y-(circle_diameter+10)/2, circle_diameter+10, circle_diameter+10);
							slideArea.add(new Area(outer));
						}
						for(int i = 0;i < BzPointResult.size();i++){
							inner = new Ellipse2D.Double(BzPointResult.get(i).x-circle_diameter/2, BzPointResult.get(i).y-circle_diameter/2, circle_diameter, circle_diameter);
							inArea.add(new Area(inner));
						}
						slideArea.subtract( inArea );
					} catch (Exception e) {
					  System.out.println("執行緒中斷了...");
					  return;
					}
				}
			});
			thread.start();
		}
			
		if(CurrentSlideArea == null){
			CurrentSlideArea = new Area();
			CurrentSlideArea.add(new Area(new Ellipse2D.Double(BzPointResult.get(0).x-circle_diameter/2 - 1, BzPointResult.get(0).y-circle_diameter/2 - 1, circle_diameter + 2, circle_diameter + 2)));
		}
		int s = BzPointResult.size() - 1;
		int len = ( (int)( (1.0/time_check_point) + 1 ) );
		isCheck = new boolean[len];
	}
	
	private double Cnk(int n, int k){
		double res = 1;
		if(n < k){
			return -1;
		}
		for(int i = k+1;i<=n;i++){
			res*=i;
		}
		for(int i = n-k;i>0;i--){
			res/=i;
		}
		return res;
	}
	
	/*回傳值
	-3還沒出現
	-2正在顯示
	-1過時
	>=0分數
	*/
	int drawSlide(Graphics g){
		//Calculate data
		boolean exist_CurrSlide = false;
		boolean exist_ring = false;
		boolean exist_score = false;
		long ct = System.currentTimeMillis();
		if(ct < time_start){
			//System.out.print("太早\n");
			return -3;
		}
		else if(ct < time_perfect){//縮圈
			//System.out.print("縮圈\n");
			if(SMME == null){
				
				SMME = new SlideMouseMotionEvent();
				jp.addMouseMotionListener(SMME);
			}
			if(SKE == null){
				
				SKE = new SlideKeyEvent();
				jp.addKeyListener(SKE);
			}
			int during_pt_st = (int)(time_perfect - time_start);
			int during_pt_ct = (int)(time_perfect - ct);
			exist_ring = true;
			remain_ring = ( during_pt_ct * dis_extend_ring / during_pt_st );
			ring_thickness = ( 5 + ( during_pt_ct * 10) / during_pt_st );
			transparency = ( 200 - ( during_pt_ct * 200 ) / during_pt_st );
			//System.out.print("環: "+(remain_ring + circle_diameter)+"\n");
		}
		else if(ct < time_finsh){//跑滑條
			//System.out.print("跑滑條\n");
			int size = BzPointResult.size();
			int cntLast = ((int)(ct - time_perfect) * size / (int)(during_time));
			//System.out.print("St-Tp: "+(ct - time_perfect)+"\nduring_time: "+during_time+"\nSize: "+size+"\nCntLast: "+cntLast+"\n\n");
			exist_CurrSlide = true;
			for(int i = runLast; i < cntLast && i < size;i++){
				CurrentSlideArea.add(new Area(new Ellipse2D.Double(BzPointResult.get(i).x-circle_diameter/2 - 1, BzPointResult.get(i).y-circle_diameter/2 - 1, circle_diameter + 2, circle_diameter + 2)));
			}
			runLast = cntLast;
			int tmpLast = (int)((double)runLast * ( 1.0 / time_check_point + 1.0) / (double)BzPointResult.size()); 
			for(int i = runLisLast; i<tmpLast; i++){
				if(i > 0){
					isCheck[i - 1] = true;
				}
			}
			runLisLast = tmpLast;
			//System.out.print("run: "+runLisLast+"\n");
			if(runLisLast >= isCheck.length){
				runLisLast = isCheck.length - 1;
			}
			else if(runLisLast < 0){
				runLisLast = 0;
			}
		}
		else if(ct < time_end){//消失
			if(runLast <  BzPointResult.size()){
				for(int i = runLast; i < BzPointResult.size();i++){
					CurrentSlideArea.add(new Area(new Ellipse2D.Double(BzPointResult.get(i).x-circle_diameter/2 - 1, BzPointResult.get(i).y-circle_diameter/2 - 1, circle_diameter + 2, circle_diameter + 2)));
				}
				runLast = BzPointResult.size() - 1;
				runLisLast = isCheck.length - 1;
			}
			//System.out.print("消失\n");
			int during_et_ft = (int)(time_end - time_finsh);
			exist_CurrSlide = true;
			transparency = ( (int)(time_end - ct) * 200 / during_et_ft );
			currentArea_transparency = transparency;
		}
		else if(ct < time_score_end){//分數顯示
			exist_score = true;
			score_transparency = (int)( (double)(time_score_end - ct) / (double)duringTime_score * 250.0 );
		}
		else{//不存在(過時)
			if(SMME != null){
				jp.removeMouseMotionListener(SMME);
			}
			if(SKE != null){
				jp.removeKeyListener(SKE);
			}
			//System.out.print("過時\n");
			if(score >= 0){
				int tmp = score;
				score = -1;
				return tmp;
			}
			return -1;
		}
		
		//draw Slide code
		int n = BzPointResult.size();
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_DEFAULT);
		
		
		
		if(exist_score){//顯示分數
			String msg = "";
			if(score == 0){
				msg = "MISS";
			}
			else{
				if(score >= 200){
					score = 300;
				}
				else if(score >= 60){
					score = 100;
				}
				else if(score > 0){
					score = 50;
				}
				msg = String.valueOf(score);
			}
			g2d.setColor(new Color(255,255,255, score_transparency));
			g2d.setFont(new Font("Arial", Font.PLAIN, 30));
			g2d.drawString(msg, (BzPointResult.get(BzPointResult.size()-1)).x-(8*msg.length()),(BzPointResult.get(BzPointResult.size()-1)).y+8);
		}
		else if(slideArea != null){//滑條外環
			g2d.setColor(new Color(255,255,255,transparency));
			g2d.fill(slideArea);
		}
		//if(exist_CurrSlide){//跑滑條
			g2d.setColor(new Color(R,G,B,currentArea_transparency));
			g2d.fill(CurrentSlideArea);
		//}
		
		if(exist_ring){//縮圈
			int innerSize = (remain_ring + circle_diameter);
			int outSize = innerSize + (2 * ring_thickness);
			Shape outer = new Ellipse2D.Double(BzPointResult.get(0).x - outSize/2, BzPointResult.get(0).y - outSize/2, outSize, outSize);
			Shape inner = new Ellipse2D.Double(BzPointResult.get(0).x - innerSize/2, BzPointResult.get(0).y - innerSize/2, innerSize, innerSize);
			Area circle = new Area( outer );
			circle.subtract( new Area(inner) );
			g2d.setColor(new Color(R,G,B,transparency));
			g2d.fill(circle);
		}
		
		return -2;
	}

	public class SlideMouseMotionEvent extends MouseMotionAdapter{
		public void mouseDragged(MouseEvent e) {
			int last = runLast;//全部點的最後
			int clast = runLisLast;//監聽的最後
			//System.out.printf("Last: "+clast+"\n");
			if (isCheck[clast]){
				return;
			
			}
			int x = (BzPointResult.get(last)).x, y = (BzPointResult.get(last)).y;
			mousePos.x = e.getX();
			mousePos.y = e.getY();
			//System.out.printf("X: "+x+" "+mousePos.x+"\n");
			//System.out.printf("Y: "+y+" "+mousePos.y+"\n\n");
			int disX = (x - mousePos.x) * (x - mousePos.x), disY = (y - mousePos.y) * (y - mousePos.y);
			if(disX + disY <= circle_diameter*circle_diameter/4){
				//System.out.printf("Drag Ckeck: "+clast+"\n");
				isCheck[clast] = true;
				score += ((int)(300 / (1 / time_check_point + 1)));
			}
		}
	}
	public class SlideKeyEvent extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int last = runLast;//全部點的最後
			int clast = runLisLast;//監聽的最後
			//System.out.printf("Key: "+clast+"\n");
			if (isCheck[clast]){
				return;
			
			}
			int x = (BzPointResult.get(last)).x, y = (BzPointResult.get(last)).y;
			//System.out.printf("X: "+x+" "+mousePos.x+"\n");
			//System.out.printf("Y: "+y+" "+mousePos.y+"\n\n");
			int disX = (x - mousePos.x) * (x - mousePos.x), disY = (y - mousePos.y) * (y - mousePos.y);
			if(disX + disY <= circle_diameter*circle_diameter/4){
				//System.out.printf("Key Ckeck: "+clast+"\n");
				isCheck[clast] = true;
				score += ((int)(300 / (1 / time_check_point + 1)));
			}
		}
	}

}

class BzPoint{
	int x, y;
	BzPoint(int m,int n){
		x=m;
		y=n;
	}
}
