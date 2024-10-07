package com.analyser;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GUIApp {

    private JFrame frame;
    private JTextField projectPathField;
    private JTextField methodsThresholdField;
    private JTextArea resultsArea;
    private ProjectStaticAnalyzer analyzer; // Ajout d'une instance de ProjectStaticAnalyzer

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIApp().createAndShowGUI());
    }

    public void createAndShowGUI() {
        frame = new JFrame("Analyseur Statique de Projet Java");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Haut de l'interface : sélection du projet et seuil de méthodes
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel projectPathLabel = new JLabel("Chemin du projet : ");
        projectPathField = new JTextField();

        JButton browseButton = new JButton("Parcourir...");
        browseButton.addActionListener(e -> browseForProject());

        JLabel methodsThresholdLabel = new JLabel("Seuil de méthodes (X) : ");
        methodsThresholdField = new JTextField();

        inputPanel.add(projectPathLabel);
        inputPanel.add(projectPathField);
        inputPanel.add(new JLabel());  // Espace vide
        inputPanel.add(browseButton);
        inputPanel.add(methodsThresholdLabel);
        inputPanel.add(methodsThresholdField);

        panel.add(inputPanel, BorderLayout.NORTH);

        // Centre de l'interface : affichage des résultats
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bas de l'interface : boutons d'action
        JPanel buttonPanel = new JPanel();
        JButton analyzeButton = new JButton("Analyser");
        analyzeButton.addActionListener(e -> analyzeProject());

        JButton exportButton = new JButton("Exporter Graphe");
        exportButton.addActionListener(e -> exportGraph());

        buttonPanel.add(analyzeButton);
        buttonPanel.add(exportButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void browseForProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            projectPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void analyzeProject() {
        String projectPath = projectPathField.getText();
        String methodsThresholdText = methodsThresholdField.getText();

        if (projectPath.isEmpty() || methodsThresholdText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int methodsThreshold;
        try {
            methodsThreshold = Integer.parseInt(methodsThresholdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Veuillez entrer un nombre valide pour le seuil de méthodes.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Initialisation et analyse
        analyzer = new ProjectStaticAnalyzer();
        analyzer.analyze(projectPath, methodsThreshold);

        // Affiche les résultats dans l'interface graphique
        resultsArea.setText("");
        resultsArea.append("--- Résultats de l'analyse ---\n");
        resultsArea.append("Nombre de classes : " + analyzer.visitor.getClassCount() + "\n");
        resultsArea.append("Nombre total de lignes de code : " + analyzer.visitor.getTotalLines() + "\n");
        resultsArea.append("Nombre total de méthodes : " + analyzer.visitor.getMethodCount() + "\n");
        resultsArea.append("Nombre total de packages : " + analyzer.visitor.getPackageNames().size() + "\n");
        resultsArea.append("Nombre moyen de méthodes par classe : " + 
            (double) analyzer.visitor.getMethodCount() / analyzer.visitor.getClassCount() + "\n");
        resultsArea.append("Nombre moyen de lignes de code par méthode : " + 
            (double) analyzer.visitor.getTotalLines() / analyzer.visitor.getMethodCount() + "\n");
        resultsArea.append("Nombre moyen d'attributs par classe : " + 
            (double) analyzer.visitor.getAttributeCount() / analyzer.visitor.getClassCount() + "\n");
        resultsArea.append("\n--- Classes avec plus de " + methodsThreshold + " méthodes ---\n");
        
        analyzer.visitor.getMethodsPerClass().entrySet().stream()
            .filter(entry -> entry.getValue() > methodsThreshold)
            .forEach(entry -> resultsArea.append(entry.getKey() + " : " + entry.getValue() + " méthodes\n"));
    }

    private void exportGraph() {
        if (analyzer == null) {
            JOptionPane.showMessageDialog(frame, "Veuillez d'abord analyser un projet.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Demander le nom du fichier de sortie pour le graphe
        String fileName = JOptionPane.showInputDialog(frame, "Entrez le nom du fichier de sortie (sans extension) : ", "Nom de fichier", JOptionPane.PLAIN_MESSAGE);

        if (fileName == null || fileName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Nom de fichier invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Construction du chemin complet
        String filePath = "C:\\Users\\Ayoub\\Downloads\\" + fileName;

        // Exporter le graphe d'appel
        analyzer.exportCallGraphToDot(filePath);
        JOptionPane.showMessageDialog(frame, "Le graphe d'appel a été exporté vers : " + filePath + ".dot", "Export Réussi", JOptionPane.INFORMATION_MESSAGE);

        // Demander si l'utilisateur veut générer l'image
        int choice = JOptionPane.showConfirmDialog(frame, "Voulez-vous générer l'image du graphe maintenant ?", "Générer image", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            String imagePath = filePath + ".png";
            analyzer.generateGraphImage(filePath, imagePath);
            JOptionPane.showMessageDialog(frame, "L'image du graphe a été générée : " + imagePath, "Image Générée", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
