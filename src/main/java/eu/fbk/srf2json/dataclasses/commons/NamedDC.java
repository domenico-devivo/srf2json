package eu.fbk.srf2json.dataclasses.commons;

public interface NamedDC {
	default String processName(String nameToProcess) {
		if (nameToProcess == null) {
			return null;
		}

		nameToProcess = removeAccents(nameToProcess);

		boolean firstPart = true;
		
		StringBuilder sb = new StringBuilder();
		for (String namePart : nameToProcess.split("\\s+")) {
			if (!namePart.isEmpty()) {
				// The entire name starts with a lowercase letter
				if (firstPart) {
					sb.append(namePart.toLowerCase());
					firstPart = false;
				} else {
					sb.append(namePart.substring(0, 1).toUpperCase());
					sb.append(namePart.substring(1).toLowerCase());
				}
			}
		}
		
		return sb.toString();
	}

	default String eliminateSpaces(String nameToProcess) {
		if (nameToProcess == null) {
			return null;
		}

		nameToProcess = removeAccents(nameToProcess);

		StringBuilder sb = new StringBuilder();
		for (String namePart : nameToProcess.split("\\s+")) {
			if (!namePart.isEmpty()) {
				sb.append(namePart);
			}
		}

		return sb.toString();
	}

	default String removeAccents(String nameToProcess) {
		if (nameToProcess == null) {
			return null;
		}

		return nameToProcess
			.replace("à", "a ")
			.replace("è", "e ")
			.replace("é", "e ")
			.replace("ò", "o ")
			.replace("ì", "i ")
			.replace("/", "")
			.replace("'", " ")
		;
	}
}
