package ru.eurekabpo.validatorxmlutil.model;

public class ReportData {
	private int id;
	private String fileName;
	private String hashSum;
	private String computedHashSum;
	private String resultValidation;

	public ReportData() {
	}

	public ReportData(int id, String fileName, String hashSum, String computedHashSum, String resultValidation) {
		this.id = id;
		this.fileName = fileName;
		this.hashSum = hashSum;
		this.computedHashSum = computedHashSum;
		this.resultValidation = resultValidation;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getHashSum() {
		return hashSum;
	}

	public void setHashSum(String hashSum) {
		this.hashSum = hashSum;
	}

	public String getComputedHashSum() {
		return computedHashSum;
	}

	public void setComputedHashSum(String computedHashSum) {
		this.computedHashSum = computedHashSum;
	}

	public String getResultValidation() {
		return resultValidation;
	}

	public void setResultValidation(String resultValidation) {
		this.resultValidation = resultValidation;
	}
}
