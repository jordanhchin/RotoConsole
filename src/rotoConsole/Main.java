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
		String[][] stats = new String[teamCnt][38];
		
		loadCats(catUsed);
		loadTeams(teams);
		int totalCats = countCats(catUsed);
		loadStats(catUsed, stats, totalCats);
		scoreStats(catUsed, stats, totalCats, teams);
		//showTeams(teams);
		//showCats(catUsed);
	}

	public static void scoreStats(boolean[] cats, String[][] stat, int catCount, String[] teamNames) {
		String[] tempArray = new String[teamCnt];
		float[] tempRotoScore = new float[teamCnt];
		float[] rotoScore = new float[teamCnt];
		
		for (int i = 0; i < teamCnt; i++) 
			tempArray[i] = stat[i][0];
		
		Arrays.sort(tempArray);
		
		for (int i = 0; i < teamCnt; i++) {
			for (int j = 0; j < teamCnt; j++) {
				if (tempArray[j].equals(stat[i][0]))
					tempRotoScore[i] = 12-j; 
			}
		}

		for (int i = 0; i < teamCnt; i++)
			System.out.println(teamNames[i] + " - " + tempRotoScore[i]);
		System.out.println("");
		
		for (int i = 0; i < teamCnt; i++) {
			float dupes = 0;
			for (int j = 0; j < teamCnt; j++) {
				if (tempRotoScore[i] == tempRotoScore[j]) {
					dupes++;
				}
			}
			rotoScore[i] = tempRotoScore[i] + (dupes-1)/2;
		}

		for (int i = 0; i < teamCnt; i++)
			System.out.println(teamNames[i] + " - " + rotoScore[i]);
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
			// Check if this stat category is used in this league
			while (cats[statIndex] == false)
				statIndex++;
			//System.out.println("position=" + position + " currentTeam=" + currentTeam + " statIndex=" + statIndex + " data.text()=" + data.text());		
			stat[currentTeam][statIndex] = data.text();
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
	
	public static void loadCats(boolean[] cats) throws IOException {
		Document doc = Jsoup.connect(url).get();
		Elements query = doc.select("TD[style]");

		for (int i = 0; i < 38; i++) 
			cats[i] = false;
		
		for (Element td : query) {
			
			//System.out.println(td.text());
			//boolean found = false;
			
			for (int i = 0; i < 38; i++) {
				if (td.text().equals(catLabels[i])) {
					cats[i] = true;
					//System.out.println("Found!");
					//found = true;
				}
			}
				
			//if (found == false)
				//System.out.println("Not found.");
		}
				
		
	}
	
	public static int countCats(boolean[] cats) {
		int total = 0;
		for (int i = 0; i < 38; i++) {
			if (cats[i] == true)
				total++;
		}
		return total;
	}
	
	public static void showCats(boolean[] cats) {
		for (int i = 0; i < 38; i++) {
			System.out.println(catLabels[i] + " = " + cats[i]); 
		}
	}

}
