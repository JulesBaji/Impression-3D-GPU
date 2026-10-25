# Impression 3D : découpe en couches (prototype)

Projet d'impression 3D du master Informatique ISICG (Université de Limoges, 2024), en Java avec OpenGL (LWJGL).

**Prototype inachevé.** Il prépare un modèle 3D pour une imprimante en le découpant en couches.

## Ce qui est implémenté

- Chargement d'un maillage OBJ et calcul de sa boîte englobante.
- **Découpe en couches** : intersection des triangles du maillage avec des plans horizontaux.
- **Triangulation de Delaunay** des points de chaque coupe, avec la bibliothèque de triangulation de Paul Chew (fournie).
- **Rastérisation GPU** des triangles de chaque coupe dans une image (OpenGL hors écran).
- Remise en ordre des points du contour et **simplification de Ramer-Douglas-Peucker**.

## Limites connues

- La triangulation de Delaunay porte sur les points de la coupe sans tenir compte du contour : des triangles apparaissent hors de la pièce et les trous ne sont pas respectés.
- Le remplissage de l'intérieur des couches (méthode de Guthe et al.) n'est pas implémenté.
- Le modèle chargé et la coupe affichée sont fixés dans le code (`Main.java`).

## Compilation

Projet Maven (Eclipse ou VS Code avec l'extension Java) : importer les dossiers `fc.*`, puis lancer `fc.PrintingApplication.TP2.Main`. Les modèles sont dans `3d_models/`.

Le squelette (bibliothèques `fc.Math`, `fc.GLObjects`, chargeur OBJ) est fourni par l'université.

## Technologies

Java, OpenGL (LWJGL), Maven.
