//===========================================================================================================================
//	Program : Class that performs tokenizing and stop words removal from the collection
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenize {
	private static ArrayList<String> stopWords;
	private static ArrayList<String> tokenisedWords;
	
	public static void main(String[] args) throws IOException {
		int docId = 0;
		//Removal of stop words
		Scanner stopWordFile = new Scanner(new File("stopwords"));
		stopWords = new ArrayList<>();
		tokenisedWords = new ArrayList<>();
		
		while (stopWordFile.hasNext()) {
			stopWords.add(stopWordFile.next());
		}
		String[] idAndNames = null;
		//Reading from cranfield collection
		File folder = new File("Cranfield/");
		
		File[] files = null;
		if (folder.exists() && folder.isDirectory()) {
			files = folder.listFiles();
		}
		for (File inputFile : files) {
			if (inputFile.isFile()) {
				idAndNames = inputFile.getName().split("(?=\\d)(?<!\\d)");
				docId = Integer.parseInt(idAndNames[1]);
				createTokens(inputFile, docId);
			}
		}
		stopWordFile.close();
		//calling lemmatizer 
		Lemmatize.lemmatizer(tokenisedWords);
		//calling stemmer
		Stemmer.stemmerCollection(tokenisedWords);
		//statistics asked in the result
		Lemmatize.printStatistics();
		Stemmer.printStatistics();
	}

	//Create tokens after removing unwanted characters
	private static void createTokens(File inputFile, int docId) throws FileNotFoundException, UnsupportedEncodingException {//, PrintWriter writer
		String[] sentence;
		Scanner srcFile1 = new Scanner(inputFile);
		int docLen = 0;
		while (srcFile1.hasNextLine()) {
			sentence = srcFile1.nextLine().split("[\\s/(,='-]");
			docLen += sentence.length;
		}
		srcFile1.close();
		
		Scanner srcFile = new Scanner(inputFile);
		while (srcFile.hasNextLine()) {
			// splitting the tokens
			sentence = srcFile.nextLine().split("[\\s/(,='-]");
			
			for (String term : sentence) {
				// case folding
				term = term.toLowerCase().trim();

				// Remove hyphens, possessions, trim trailing and leading
				// special characters like comma, dot 
				// Eliminate one length terms and spaces
				if (!term.isEmpty() && !Pattern.matches("<*\\D+>", term) && term.length() > 1
						&& !Pattern.matches(".*[0-9].*", term)) {
					term = term.replaceAll("[^a-zA-Z0-9]*$", "").replaceAll("^[^a-zA-Z0-9]*", "").replaceAll("\'s$", "");

					// i.e is appended to some words by typo, remove that
					Pattern ptrn = Pattern.compile("^\\D+(i\\.e)$");
					Matcher matcher = ptrn.matcher(term);
					if (matcher.matches()) {
						term = term.replaceAll("(i\\.e)$", "");
					}
					// abbreviations to tokens
					term = term.replaceAll("\\.", "");
					// Storing the terms and counts
					if (term.length() > 1 && !stopWords.contains(term)) {
						tokenisedWords.add(term + " " + docId + " " + docLen);
					}
				}
			}
		}
		srcFile.close();
	}
	//sort by value for the max tf
	public static <K, V extends Comparable<? super V>> Map<K, V> 
			sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
