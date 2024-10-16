package com.analyser;

import java.util.Scanner;

public class CLIApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Demande du chemin du projet
        System.out.print("Entrez le chemin du projet Java à analyser : ");
        String projectPath = scanner.nextLine();

        // Demande du seuil de méthodes pour une classe (paramètre X)
        System.out.print("Entrez la valeur du seuil pour les classes avec plus de X méthodes : ");
        int methodsThreshold = scanner.nextInt();
        scanner.nextLine();  // Consommer le retour à la ligne

        // Initialisation et analyse
        ProjectStaticAnalyzer analyzer = new ProjectStaticAnalyzer();
        analyzer.analyze(projectPath, methodsThreshold);

        // Affichage des résultats d'analyse statique
        System.out.println("\n--- Résultats de l'analyse ---");
        analyzer.displayResults(methodsThreshold);

        // Option pour afficher le graphe d'appel
        System.out.print("\nVoulez-vous afficher le graphe d'appel ? (oui/non) : ");
        String showGraphResponse = scanner.nextLine().trim().toLowerCase();

        if (showGraphResponse.equals("oui") || showGraphResponse.equals("o")) {
            System.out.println("\n--- Graphe d'appel ---");
            analyzer.displayCallGraph();
        }

        // Option pour exporter le graphe d'appel avec Graphviz
        System.out.print("\nVoulez-vous exporter le graphe d'appel dans un fichier Graphviz (.dot) ? (oui/non) : ");
        String exportGraphResponse = scanner.nextLine().trim().toLowerCase();

        if (exportGraphResponse.equals("oui") || exportGraphResponse.equals("o")) {
        	 if (exportGraphResponse.equals("oui") || exportGraphResponse.equals("o")) {
                 // Demande du nom du fichier
                 System.out.print("Entrez le nom du fichier de sortie (sans extension) : ");
                 String fileName = scanner.nextLine();

                 // Construction du chemin du fichier
                 String filePath = "Results/" + fileName ;
                 analyzer.exportCallGraphToDot(filePath);
                 System.out.println("Le graphe d'appel a été exporté vers : " + filePath);
              // Après l'export du fichier .dot
                 System.out.print("Voulez-vous générer l'image du graphe maintenant ? (oui/non) : ");
                 String generateImageResponse = scanner.nextLine().trim().toLowerCase();

                 if (generateImageResponse.equals("oui") || generateImageResponse.equals("o")) {
                     String imagePath = "Results/" + fileName + ".png";
                     analyzer.generateGraphImage(filePath, imagePath);
                     System.out.println("L'image du graphe a été générée : " + imagePath);
                 }

        scanner.close();
        	 }
        }
    }
}
