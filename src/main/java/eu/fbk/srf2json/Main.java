package eu.fbk.srf2json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;

import eu.fbk.srf2json.dataclasses.LogicProjectDC;
import eu.fbk.srf2json.dataclasses.LogicTypeDC;
import eu.fbk.srf2json.logic.DeclarationIdManager;
import eu.fbk.srf2json.logic.SRFDictionary;

public class Main {
	public static void main(String[] args) {
		final String ANSI_RESET = "\u001B[0m";
		final String ANSI_RED_BOLD_BRIGHT = "\033[1;91m";
		final String ANSI_GREEN_BOLD_BRIGHT = "\033[1;92m";

		if (args.length < 2) {
			System.err.println(ANSI_RED_BOLD_BRIGHT + "Missing input folder name and/or output file name!" + ANSI_RESET);
			System.exit(1);
		}

		String inputFolderName = args[0];
		String outputFileName = args[1];

		Logger logger = Logger.getLogger("eu.fbk.srf2json");
		LogManager.getLogManager().reset();

		Path logFilePath = null;
		try {
			logFilePath = Files.createTempFile("srf2json", ".log");
			logger.addHandler(new FileHandler(logFilePath.toString()));
		} catch (IOException e) {
			System.err.println(ANSI_RED_BOLD_BRIGHT +
				"An error occurred during log handler creation, impossible to write logs to a file!" +
				" The exception message is: " + e.getMessage() +
			ANSI_RESET);
		}

		Path resultPath = null;
		try (ResultWriter writer = new ResultWriter(outputFileName)) {
			try {
				LogicProjectDC logicProject = parseInputFolder(inputFolderName);
				resultPath = writer.writeResult(logicProject);
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Conversion error", e);
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error during ResultWriter initialisation", e);
		}

		if (resultPath == null) {
			String logRelatedMessage;
			if (logFilePath == null) {
				logRelatedMessage = "No log has been written due to an error.";
			} else {
				logRelatedMessage = "Please refer to the log in " + logFilePath.toAbsolutePath().normalize();
			}
			System.out.println(ANSI_RED_BOLD_BRIGHT + "Conversion failed. " + logRelatedMessage + ANSI_RESET);
			System.exit(1);
		} else {
			System.out.println(ANSI_GREEN_BOLD_BRIGHT +
				"The output has been successfully written to " + resultPath.toAbsolutePath().normalize() +
			ANSI_RESET);
		}
	}

	private static LogicProjectDC parseInputFolder(String inputFolderName) throws Exception {
		LogicProjectDC logicProject = SRFLoader.loadLogicProject(inputFolderName);

		System.out.println("Sorting project objects...");
		logicProject.getSubTreeID(); // this method sorts the underlying object tree

		System.out.println("Assigning IDs to declarations (attributes and commands)...");
		SRFDictionary dictionary = logicProject.getProject().getDictionary();
		logicProject.getProject().getFlatClassesStream().forEach(classDC -> {
			LogicTypeDC lt = classDC.getParent().getLogicType();
			DeclarationIdManager declarationIdManager = new DeclarationIdManager();
			declarationIdManager.assignIds(
					Stream.concat(
							classDC.getDeclarations().getAttributesStream(),
							classDC.getDeclarations().getCommandsStream()
					),
					dictionary.getAttrIds(
							lt.getPlantType().getName(),
							lt.isLdS() ? "LDS" : "LDV",
							classDC.getName()
					)
			);
		});
		logicProject.getProject().assignIdsToPlants();

		System.out.println("Generating views...");
		logicProject.getProject().getViewsManager().generateViews();

		logicProject.getSubTreeID(); // this method sorts the underlying object tree

		return logicProject;
	}
}
