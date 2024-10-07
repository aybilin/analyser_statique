package com.analyser;

import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassStructureVisitor extends ASTVisitor {

    private int classCount = 0;  
    private int methodCount = 0; 
    private int attributeCount = 0; 
    private int totalLines = 0; 
    private int maxParameters = 0; 

    private Set<String> packageNames = new HashSet<>(); 
    private Map<String, Integer> methodsPerClass = new HashMap<>(); 
    private Map<String, Integer> attributesPerClass = new HashMap<>(); 
    private Map<String, Set<String>> callGraph = new HashMap<>();  // Graphe d'appel (méthode -> méthodes appelées)

    private String currentClassName = null;  
    private String currentMethodName = null; 

    @Override
    public boolean visit(PackageDeclaration node) {
        packageNames.add(node.getName().getFullyQualifiedName());
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        currentClassName = node.getName().getIdentifier(); 
        classCount++; 
        methodsPerClass.put(currentClassName, 0);
        attributesPerClass.put(currentClassName, 0);
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (currentClassName != null) {
            methodCount++; 
            currentMethodName = node.getName().getIdentifier();
            methodsPerClass.put(currentClassName, methodsPerClass.get(currentClassName) + 1);

            int parametersCount = node.parameters().size();
            if (parametersCount > maxParameters) {
                maxParameters = parametersCount;
            }

            if (node.getBody() != null) {
                int methodLines = node.getBody().toString().split("\n").length;
                totalLines += methodLines;
            }

            // Initialiser l'ensemble des méthodes appelées par cette méthode
            callGraph.putIfAbsent(getFullMethodName(currentClassName, currentMethodName), new HashSet<>());
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (currentMethodName != null) {
            // Récupérer le nom de la méthode appelée
            String calledMethodName = node.getName().getIdentifier();
            String calledMethodFullName = getFullMethodName(currentClassName, calledMethodName);

            // Ajouter cette relation dans le graphe d'appel
            callGraph.get(getFullMethodName(currentClassName, currentMethodName)).add(calledMethodFullName);
        }

        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if (currentClassName != null) {
            attributeCount++; 
            attributesPerClass.put(currentClassName, attributesPerClass.get(currentClassName) + 1);
        }

        return super.visit(node);
    }

    // Méthode utilitaire pour obtenir le nom complet de la méthode (classe.méthode)
    private String getFullMethodName(String className, String methodName) {
        return className + "." + methodName;
    }

    // Récupère le nombre total de classes
    public int getClassCount() {
        return classCount;
    }

    // Récupère le nombre total de méthodes
    public int getMethodCount() {
        return methodCount;
    }

    // Récupère le nombre total d'attributs
    public int getAttributeCount() {
        return attributeCount;
    }

    // Récupère le nombre total de lignes de code
    public int getTotalLines() {
        return totalLines;
    }

    // Récupère le nombre maximal de paramètres dans une méthode
    public int getMaxParameters() {
        return maxParameters;
    }

    // Récupère le nombre de méthodes par classe
    public Map<String, Integer> getMethodsPerClass() {
        return methodsPerClass;
    }

    // Récupère le nombre d'attributs par classe
    public Map<String, Integer> getAttributesPerClass() {
        return attributesPerClass;
    }

    // Récupère la liste des packages
    public Set<String> getPackageNames() {
        return packageNames;
    }

    // Récupère le graphe d'appel
    public Map<String, Set<String>> getCallGraph() {
        return callGraph;
    }
}
