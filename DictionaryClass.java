//===========================================================================================================================
//	Program : Class that contains dictionary data structure
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
import java.util.*;

public class DictionaryClass{
	private String term;
    private TreeMap<Integer, DocDetails> postingList;
	private Integer docFrequency = new Integer(0);
	
	public DictionaryClass(String term, Integer docId, Integer docLen, Integer maxTf, Integer termFrequency, Integer docFrequency) {
		this.term = term;
		this.postingList = new TreeMap<Integer, DocDetails>();
		this.postingList.put(docId, new DocDetails(docLen, maxTf, termFrequency));
		this.docFrequency = docFrequency;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public TreeMap<Integer, DocDetails> getPostingList() {
		return postingList;
	}

	public void setPostingList(TreeMap<Integer, DocDetails> postingList) {
		this.postingList = postingList;
	}

	public Integer getDocFrequency() {
		return docFrequency;
	}

	public void setDocFrequency(Integer docFrequency) {
		this.docFrequency = docFrequency;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((docFrequency == null) ? 0 : docFrequency.hashCode());
		result = prime * result + ((postingList == null) ? 0 : postingList.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DictionaryClass other = (DictionaryClass) obj;
		if (docFrequency == null) {
			if (other.docFrequency != null)
				return false;
		} else if (!docFrequency.equals(other.docFrequency))
			return false;
		if (postingList == null) {
			if (other.postingList != null)
				return false;
		} else if (!postingList.equals(other.postingList))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}
	
}
