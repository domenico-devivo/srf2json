# SRF2JSON

The second channel of the SRF to JSON conversion. Uses ANTLR 4 for lexer / parser generation.

# Uso del tool

Il tool richiede Java 11 ed è stato implementato come un progetto Maven. Per preparare il pacchetto bisogna eseguire il comando

```
mvn package
```

nella cartella che contiene il file `pom.xml` (la cartella principale del progetto). Per lanciare (usando Java) il pacchetto JAR risultato dal passo precedente bisogna fornire:

- il percorso ad una cartella con i dati (il dizionario e una cartella per ogni tipo di impianto contenente le SRF);

- il percorso al file di output dove scrivere il JSON risultante.

Ad esempio, il comando può essere così formato:

```
java -jar target/srf2json-1.0.0-SNAPSHOT-jar-with-dependencies.jar ~/SRF2JSON/test/ ~/SRF2JSON/test_result.json
```
