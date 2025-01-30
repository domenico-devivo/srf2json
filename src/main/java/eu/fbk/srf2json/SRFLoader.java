package eu.fbk.srf2json;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.fbk.srf2json.dataclasses.*;
import eu.fbk.srf2json.logic.*;
import me.tongfei.progressbar.ConsoleProgressBarConsumer;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.antlr.v4.runtime.*;

import eu.fbk.srf2json.dataclasses.commons.types.ClassReferenceTypeDefinitionDC;
import eu.fbk.srf2json.dataclasses.fsm.FsmDC;
import eu.fbk.srf2json.parsing.*;
import eu.fbk.srf2json.parsing.SRFParser.TransitionContext;

import me.tongfei.progressbar.ProgressBar;

//import java.nio.charset.StandardCharsets;
//import org.antlr.v4.runtime.Token;

public class SRFLoader {
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_RED_BOLD_BRIGHT = "\033[1;91m";

	public static <TParser extends Parser, TLexer extends TokenSource> TParser getParser(Class<TParser> parserClass, Class<TLexer> lexerClass, CharStream inputStream) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		TLexer lexer = lexerClass.getDeclaredConstructor(CharStream.class).newInstance(inputStream);
		CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
		commonTokenStream.fill();
		
// Uncomment this to dump all the tokens to file
//		Path file = Paths.get("tokens." + lexerClass.getName() + ".txt");
//		try {
//			Files.write(file, commonTokenStream.getTokens().stream().map(Token::toString).collect(Collectors.toList()), StandardCharsets.UTF_8);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		TParser parser = parserClass.getDeclaredConstructor(TokenStream.class).newInstance(commonTokenStream);
		parser.removeErrorListeners();
		parser.addErrorListener(new SRFErrorListener());
		return parser;
	}
	
	public static ClassDC visitClass(FilePathsPair filePathsPair, LiteralIdManagerDispatcher literalIdManager, Collection<TypesManager> typesManagers, LogicTypeDC logicTypeDC) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		CharStream definitionsInputStream;
        
    	definitionsInputStream = CharStreams.fromFileName(filePathsPair.getDefinitionFilePath().toString());
		
		SRF_definitionsParser definitionsParser = getParser(SRF_definitionsParser.class, SRF_definitionsLexer.class, definitionsInputStream);
		SRF_definitionsParser.RootContext definitionsCtx = definitionsParser.root();
		
		PlantTypeDC plantTypeDC = logicTypeDC.getPlantType();
		ProjectDC projectDC = plantTypeDC.getProject();
		TypesManager typesManager = new TypesManager();
		
		ClassJsonGeneratorVisitor definitionsVisitor = new ClassJsonGeneratorVisitor(
			literalIdManager,
			projectDC.getDictionary(),
			plantTypeDC.getName(),
			logicTypeDC.getName(),
			typesManager,
			projectDC.getViewsManager()
		);
		ClassDC definitionsResult = definitionsVisitor.visitRoot(definitionsCtx);
		projectDC.addClassToFlatCollection(definitionsResult);
		
		CharStream inputStream;

		inputStream = CharStreams.fromFileName(filePathsPair.getSheetFilePath().toString());
        
		SRFParser parser = getParser(SRFParser.class, SRFLexer.class, inputStream);
		PrioritiesManager<TransitionContext> prioritiesManager = new PrioritiesManager<>();
		TransitionPrioritizerListener prioritizer = new TransitionPrioritizerListener(prioritiesManager);
		parser.addParseListener(prioritizer);
		SRFParser.RootContext ctx = parser.root();
		
		FsmJsonGeneratorVisitor visitor = new FsmJsonGeneratorVisitor(
			prioritiesManager,
			definitionsResult.getStatesEnum()
		);
		FsmDC fsmResult = visitor.visitRoot(ctx);

		definitionsResult.setFsm(fsmResult);

		typesManagers.add(typesManager);
		
		return definitionsResult;
	}
	
	public static Collection<ClassDC> loadClasses(Path dir, LiteralIdManagerDispatcher literalIdManager, Collection<TypesManager> typesManagers, LogicTypeDC logicTypeDC) throws IOException {
		Map<String, FilePathsPair> filesMap = new HashMap<>();
		Files.walkFileTree(dir, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (!Files.isDirectory(file)) {
					String[] nameParts = file.getFileName().toString().split("\\.(?=[^.]+$)");
					String key = nameParts[0];

					FilePathsPair pair;
					if (filesMap.containsKey(key)) {
						pair = filesMap.get(key);
					} else {
						pair = new FilePathsPair();
						filesMap.put(key, pair);
					}
					pair.addFilePath(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		List<FilePathsPair> validPairs = filesMap.values().stream().filter(FilePathsPair::isValid).collect(Collectors.toList());
		
		Collection<ClassDC> classes = new ArrayList<>();

		try (ProgressBar pb = new ProgressBarBuilder()
			 	.setConsumer(new ConsoleProgressBarConsumer(System.out))
				.setInitialMax(validPairs.size())
				.setTaskName(dir.toString())
				.build()) {
			for(FilePathsPair filePathsPair : validPairs) {
				try {
					classes.add(visitClass(filePathsPair, literalIdManager, typesManagers, logicTypeDC));
				} catch (Exception e) {
					String msg =
							"Problem with parsing the class described by the pair " +
							filePathsPair.getSheetFilePath().toString() + " and " +
							filePathsPair.getDefinitionFilePath().toString();
					Logger.getLogger("eu.fbk.srf2json").log(Level.SEVERE, msg, e);
					throw new IncompleteOutputException(e);
				}
				pb.step();
			}
		}
		
		return classes;
	}
	
	public static void loadLogicType(Path dir, PlantTypeDC plantType, LiteralIdManagerDispatcher literalIdManager, Collection<TypesManager> typesManagers) throws IOException {
		String logicTypeName = dir.getFileName().toString();
		
		System.out.println("Parsing logic type: " + logicTypeName);
		
		LogicTypeDC res = new LogicTypeDC(plantType)
			.setName(logicTypeName);
		new LogicDC(res)
			.addClasses(SRFLoader.loadClasses(dir, literalIdManager, typesManagers, res));
	}

	public static void createEmptyLogicType(PlantTypeDC plantType, String logicTypeName) {
		System.out.println(ANSI_RED_BOLD_BRIGHT +
				"No folder found for logic type: " + logicTypeName + ". Creating an empty logic type object." +
		ANSI_RESET);

		LogicTypeDC res = new LogicTypeDC(plantType)
				.setName(logicTypeName);
		new LogicDC(res);
	}
	
	public static void loadPlantType(Path dir, ProjectDC project, LiteralIdManagerDispatcher literalIdManager, Collection<TypesManager> typesManagers) throws IOException {
		String plantTypeName = dir.getFileName().toString();
		
		System.out.println("Parsing plant type: " + plantTypeName);
		PlantTypeDC plantType = new PlantTypeDC(project).setName(plantTypeName);

		List<String> remainingLogicTypeNames = new LinkedList<>();
		remainingLogicTypeNames.add("LdS");
		remainingLogicTypeNames.add("LdV");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
	        for (Path file: stream) {
	        	if (Files.isDirectory(file)) {
	        		String fn = file.getFileName().toString();
					// If the folder name equals to one of the logic types we expect, load it and remove from the list
					if (remainingLogicTypeNames.removeIf(fn::equalsIgnoreCase)) {
	        			SRFLoader.loadLogicType(file, plantType, literalIdManager, typesManagers);
	        		}
	            }
	        }
	    }

		remainingLogicTypeNames.forEach(logicTypeName -> createEmptyLogicType(plantType, logicTypeName));
	}
	
	public static SRFDictionary parseDictionary(Path dict_file) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		CharStream inputStream;

		inputStream = CharStreams.fromFileName(dict_file.toString());
        
		SRF_dictionaryParser parser = getParser(SRF_dictionaryParser.class, SRF_dictionaryLexer.class, inputStream);
		SRFDictionary dictionary = new SRFDictionary();
		DictionaryListener listener = new DictionaryListener(dictionary);
		parser.addParseListener(listener);
		parser.root();
		
		return dictionary;
	}

	public static LogicProjectDC loadLogicProject(String dirString) throws IOException {
		System.out.println("Parsing project: " + dirString);
		
		Path dir = Paths.get(dirString);
		LiteralIdManagerDispatcher literalIdManager = new LiteralIdManagerDispatcher();
		ArrayList<TypesManager> typesManagers = new ArrayList<>();
		LogicProjectDC logicProject = new LogicProjectDC();
		ProjectDC project = logicProject.getProject().setProjectName(dir.getFileName().toString());

		// IMPORTANT: The dictionary should be parsed before all the classes in order to take effect
		List<Path> tmp = Files.find(dir, 1, (path, attributes) -> path.getFileName().toString().equals("Dizionario.rfisrf_dictionary"))
			.collect(Collectors.toList());
		
		if (tmp.size() == 0) {
			System.out.println(ANSI_RED_BOLD_BRIGHT + "No dictionary file found! Skipping" + ANSI_RESET);
		} else
		if (tmp.size() > 1) {
			System.out.println(ANSI_RED_BOLD_BRIGHT + "More than one dictionary file found! Skipping" + ANSI_RESET);
		} else {
			try {
				Path dict_file = tmp.get(0);
				project.setDictionary(parseDictionary(dict_file));
			} catch (Exception e) {
				Logger.getLogger("eu.fbk.srf2json").log(Level.SEVERE, "Problem with parsing the dictionary file", e);
				throw new IncompleteOutputException(e);
			}
		}
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
	        for (Path file: stream) {
				if (file.getFileName().toString().equalsIgnoreCase("release.yml")) {
					project.setLatestRelease(YamlToJsonConverter.convertYamlToJson(file.toFile()));
				} else {
					if (Files.isDirectory(file) && file.getFileName().toString().equals("SRF")) {
						try (DirectoryStream<Path> srfStream = Files.newDirectoryStream(file)) {
							for (Path plantDir: srfStream) {
								if (Files.isDirectory(plantDir) && !plantDir.getFileName().toString().startsWith(".")) {
									SRFLoader.loadPlantType(plantDir, project, literalIdManager, typesManagers);
								}
							}
						}
					}
				}
	        }
	    }

		project.getClassIdManager().assignIds();
		literalIdManager.assignIds(0);

		// First we register instances for all the class references
		project.getDictionary().getClassNames().forEach(className -> {
			ClassReferenceTypeDefinitionDC instance = new ClassReferenceTypeDefinitionDC().setName(className);
			typesManagers.forEach(typesManager -> typesManager.registerClassReferenceIfAny(instance));
		});
		
		// Then we instantiate all the types
		typesManagers.forEach(TypesManager::instantiateTypes);
		
		return logicProject;
	}
}
