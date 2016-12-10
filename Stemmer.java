//===========================================================================================================================
//	Program : Class that performs stemming from the tokens
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Stemmer, implementing the Porter Stemming Algorithm
 *
 * The Stemmer class transforms a word into its root form. The input word can be
 * provided a character at time (by calling add()), or at once by calling one of
 * the various stem(something) methods.
 */

class Stemmer {
	// Count information helpers

	private char[] b;
	private int i, /* offset into b */
			i_end, /* offset to end of stemmed word */
			j, k;
	private static final int INC = 50;

	/* unit of size whereby b is increased */
	public Stemmer() {
		b = new char[INC];
		i = 0;
		i_end = 0;
	}

	/**
	 * Add a character to the word being stemmed. When you are finished adding
	 * characters, you can call stem(void) to stem the word.
	 */

	public void add(char ch) {
		if (i == b.length) {
			char[] new_b = new char[i + INC];
			for (int c = 0; c < i; c++)
				new_b[c] = b[c];
			b = new_b;
		}
		b[i++] = ch;
	}

	/**
	 * Adds wLen characters to the word being stemmed contained in a portion of
	 * a char[] array. This is like repeated calls of add(char ch), but faster.
	 */

	public void add(char[] w, int wLen) {
		if (i + wLen >= b.length) {
			char[] new_b = new char[i + wLen + INC];
			for (int c = 0; c < i; c++)
				new_b[c] = b[c];
			b = new_b;
		}
		for (int c = 0; c < wLen; c++)
			b[i++] = w[c];
	}

	/**
	 * After a word has been stemmed, it can be retrieved by toString(), or a
	 * reference to the internal buffer can be retrieved by getResultBuffer and
	 * getResultLength (which is generally more efficient.)
	 */
	public String toString() {
		return new String(b, 0, i_end);
	}

	/**
	 * Returns the length of the word resulting from the stemming process.
	 */
	public int getResultLength() {
		return i_end;
	}

	/**
	 * Returns a reference to a character buffer containing the results of the
	 * stemming process. You also need to consult getResultLength() to determine
	 * the length of the result.
	 */
	public char[] getResultBuffer() {
		return b;
	}

	/* cons(i) is true <=> b[i] is a consonant. */

	private final boolean cons(int i) {
		switch (b[i]) {
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			return false;
		case 'y':
			return (i == 0) ? true : !cons(i - 1);
		default:
			return true;
		}
	}

	/*
	 * m() measures the number of consonant sequences between 0 and j. if c is a
	 * consonant sequence and v a vowel sequence, and <..> indicates arbitrary
	 * presence,
	 * 
	 * <c><v> gives 0 <c>vc<v> gives 1 <c>vcvc<v> gives 2 <c>vcvcvc<v> gives 3
	 * ....
	 */

	private final int m() {
		int n = 0;
		int i = 0;
		while (true) {
			if (i > j)
				return n;
			if (!cons(i))
				break;
			i++;
		}
		i++;
		while (true) {
			while (true) {
				if (i > j)
					return n;
				if (cons(i))
					break;
				i++;
			}
			i++;
			n++;
			while (true) {
				if (i > j)
					return n;
				if (!cons(i))
					break;
				i++;
			}
			i++;
		}
	}

	/* vowelinstem() is true <=> 0,...j contains a vowel */

	private final boolean vowelinstem() {
		int i;
		for (i = 0; i <= j; i++)
			if (!cons(i))
				return true;
		return false;
	}

	/* doublec(j) is true <=> j,(j-1) contain a double consonant. */

	private final boolean doublec(int j) {
		if (j < 1)
			return false;
		if (b[j] != b[j - 1])
			return false;
		return cons(j);
	}

	/*
	 * cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
	 * and also if the second c is not w,x or y. this is used when trying to
	 * restore an e at the end of a short word. e.g.
	 * 
	 * cav(e), lov(e), hop(e), crim(e), but snow, box, tray.
	 * 
	 */

	private final boolean cvc(int i) {
		if (i < 2 || !cons(i) || cons(i - 1) || !cons(i - 2))
			return false;
		{
			int ch = b[i];
			if (ch == 'w' || ch == 'x' || ch == 'y')
				return false;
		}
		return true;
	}

	private final boolean ends(String s) {
		int l = s.length();
		int o = k - l + 1;
		if (o < 0)
			return false;
		for (int i = 0; i < l; i++)
			if (b[o + i] != s.charAt(i))
				return false;
		j = k - l;
		return true;
	}

	/*
	 * setto(s) sets (j+1),...k to the characters in the string s, readjusting
	 * k.
	 */

	private final void setto(String s) {
		int l = s.length();
		int o = j + 1;
		for (int i = 0; i < l; i++)
			b[o + i] = s.charAt(i);
		k = j + l;
	}

	/* r(s) is used further down. */

	private final void r(String s) {
		if (m() > 0)
			setto(s);
	}

	/*
	 * step1() gets rid of plurals and -ed or -ing. e.g.
	 * 
	 * caresses -> caress ponies -> poni ties -> ti caress -> caress cats -> cat
	 * 
	 * feed -> feed agreed -> agree disabled -> disable
	 * 
	 * matting -> mat mating -> mate meeting -> meet milling -> mill messing ->
	 * mess
	 * 
	 * meetings -> meet
	 * 
	 */

	private final void step1() {
		if (b[k] == 's') {
			if (ends("sses"))
				k -= 2;
			else if (ends("ies"))
				setto("i");
			else if (b[k - 1] != 's')
				k--;
		}
		if (ends("eed")) {
			if (m() > 0)
				k--;
		} else if ((ends("ed") || ends("ing")) && vowelinstem()) {
			k = j;
			if (ends("at"))
				setto("ate");
			else if (ends("bl"))
				setto("ble");
			else if (ends("iz"))
				setto("ize");
			else if (doublec(k)) {
				k--;
				{
					int ch = b[k];
					if (ch == 'l' || ch == 's' || ch == 'z')
						k++;
				}
			} else if (m() == 1 && cvc(k))
				setto("e");
		}
	}

	/* step2() turns terminal y to i when there is another vowel in the stem. */

	private final void step2() {
		if (ends("y") && vowelinstem())
			b[k] = 'i';
	}

	/*
	 * step3() maps double suffices to single ones. so -ization ( = -ize plus
	 * -ation) maps to -ize etc. note that the string before the suffix must
	 * give m() > 0.
	 */

	private final void step3() {
		if (k == 0)
			return;
		/* For Bug 1 */ switch (b[k - 1]) {
		case 'a':
			if (ends("ational")) {
				r("ate");
				break;
			}
			if (ends("tional")) {
				r("tion");
				break;
			}
			break;
		case 'c':
			if (ends("enci")) {
				r("ence");
				break;
			}
			if (ends("anci")) {
				r("ance");
				break;
			}
			break;
		case 'e':
			if (ends("izer")) {
				r("ize");
				break;
			}
			break;
		case 'l':
			if (ends("bli")) {
				r("ble");
				break;
			}
			if (ends("alli")) {
				r("al");
				break;
			}
			if (ends("entli")) {
				r("ent");
				break;
			}
			if (ends("eli")) {
				r("e");
				break;
			}
			if (ends("ousli")) {
				r("ous");
				break;
			}
			break;
		case 'o':
			if (ends("ization")) {
				r("ize");
				break;
			}
			if (ends("ation")) {
				r("ate");
				break;
			}
			if (ends("ator")) {
				r("ate");
				break;
			}
			break;
		case 's':
			if (ends("alism")) {
				r("al");
				break;
			}
			if (ends("iveness")) {
				r("ive");
				break;
			}
			if (ends("fulness")) {
				r("ful");
				break;
			}
			if (ends("ousness")) {
				r("ous");
				break;
			}
			break;
		case 't':
			if (ends("aliti")) {
				r("al");
				break;
			}
			if (ends("iviti")) {
				r("ive");
				break;
			}
			if (ends("biliti")) {
				r("ble");
				break;
			}
			break;
		case 'g':
			if (ends("logi")) {
				r("log");
				break;
			}
		}
	}

	/* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

	private final void step4() {
		switch (b[k]) {
		case 'e':
			if (ends("icate")) {
				r("ic");
				break;
			}
			if (ends("ative")) {
				r("");
				break;
			}
			if (ends("alize")) {
				r("al");
				break;
			}
			break;
		case 'i':
			if (ends("iciti")) {
				r("ic");
				break;
			}
			break;
		case 'l':
			if (ends("ical")) {
				r("ic");
				break;
			}
			if (ends("ful")) {
				r("");
				break;
			}
			break;
		case 's':
			if (ends("ness")) {
				r("");
				break;
			}
			break;
		}
	}

	/* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

	private final void step5() {
		if (k == 0)
			return;
		/* for Bug 1 */ switch (b[k - 1]) {
		case 'a':
			if (ends("al"))
				break;
			return;
		case 'c':
			if (ends("ance"))
				break;
			if (ends("ence"))
				break;
			return;
		case 'e':
			if (ends("er"))
				break;
			return;
		case 'i':
			if (ends("ic"))
				break;
			return;
		case 'l':
			if (ends("able"))
				break;
			if (ends("ible"))
				break;
			return;
		case 'n':
			if (ends("ant"))
				break;
			if (ends("ement"))
				break;
			if (ends("ment"))
				break;
			/* element etc. not stripped before the m */
			if (ends("ent"))
				break;
			return;
		case 'o':
			if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't'))
				break;
			/* j >= 0 fixes Bug 2 */
			if (ends("ou"))
				break;
			return;
		/* takes care of -ous */
		case 's':
			if (ends("ism"))
				break;
			return;
		case 't':
			if (ends("ate"))
				break;
			if (ends("iti"))
				break;
			return;
		case 'u':
			if (ends("ous"))
				break;
			return;
		case 'v':
			if (ends("ive"))
				break;
			return;
		case 'z':
			if (ends("ize"))
				break;
			return;
		default:
			return;
		}
		if (m() > 1)
			k = j;
	}

	/* step6() removes a final -e if m() > 1. */

	private final void step6() {
		j = k;
		if (b[k] == 'e') {
			int a = m();
			if (a > 1 || a == 1 && !cvc(k - 1))
				k--;
		}
		if (b[k] == 'l' && doublec(k) && m() > 1)
			k--;
	}

	/**
	 * Stem the word placed into the Stemmer buffer through calls to add().
	 * Returns true if the stemming process resulted in a word different from
	 * the input. You can retrieve the result with
	 * getResultLength()/getResultBuffer() or toString().
	 */
	public void stem() {
		k = i - 1;
		if (k > 1) {
			step1();
			step2();
			step3();
			step4();
			step5();
			step6();
		}
		i_end = k + 1;
		i = 0;
	}

	/**
	 * Test program for demonstrating the Stemmer. It reads text from a a list
	 * of files, stems each word, and writes the result to standard output. Note
	 * that the word stemmed is expected to be in lower case: forcing lower case
	 * must be done outside the Stemmer class. Usage: Stemmer file-name
	 * file-name ...
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public static TreeMap<String, DictionaryClass> info = new TreeMap<>();
	private static int max_docFreq = Integer.MIN_VALUE;
	private static int min_docFreq = Integer.MAX_VALUE;
	private static int docId_maxTf = 0;
	private static int docId_maxDocLen = 0;
	//Stemminf the collection and frame dictionary and posting list
	public static void stemmerCollection(ArrayList<String> tokenisedWords) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<Integer, HashMap<String, Integer>> maxFqMap = new HashMap<>();
		Stemmer s = new Stemmer();
		String[] term;
		int docId = 0, docLen = 0, index = 0;
		Integer maxTf = 0;
		int termFrequency = 1, docFrequency = 1;
		DictionaryClass dcNew;
		String stemToken = "";
		ArrayList<String> arrayList = new ArrayList<>();
		HashMap<String, Integer> h = null ;
		//Timer for the calculating the time
		Timer timeV2UnCompress = new Timer();
		Timer timeV2Compress = new Timer();
		
		while (index < tokenisedWords.size()) { 
			termFrequency = 1;
			docFrequency = 1;
			term = tokenisedWords.get(index++).split(" ");
			docId = Integer.parseInt(term[1]);
			docLen = Integer.parseInt(term[2]);
			for (char ch : term[0].toCharArray()) {
				s.add(ch);
			}
			s.stem();
			{
				stemToken = s.toString();
				arrayList.add(stemToken+" "+docId+" "+docLen);
				//storing in hashmap for further logic and finding max tf
				if (!maxFqMap.containsKey(docId)){
					h = new HashMap<>();
					h.put(stemToken, 1);
					maxFqMap.put(docId, h);
				} else {
					h = maxFqMap.get(docId);
					if(!h.containsKey(stemToken)) {
						h.put(stemToken, 1);
					} else {
						h.put(stemToken,h.get(stemToken) + 1);
					}
				}
			}
		}
		
		DocDetails docDet;
		int i = 0;
		while (arrayList.size() > i) {
			term = arrayList.get(i++).split(" ");
			stemToken = term[0]; 
			docId = Integer.parseInt(term[1]); 
			docLen = Integer.parseInt(term[2]);
			//finding max tf
			for(Entry<String, Integer> item : Tokenize.sortByValue(maxFqMap.get(docId)).entrySet()) {
				maxTf = item.getValue();
				break;
			}
			//storing them into dictioanry and inverted list
			if (!info.containsKey(stemToken))
				info.put(stemToken, new DictionaryClass(stemToken, docId, docLen, maxTf, termFrequency, docFrequency));
			else {
				dcNew = info.get(stemToken);
				TreeMap<Integer, DocDetails> postingListObj = dcNew.getPostingList(); 
				if (postingListObj.containsKey(docId)) {
					docDet = postingListObj.get(docId);
					docDet.setTermFrequency(docDet.getTermFrequency() + 1);
					postingListObj.put(docId, docDet);
					dcNew.setPostingList(postingListObj);
					info.put(stemToken, dcNew);
				} else {
					postingListObj.put(docId, new DocDetails(docLen, maxTf, termFrequency));
					dcNew.setPostingList(postingListObj);
					dcNew.setDocFrequency(dcNew.getDocFrequency() + 1);
					info.put(stemToken, dcNew);
				}
			}
		}
		//timer for indexing ends here
		System.out.println("\nTime taken to buid Index v2 uncompressed :: " + timeV2UnCompress.end());
		//calling blocking compression followed by front coding, so sent TRUE 
		Compression.blockingCompression(info, true);
		//To write in random access file after byte conversion
		compressionOfStem(info);
		System.out.println("\nTime taken to buid Index v2 compressed :: " + timeV2Compress.end());
		//writing uncompressed information as bytes in binary file
		stemCountInformation(info);
	}
	
	//To write uncompressed information as bytes in binary file
	private static void stemCountInformation(TreeMap<String, DictionaryClass> info) throws IOException {
		//PrintWriter writer = new PrintWriter("output");
		File file = new File("Index_Version2.uncompressed");
		RandomAccessFile newTextFile = new RandomAccessFile("Index_Version2.uncompressed", "rw");
		int maxDocLen = Integer.MIN_VALUE, maxDocTf = Integer.MIN_VALUE;
		for (String stemToken : info.keySet()) {
			DictionaryClass dictionary = info.get(stemToken);
			max_docFreq = Math.max(dictionary.getDocFrequency(), max_docFreq);
			min_docFreq = Math.min(dictionary.getDocFrequency(), min_docFreq);
			//writer.print(dictionary.getTerm() + "," + dictionary.getDocFrequency() +"|");
			newTextFile.writeBytes(dictionary.getTerm() + "," + dictionary.getDocFrequency() +"|");
			Set<Integer> post = dictionary.getPostingList().keySet();
			int count = 0;
			for (Integer docId : post) {
				count++;
				DocDetails d  = dictionary.getPostingList().get(docId);
				//writer.print(docId +","+d.getMaxTf() +","+d.getTermFrequency() +","+ d.getDocLen());
				newTextFile.writeBytes(docId +","+d.getMaxTf() +","+d.getTermFrequency() +","+ d.getDocLen());
				if (count < post.size()) {
					//writer.print("->");
					newTextFile.writeBytes("->");
				}
				//For finding doc with max length
				if (d.getDocLen() > maxDocLen) {
					maxDocLen = d.getDocLen();
					docId_maxDocLen = docId;
				}
				//For finding doc with max tf
				if (d.getMaxTf() >  maxDocTf) {
					maxDocTf = d.getMaxTf();
					docId_maxTf = docId; 
				}
			}
			//writer.println();
			newTextFile.write(System.getProperty("line.separator").getBytes());
		}
		newTextFile.close();
		//writer.close();
		double bytes = file.length();
		double kilobytes = (bytes / 1024);
		System.out.println("Size of V2 uncompressed :: " + kilobytes + " KB");
	}
	
	//To write in random access file after byte conversion
	private static void compressionOfStem(TreeMap<String, DictionaryClass> info) throws IOException {
		File file = new File("Index_Version2.compressed");
		RandomAccessFile newFile = new RandomAccessFile("Index_Version2.compressed", "rw");
		int i = 0, j = 0;
		byte[] frontCodeTermPtr = null;
		byte[] dcFreq, dcId,  max_tf, termFreq, docLen;
		int docFreq = 0;
		//the long string after blocking compression
		for (String longString : Compression.storeResult)
			newFile.writeBytes(longString);
		newFile.write(System.getProperty("line.separator").getBytes());
		//converting to bytes after gamma compression of all numbers and writing in the file
		for (String stemToken : info.keySet()) {
			DictionaryClass dictionary = info.get(stemToken);
			docFreq = dictionary.getDocFrequency();
			dcFreq = Compression.convertToByteArray(Compression.deltaCompression(docFreq));
			newFile.write(dcFreq);
			newFile.writeBytes("|");
			Set<Integer> post = dictionary.getPostingList().keySet();
			for (Integer docId : post) {
				DocDetails d  = dictionary.getPostingList().get(docId);
				dcId = Compression.convertToByteArray(Compression.deltaCompression(docId));
				max_tf = Compression.convertToByteArray(Compression.deltaCompression(d.getMaxTf()));
				termFreq = Compression.convertToByteArray(Compression.deltaCompression(d.getTermFrequency()));
				docLen = Compression.convertToByteArray(Compression.deltaCompression(d.getDocLen()));
				newFile.write(dcId); newFile.writeBytes(",");
				newFile.write(docLen);newFile.writeBytes(",");
				newFile.write(max_tf);newFile.writeBytes(",");
				newFile.write(termFreq);
			}
			//term pointers
			if (i % 8 == 0)
				frontCodeTermPtr = Compression.convertToByteArray(String.valueOf(Compression.frontCodeTermPtr.get(j++)));
			else
				frontCodeTermPtr = null;
			if (frontCodeTermPtr != null) {
				newFile.writeBytes("|");
				newFile.write(frontCodeTermPtr);
			}
			newFile.write(System.getProperty("line.separator").getBytes());
			i++;
		}
		newFile.close();
		double bytes = file.length();
		double kilobytes = (bytes / 1024);
		System.out.println("Size of V2 compressed :: " + kilobytes + " KB");
	}
	
	//To print the questions asked in the requirement
	public static void printStatistics() throws IOException {
		String[] termsTofind = {"Reynolds", "NASA", "Prandtl", "flow", "pressure", "boundary", "shock"};
		byte[] dcId,  max_tf, termFreq, docLen;
		int docFreq = 0, totalTermFreq = 0, invertedListLen = 0;
		int nasa_dcFreq = 0, nasa_count = 1;
		StringBuilder nasa_details = new StringBuilder();
		System.out.println("\nTerm\t\tdf\ttf\tInvertedListLength");
		System.out.println("******************************************************");
		String[] stemToken = {"reynold", "nasa", "prandtl", "flow", "pressur", "boundari", "shock"};
		for (int i = 0; i < termsTofind.length; i++) {
			DictionaryClass dictionary = info.get(stemToken[i]);
			docFreq = dictionary.getDocFrequency();
			if (stemToken[i].equals("nasa"))
				nasa_dcFreq = docFreq;
			Set<Integer> post = dictionary.getPostingList().keySet();
			for (Integer docId : post) {
				DocDetails d  = dictionary.getPostingList().get(docId);
				totalTermFreq += d.getTermFrequency();
				
				dcId = Compression.convertToByteArray(String.valueOf(docId));
				max_tf = Compression.convertToByteArray(String.valueOf(d.getMaxTf()));
				termFreq = Compression.convertToByteArray(String.valueOf(d.getTermFrequency()));
				docLen = Compression.convertToByteArray(String.valueOf(d.getDocLen()));
				
				invertedListLen += dcId.length +  max_tf.length + termFreq.length + docLen.length;
				if (nasa_count <= 3 && stemToken[i].equals("nasa")) {
					nasa_details.append(docId+"\t"+d.getDocLen()+"\t\t"+d.getTermFrequency()+"\t\t"+d.getMaxTf()+"\n");
					nasa_count++;
				}
			}
			System.out.println(String.format("%-10s", termsTofind[i]) + "\t" + docFreq + "\t" + totalTermFreq + "\t" + invertedListLen);
		}
		System.out.println("\nNASA details :: ");
		System.out.println("Doc Frequency :: " + nasa_dcFreq);
		System.out.println("Doc_Id\tDoc_Length\tTerm_Frequency\tMax_tf");
		System.out.println("******************************************************");
		System.out.println(nasa_details);

		StringBuilder maxFreqWords = new StringBuilder();
		StringBuilder minFreqWords = new StringBuilder();

		for (String token : info.keySet()) {
			DictionaryClass dictionary = info.get(token);
			docFreq = dictionary.getDocFrequency();
			if (docFreq == max_docFreq) {
				maxFreqWords.append(token + " ");
			} else if (docFreq == min_docFreq) {
				minFreqWords.append(token + " ");
			}
		}
		System.out.println("\nStem from index 2 with the largest df :: " + maxFreqWords);
		System.out.println("Stem from index 2 with the smallest df :: " + minFreqWords);
		System.out.println("The document with the largest max_tf in collection :: " + docId_maxTf);
		System.out.println("The document with the largest doclen in collection :: " + docId_maxDocLen);
	}
	
}
