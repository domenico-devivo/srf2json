package eu.fbk.srf2json;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

class FilePathsPair {
	private Path sheetFilePath;
	private Path definitionFilePath;
	
	private PathMatcher sheetPathMatcher;
	private PathMatcher definitionPathMatcher;
	
	public FilePathsPair() {
		super();
		
		this.sheetFilePath = null;
		this.definitionFilePath = null;
		
		this.sheetPathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{rfisrf_sheet,rfisrf_ldv_sheet}");
		this.definitionPathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{rfisrf_definition,rfisrf_ldv_definition}");
	}
	
	public void addFilePath(Path filePath) throws IllegalArgumentException {
		Path fileName = filePath.getFileName();
		if (sheetPathMatcher.matches(fileName)) {
			this.sheetFilePath = filePath;
		} else {
			if (definitionPathMatcher.matches(fileName)) {
				this.definitionFilePath = filePath;
			} else {
				//("The path " + filePath.toString() + " corresponds neither to a sheet description (rfisrf_sheet or rfisrf_ldv_sheet) nor to a definitions file (rfisrf_definition or rfisrf_ldv_definition)");
			}
		}
	}

	public boolean isValid() {
		return (this.sheetFilePath != null && this.definitionFilePath != null); 
	}
	
	public Path getSheetFilePath() {
		return sheetFilePath;
	}

	public Path getDefinitionFilePath() {
		return definitionFilePath;
	}
}
