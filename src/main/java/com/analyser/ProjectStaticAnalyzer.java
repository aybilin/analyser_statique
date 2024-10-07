package com.analyser;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.parse.Parser;
import java.io.File;

public class ProjectStaticAnalyzer {

    ClassStructureVisitor visitor = new ClassStructureVisitor();

    // Analyse les fichiers d'un projet donné
    public void analyze(String projectPath, int methodsThreshold) {
        File projectDir = new File(projectPath);
        if (!projectDir.isDirectory()) {
            System.err.println("Le chemin spécifié n'est pas un répertoire valide !");
            return;
        }

        // Parcours récursif des fichiers dans le répertoire du projet
        List<File> javaFiles = getJavaFiles(projectDir);
        if (javaFiles.isEmpty()) {
            System.err.println("Aucun fichier Java trouvé dans le répertoire spécifié.");
            return;
        }

        // Analyser chaque fichier Java
        for (File file : javaFiles) {
            CompilationUnit cu = parseFile(file);
            if (cu != null) {
                cu.accept(visitor);
            }
        }

        // Affichage des résultats d'analyse
        displayResults(methodsThreshold);
    }

    // Récupère la liste des fichiers Java dans le répertoire (et sous-répertoires)
    private List<File> getJavaFiles(File dir) {
        List<File> javaFiles = new ArrayList<>();
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Parcours récursif des sous-répertoires
                    javaFiles.addAll(getJavaFiles(file));
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }

    // Lit le contenu d'un fichier Java et retourne le CompilationUnit associé
    private CompilationUnit parseFile(File file) {
        try {
            // Lire le contenu du fichier
            String source = new String(Files.readAllBytes(file.toPath()));
            
            // Utiliser le parseur JDT pour obtenir le CompilationUnit
            ASTParser parser = ASTParser.newParser(AST.JLS4);  // Utilisation de Java 8
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setResolveBindings(true);
            parser.setBindingsRecovery(true);
            parser.setUnitName(file.getName());
            parser.setSource(source.toCharArray());

            return (CompilationUnit) parser.createAST(null);

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + file.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }

    // Affiche les résultats de l'analyse
    void displayResults(int methodsThreshold) {
        // 1. Nombre de classes de l’application
        System.out.println("Nombre de classes : " + visitor.getClassCount());

        // 2. Nombre de lignes de code de l’application
        System.out.println("Nombre total de lignes de code : " + visitor.getTotalLines());

        // 3. Nombre total de méthodes de l’application
        System.out.println("Nombre total de méthodes : " + visitor.getMethodCount());

        // 4. Nombre total de packages de l’application
        System.out.println("Nombre total de packages : " + visitor.getPackageNames().size());

        // 5. Nombre moyen de méthodes par classe
        System.out.println("Nombre moyen de méthodes par classe : " + (double) visitor.getMethodCount() / visitor.getClassCount());

        // 6. Nombre moyen de lignes de code par méthode
        System.out.println("Nombre moyen de lignes de code par méthode : " + (double) visitor.getTotalLines() / visitor.getMethodCount());

        // 7. Nombre moyen d’attributs par classe
        System.out.println("Nombre moyen d'attributs par classe : " + (double) visitor.getAttributeCount() / visitor.getClassCount());

        // 8 & 9. Les 10% des classes avec le plus grand nombre de méthodes et d'attributs
        displayTopClasses(20);

        // 10. Les classes qui possèdent plus de X méthodes
        displayClassesWithMoreThanXMethods(methodsThreshold);

        // 11. Le nombre maximal de paramètres par rapport à toutes les méthodes
        System.out.println("Nombre maximal de paramètres : " + visitor.getMaxParameters());
    }

    // Affiche les 10% des classes ayant le plus de méthodes et d'attributs
    private void displayTopClasses(int percent) {
        List<Map.Entry<String, Integer>> topMethodsClasses = getTopNClasses(visitor.getMethodsPerClass(), percent);
        List<Map.Entry<String, Integer>> topAttributesClasses = getTopNClasses(visitor.getAttributesPerClass(), percent);

        System.out.println("Top " + percent + "% des classes avec le plus de méthodes : " + topMethodsClasses);
        System.out.println("Top " + percent + "% des classes avec le plus d'attributs : " + topAttributesClasses);

        // Trouver les classes présentes dans les deux listes (méthodes et attributs)
        Set<String> topMethodsClassNames = topMethodsClasses.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        Set<String> topAttributesClassNames = topAttributesClasses.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        Set<String> commonClasses = new HashSet<>(topMethodsClassNames);
        commonClasses.retainAll(topAttributesClassNames);

        System.out.println("Classes présentes dans les deux catégories : " + commonClasses);
    }

    // Récupère les N% de classes avec le plus grand nombre d'attributs ou de méthodes
    private List<Map.Entry<String, Integer>> getTopNClasses(Map<String, Integer> map, int percent) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(map.size() * percent / 100)
                .collect(Collectors.toList());
    }

    // Affiche les classes avec plus de X méthodes
    private void displayClassesWithMoreThanXMethods(int x) {
        System.out.println("Classes avec plus de " + x + " méthodes :");
        visitor.getMethodsPerClass().entrySet().stream()
                .filter(entry -> entry.getValue() > x)
                .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue() + " méthodes"));
    }
    //methode graphe dappel
    public void displayCallGraph() {
        Map<String, Set<String>> callGraph = visitor.getCallGraph();
        
        System.out.println("Graphe d'appel :");
        for (Map.Entry<String, Set<String>> entry : callGraph.entrySet()) {
            String method = entry.getKey();
            Set<String> calledMethods = entry.getValue();
            
            if (!calledMethods.isEmpty()) {
                System.out.println(method + " appelle : " + calledMethods);
            } else {
                System.out.println(method + " n'appelle aucune méthode.");
            }
        }
    }
 // Méthode pour exporter le graphe d'appel dans un fichier .dot
    public void exportCallGraphToDot(String fileName) {
        Map<String, Set<String>> callGraph = visitor.getCallGraph();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".dot"))) {
            writer.write("digraph CallGraph {\n");
            
            for (Map.Entry<String, Set<String>> entry : callGraph.entrySet()) {
                String method = entry.getKey();
                Set<String> calledMethods = entry.getValue();
                
                for (String calledMethod : calledMethods) {
                    writer.write("\"" + method + "\" -> \"" + calledMethod + "\";\n");
                }
            }

            writer.write("}\n");
            System.out.println("Le graphe d'appel a été exporté dans le fichier " + fileName + ".dot");

        } catch (IOException e) {
            System.err.println("Erreur lors de l'export du graphe d'appel : " + e.getMessage());
        }
    }
   

    public void generateGraphImage(String dotFilePath, String outputImagePath) {
        try {
            // Vérification si l'extension .dot est bien présente
            if (!dotFilePath.endsWith(".dot")) {
                dotFilePath += ".dot";
            }
            
            File dotFile = new File(dotFilePath);
            
            // Vérification que le fichier existe
            if (!dotFile.exists()) {
                System.err.println("Le fichier .dot n'existe pas : " + dotFilePath);
                return;
            }
            
            Parser parser = new Parser();
            MutableGraph g = parser.read(dotFile);
            Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(outputImagePath));
            System.out.println("Image du graphe générée : " + outputImagePath);
         // Utilisation d'une commande système spécifique pour ouvrir l'image
            File imageFile = new File(outputImagePath);
            openImage(imageFile);
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la génération de l'image du graphe : " + e.getMessage());
        }
    }
    public void openImage(File imageFile) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Commande pour Windows
                new ProcessBuilder("cmd", "/c", "start", imageFile.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                // Commande pour macOS
                new ProcessBuilder("open", imageFile.getAbsolutePath()).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                // Commande pour Linux
                new ProcessBuilder("xdg-open", imageFile.getAbsolutePath()).start();
            } else {
                System.err.println("Système d'exploitation non pris en charge pour l'affichage automatique.");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de l'image : " + e.getMessage());
        }
    }



}
