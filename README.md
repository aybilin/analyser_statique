# Analyseur Statique de Projet Java

## Description
Cet outil est un analyseur statique pour les projets Java. Il permet d'extraire diverses métriques sur la structure du code, telles que le nombre de classes, de méthodes, d'attributs, ainsi que des informations sur la complexité du code. L'application offre à la fois une interface en ligne de commande (CLI) et une interface graphique (GUI) pour l'analyse des projets.

## Fonctionnalités
- Comptage des classes, méthodes, attributs et lignes de code
- Calcul de moyennes (méthodes par classe, lignes par méthode, attributs par classe)
- Identification des classes avec un nombre élevé de méthodes
- Analyse des packages
- Génération d'un graphe d'appel des méthodes
- Exportation du graphe d'appel au format DOT (Graphviz)
- Génération d'une image PNG du graphe d'appel

## Prérequis
- Java JDK 8 ou supérieur
- Eclipse IDE (ou autre IDE Java de votre choix)
- Eclipse JDT Core (pour l'analyse AST)
- Graphviz (pour la génération de graphes)

## Installation
1. Clonez ce dépôt ou téléchargez les fichiers source.
2. Importez le projet dans Eclipse (ou votre IDE préféré).
3. Assurez-vous que toutes les dépendances nécessaires sont dans le classpath du projet.

## Utilisation

### Exécution depuis l'IDE
1. Ouvrez le projet dans votre IDE.
2. Localisez la classe principale que vous souhaitez exécuter (`CLIApp` pour l'interface en ligne de commande ou `GUIApp` pour l'interface graphique).
3. Cliquez droit sur la classe et sélectionnez "Run As" > "Java Application" (ou l'équivalent dans votre IDE).

### Interface en Ligne de Commande (CLI)
1. Exécutez la classe `CLIApp`.
2. Suivez les instructions à l'écran pour :
   - Entrer le chemin du projet à analyser
   - Définir le seuil de méthodes
   - Choisir d'afficher le graphe d'appel
   - Exporter le graphe si désiré

### Interface Graphique (GUI)
1. Exécutez la classe `GUIApp`.
2. Utilisez l'interface graphique pour :
   - Sélectionner le projet à analyser (utilisez le bouton "Parcourir")
   - Définir le seuil de méthodes
   - Lancer l'analyse avec le bouton "Analyser"
   - Visualiser les résultats dans la zone de texte
   - Exporter le graphe d'appel avec le bouton "Exporter Graphe"

## Configuration
- Le seuil pour identifier les classes avec "trop" de méthodes est configurable dans l'interface.
- Les chemins d'exportation pour les fichiers DOT et PNG sont actuellement définis dans le code. Pour les modifier :
  - Dans `CLIApp`, cherchez la ligne contenant `String filePath = "C:\\Users\\Ayoub\\Downloads\\"` et ajustez le chemin.
  - Dans `GUIApp`, cherchez une ligne similaire et ajustez-la de la même manière.

## Dépannage
- Si l'analyse échoue, vérifiez que le chemin du projet est correct et contient des fichiers Java valides.
- Pour la génération d'images de graphe, assurez-vous que Graphviz est correctement installé et accessible.
- En cas d'erreur lors de l'accès aux fichiers, vérifiez les permissions de lecture sur le projet analysé.
- Si vous rencontrez des problèmes avec les dépendances, assurez-vous qu'elles sont correctement configurées dans le build path de votre projet Eclipse.

