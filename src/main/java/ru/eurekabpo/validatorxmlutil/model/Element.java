package ru.eurekabpo.validatorxmlutil.model;

public class Element {
	private String checkSum;
	private String fileName;
	private String algorithm;
	private String base64;
	private String pdfRef;

	public Element() {
	}


	public Element(String checkSum, String fileName, String algorithm, String base64, String pdfRef) {
		this.checkSum = checkSum;
		this.fileName = fileName;
		this.algorithm = algorithm;
		this.base64 = base64;
		this.pdfRef = pdfRef;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

	public String getPdfRef() {
		return pdfRef;
	}

	public void setPdfRef(String pdfRef) {
		this.pdfRef = pdfRef;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return "Element{" +
				"checkSum='" + checkSum + '\'' +
				", algorithm='" + algorithm + '\'' +
				", base64='" + base64 + '\'' +
				", pdfRef='" + pdfRef + '\'' +
				'}';
	}
}
