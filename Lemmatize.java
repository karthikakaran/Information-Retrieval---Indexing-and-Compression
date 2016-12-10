//===========================================================================================================================
//	Program : Class that performs lemmatisation from the tokens
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.Map.Entry;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Lemmatize {
	public static StanfordCoreNLP pipeline;
	public static TreeMap<String, DictionaryClass> info = new TreeMap<>();
	private static int max_docFreq = Integer.MIN_VALUE;
	private static int min_docFreq = Integer.MAX_VALUE;
	private static int docId_maxTf = 0;
	private static int docId_maxDocLen = 0;
	
	//To create lemmas from the tokens 
	public static void lemmatizer(ArrayList<String> tokenisedWords) throws IOException {
		StanfordLemmatizer();
		HashMap<Integer, HashMap<String, Integer>> maxFqMap = new HashMap<>();
		String[] term;
		int docId = 0, docLen = 0, index = 0;
		Integer maxTf = 0;
		int termFrequency = 1, docFrequency = 1;
		DictionaryClass dcNew;
		String stemToken = "";
		ArrayList<String> arrayList = new ArrayList<>();
		HashMap<String, Integer> h = null ;
		Timer timeV1UnCompress = new Timer();
		Timer timeV1Compress = new Timer();
		while (index < tokenisedWords.size()) { 
			termFrequency = 1;
			docFrequency = 1;
			term = tokenisedWords.get(index++).split(" ");
			docId = Integer.parseInt(term[1]);
			docLen = Integer.parseInt(term[2]);
			stemToken = lemmatize(term[0]);
			arrayList.add(stemToken + " " + docId + " " + docLen);
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
		
		DocDetails docDet;
		int i = 0;
		while (arrayList.size() > i) {
			term = arrayList.get(i++).split(" ");
			stemToken = term[0]; docId = Integer.parseInt(term[1]); docLen = Integer.parseInt(term[2]);
			
			for(Entry<String, Integer> item : Tokenize.sortByValue(maxFqMap.get(docId)).entrySet()) {
				maxTf = item.getValue();
				break;
			}
			
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
		System.out.println("\nTime taken to buid Index v1 uncompressed :: " +timeV1UnCompress.end());
		//only blocking compression is done, so FALSE is sent
		Compression.blockingCompression(info, false);
		//To write the byte values after compression into binary file
		compressionOfLemma(info);
		System.out.println("\nTime taken to buid Index v1 compressed :: " + timeV1Compress.end());
		//To write the uncompressed dictionary and inverted list as bytes into a file
		lemmaCountInformation(info);
	}
	
	//nlp lemmatizer from the jar, set up for API
	public static String lemmatize(String documentText)
    {
        List<String> lemmas = new LinkedList<String>();
        // Create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);
        // run all Annotators on this text
        pipeline.annotate(document);
        // Iterate over all of the sentences found
        String temp="";
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
            	temp=token.get(LemmaAnnotation.class);
            	lemmas.add(temp);
            }
        }
        return lemmas.get(0).toString();
    }
	
	public static void StanfordLemmatizer() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }
	
	//To write uncompressed information as bytes in binary file
	private static void lemmaCountInformation(TreeMap<String, DictionaryClass> info) throws IOException {
		File file = new File("Index_Version1.uncompressed");
		RandomAccessFile newTextFile = new RandomAccessFile("Index_Version1.uncompressed", "rw");
		int maxDocLen = Integer.MIN_VALUE, maxDocTf = Integer.MIN_VALUE;
		for (String termStoken : info.keySet()) {
			DictionaryClass dictionary = info.get(termStoken);
			max_docFreq = Math.max(dictionary.getDocFrequency(), max_docFreq);
			min_docFreq = Math.min(dictionary.getDocFrequency(), min_docFreq);
			newTextFile.writeBytes(dictionary.getTerm() + "," + dictionary.getDocFrequency() +"->");
			Set<Integer> post = dictionary.getPostingList().keySet();
			int count = 0;
			for (Integer docId : post) {
				count++;
				DocDetails d  = dictionary.getPostingList().get(docId);
				newTextFile.writeBytes(docId +","+d.getMaxTf() +","+d.getTermFrequency() +","+ d.getDocLen() + "->");
				if (count < post.size()) {
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
			newTextFile.write(System.getProperty("line.separator").getBytes());
		}
		newTextFile.close();
		double bytes = file.length();
		double kilobytes = (bytes / 1024);
		System.out.println("Size of V1 uncompressed :: " + kilobytes + " KB");
	}
	
	//To write in random access file after byte conversion
	private static void compressionOfLemma(TreeMap<String, DictionaryClass> info) throws IOException {
		File file = new File("Index_Version1.compressed");
		RandomAccessFile newFile = new RandomAccessFile("Index_Version1.compressed", "rw");
		int i = 0, j = 0;
		byte[] termPtr = null;
		byte[] dcFreq, dcId,  max_tf, termFreq, docLen;
		int docFreq = 0;
		//the long string after blocking compression
		for (String longString : Compression.longDictionaryTerms)
			newFile.writeBytes(longString);
		newFile.write(System.getProperty("line.separator").getBytes());
		for (String termStoken : info.keySet()) {
			DictionaryClass dictionary = info.get(termStoken);
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
			newFile.writeBytes("|");
			if (i % 8 == 0)
				termPtr = Compression.convertToByteArray(String.valueOf(Compression.termPtr.get(j++)));
			else
				termPtr = null;
			if (termPtr != null) {
				newFile.writeBytes("|");
				newFile.write(termPtr);
			}
			newFile.write(System.getProperty("line.separator").getBytes());
			i++;
		}
		newFile.close();
		double bytes = file.length();
		double kilobytes = (bytes / 1024);
		System.out.println("Size of V1 compressed :: " + kilobytes + " KB");
	}
	
	//To print the questions asked in the requirement
	public static void printStatistics() throws IOException {
		StringBuilder maxFreqWords = new StringBuilder();
		StringBuilder minFreqWords = new StringBuilder();
		int docFreq = 0;
		for (String token : info.keySet()) {
			DictionaryClass dictionary = info.get(token);
			docFreq = dictionary.getDocFrequency();
			if (docFreq == max_docFreq) {
				maxFreqWords.append(token + " ");
			} else if (docFreq == min_docFreq) {
				minFreqWords.append(token + " ");
			}
		}
		System.out.println("\nTerm from index 1 with the largest df :: " + maxFreqWords);
		System.out.println("Term from index 1 with the smallest df :: " + minFreqWords);
		System.out.println("The document with the largest max_tf in collection :: " + docId_maxTf);
		System.out.println("The document with the largest doclen in collection :: " + docId_maxDocLen);
	}
}
