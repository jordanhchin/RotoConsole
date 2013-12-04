package rotoConsole;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	/**
	 * @param args
	 */
	static String[] catLabels = {"G", "A", "+/-", "PTS", "PIM", "PPG", "PPA", "SHG", "SHA", "GWG", "FOW", "FOL", "SHFT", "TOI", "ATOI", "HAT", "SOG", "HIT", "BLK", "DEF", "STG", "STA", "STP", "PPP", "SHP", "GS", "W", "L", "SA", "GA", "EGA", "SV", "SO", "MIN", "OTL", "GAA", "SV%", "GW%" };
	static String url = "http://games.espn.go.com/fhl/standings?leagueId=16080&seasonId=2014";
	static int teamCnt = 12;
	
	public static void main(String[] args) throws IOException{
		
		boolean[] catUsed = new boolean[38];
		String[] teams = new String[teamCnt];
		
		int totalCats = loadCats(catUsed);
		String[][] stats = new String[teamCnt][totalCats];
		float[][] rotoScores = new float[teamCnt][totalCats];
		String[] catTable = new String[totalCats];
		boolean[] invertedCat = new boolean[totalCats];
		boolean[] useFloat = new boolean[totalCats];
		loadCatTable(catTable, invertedCat, useFloat);
		float[] finalScore = new float[teamCnt];
		int [] finalRank = new int[teamCnt];
		
		loadTeams(teams);
		//showCats(catUsed);
		loadStats(catUsed, stats, totalCats);
		scoreStats(catUsed, stats, totalCats, teams, catTable, invertedCat, useFloat, rotoScores, finalScore, finalRank);
		showTable(stats, totalCats, teams, catTable, rotoScores, finalScore, finalRank);
		//showTeamInfo(stats, teams, 0);
		//showTeams(teams);
		//showCats(catUsed);
	}

	public static void showTable(String[][] stat, int catCount, String[] teamNames, String[] catTable, float[][] rotoScores, float[] finalScore, int[] finalRank) {
		System.out.printf("Team                     ");
		for (int i = 0; i < catCount; i++)
			System.out.printf("%-6s ", catTable[i]);
		System.out.printf("%-6s ", "Score");
		System.out.printf("%-6s \n", "Rank");
		for (int i = 0; i < teamCnt; i++) {
			System.out.printf("%-25s", teamNames[i]);
			for (int j = 0; j < catCount; j++) {
				//System.out.printf("%-6s ", stat[i][j]);
				System.out.printf("%-6s ", rotoScores[i][j]);
			}
			System.out.printf("%-6s ", finalScore[i]);
			System.out.printf("%-6s \n", finalRank[i]);
		}
	}
	
	public static void scoreStats(boolean[] cats, String[][] stat, int catCount, String[] teamNames, String[] catTable, boolean[] invertedCat, boolean[] useFloat, float[][] rotoScores, float[] finalScore, int[] finalRank) {
		float[] tempFloatArray = new float[teamCnt];
		String[] tempStringArray = new String[teamCnt];
		float[] tempRotoScore = new float[teamCnt];
		int check = 11;
		
		// This FOR loop populates individual roto scores
		for (int k = 0; k < catCount; k++) {
		
			if (useFloat[k]) {
				for (int i = 0; i < teamCnt; i++)
					tempFloatArray[i] = Float.valueOf(stat[i][k]);
					
				Arrays.sort(tempFloatArray);
				
				//if (k == check)
				//	for (int i = 0; i < teamCnt; i++)
				//		System.out.println(tempFloatArray[i]);
			
				for (int i = 0; i < teamCnt; i++) {
					for (int j = 0; j < teamCnt; j++) {
						if (tempFloatArray[j] == Float.valueOf(stat[i][k])) {
							if (invertedCat[k])
								tempRotoScore[i] = j+1;
							else
								tempRotoScore[i] = 12-j;
						}
					}
				}
			}
			else {
				for (int i = 0; i < teamCnt; i++)
					tempStringArray[i] = stat[i][k];
					
				Arrays.sort(tempStringArray);
				
				if (k == check)
					for (int i = 0; i < teamCnt; i++)
						System.out.println(tempStringArray[i]);
			
				for (int i = 0; i < teamCnt; i++) {
					for (int j = 0; j < teamCnt; j++) {
						if (tempStringArray[j].equals((stat[i][k]))) {
							if (invertedCat[k])
								tempRotoScore[i] = j+1;
							else
								tempRotoScore[i] = 12-j;
						}
					}
				}				
			}
			
			for (int i = 0; i < teamCnt; i++) {
				float dupes = 0;
				for (int j = 0; j < teamCnt; j++) {
					if (tempRotoScore[i] == tempRotoScore[j]) {
						dupes++;
					}
				}
				rotoScores[i][k] = tempRotoScore[i] + (dupes-1)/2;
			}
						
			//System.out.println(catTable[k]);
			//for (int i = 0; i < teamCnt; i++)
				//System.out.println(teamNames[i] + " - " + stat[i][k] + " - " + rotoScores[i][k]);
			//System.out.println(""); 
			
		} // End FOR(k) loop
		
		// This FOR loop determines final roto score
		for (int i = 0; i < teamCnt; i++) {
			for (int j = 0; j < catCount ; j++) 
				finalScore[i] = finalScore[i] + rotoScores[i][j];
			//System.out.println(teamNames[i] + " - " + finalScore[i]);
		}
		
		// Sort for final roto ranking
		for (int i = 0; i < teamCnt; i++)
			tempFloatArray[i] = finalScore[i];
		Arrays.sort(tempFloatArray);
		for (int i = 0; i < teamCnt; i++) {
			for (int j = 0; j < teamCnt; j++) {
				if (tempFloatArray[j] == finalScore[i]) {
					finalRank[i] = j+1;
					break;
				}
			}
			//System.out.println(teamNames[i] + " - " + finalRank[i]);
		}		
	}
	
	public static void loadStats(boolean[] cats, String[][] stat, int catCount) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Elements query = doc.select("TD[id]");
		
		int position = 0;
		int currentTeam = 0;
		int statIndex = 0;
		
		// Initialize Team Index (positions in jSoup query) 
		int tmp = 1;
		int[] teamIndex = new int[teamCnt];
		for (int i = 0; i < teamCnt; i++) {		
			teamIndex[i] = tmp;
			tmp = tmp + 13;
		}
		
		jsouploop:
		for (Element data : query) {
			// Skip junk header
			if (position == 0) {
				position++;
				continue;
			}
			// Keep track of which team we're importing stats for based on teamIndex
			for (int i = 0; i < teamCnt; i++) {
				if (teamIndex[i] == position) {
					currentTeam = i;
					statIndex = 0;
					position++;				
					continue jsouploop;
				}
			}
			stat[currentTeam][statIndex] = data.text();
			//System.out.println("position=" + position + " currentTeam=" + currentTeam + " statIndex=" + statIndex + " data.text()=" + data.text());
			statIndex++;
			position++;
		}
			
		
	}
		
	public static void loadTeams(String[] teamNames) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Elements query = doc.select("A[title]");
		int i = 0;
		
		for (Element team : query) {
			if (i < teamCnt) {
				teamNames[i] = team.text();
				i++;
			}
		}
	}
	
	public static void showTeams(String[] teamNames) {
		for (int i = 0; i < teamCnt; i++) {
			System.out.println("Team " + i + " = " + teamNames[i]); 
		}	
	}
	
	public static int loadCats(boolean[] cats) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Elements query = doc.select("TD[style]");
		int total = 0;
		
		for (int i = 0; i < 38; i++) {
			cats[i] = false;
		}
		
		for (Element td : query) {
			
			for (int i = 0; i < 38; i++) {
				if (td.text().equals(catLabels[i])) {
					cats[i] = true;
					total++;
					//System.out.println("Found!");
				}
			}
				
		}
		return total;
	}

	public static void loadCatTable(String[] catTable, boolean[] invertedCat, boolean[] useFloat) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Elements query = doc.select("TD[style]");
		String[] invertedTable = {"FOL","L","GA","EGA","OTL","GAA"};
		String[] stringTable = {"TOI","ATOI"};
		int index = 0;
		
		for (Element td : query) {
			
			for (int i = 0; i < 38; i++) {
				if (td.text().equals(catLabels[i])) {
					catTable[index] = td.text();
					//System.out.println("catTable[" + index + "]=" + catTable[index]);
					for (int j = 0; j < invertedTable.length; j++) {
						if (td.text().equals(invertedTable[j]))
							invertedCat[index] = true;
						else
							invertedCat[index] = false;
					}
					for (int j = 0; j < stringTable.length; j++) {
						if (td.text().equals(stringTable[j]))
							useFloat[index] = false;
						else
							useFloat[index] = true;
					}

					//System.out.println("invertedTable[" + index + "]=" + invertedCat[index]);
					index++;
				}
			}				
		}
	}	
	
	public static void showCats(boolean[] cats) {
		for (int i = 0; i < 38; i++) {
			System.out.println(i + ": " + catLabels[i] + cats[i]); 
		}
	}
	
	public static void showTeamInfo(String[][] stat, String[] teamNames, int index) {
		System.out.println(teamNames[index] + "[" + index + "]: ");
		for (int i = 0; i < 38; i++) {
			System.out.println(i + ": " + catLabels[i] + " - " + stat[index][i]); 
		}
				
	}

}
