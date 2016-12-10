//===========================================================================================================================
//	Program : Class that contains that structure for posting list
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
public class DocDetails{
	Integer docLen;
	Integer maxTf;
	Integer termFrequency;
	
	public DocDetails(Integer docLen, Integer maxTf2, Integer termFrequency) {
		this.docLen = docLen;
		this.maxTf = maxTf2;
		this.termFrequency = termFrequency;
	}

	public Integer getTermFrequency() {
		return termFrequency;
	}

	public void setTermFrequency(Integer termFrequency) {
		this.termFrequency = termFrequency;
	}

	public Integer getDocLen() {
		return docLen;
	}

	public void setDocLen(Integer docLen) {
		this.docLen = docLen;
	}

	public Integer getMaxTf() {
		return maxTf;
	}

	public void setMaxTf(Integer maxTf) {
		this.maxTf = maxTf;
	}
}
