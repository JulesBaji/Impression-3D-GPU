package fc.PrintingApplication.TP2;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_KEEP;
import static org.lwjgl.opengl.GL11.GL_INCR;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_NOTEQUAL;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glStencilOp;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.Line;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.builder.Face;
import com.owens.oobjloader.builder.FaceVertex;
import com.owens.oobjloader.parser.Parse;

import fc.ChewTriangulation.Pnt;
import fc.ChewTriangulation.Triangulation;
import fc.GLObjects.GLProgram;
import fc.GLObjects.GLRenderTarget;
import fc.GLObjects.GLShaderMatrixParameter;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Color;
import fc.Math.*;

public class Main
{
	//
	// Dans ce TP
	// - rasteriser les perimetres (remplissage de polygone arbitraire)
	// - rasteriser avec la methode de Guthe et al.
	//
	private static final float taillePixel = .05f;
	
	public static void checkGLErrorState()
	{
		int err = GL11.glGetError();
		if (err != 0)
			throw new IllegalStateException("OpenGL is in error state " + err);
	}
	
	public static void main(String[] args)
	{
		glfwInit();
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		long window = glfwCreateWindow(100, 100, "Dummy", NULL, NULL);
		
		glfwMakeContextCurrent(window);
		GL.createCapabilities();

		GLRenderTarget rt = new GLRenderTarget(600, 400, GL30.GL_RGBA32I, GL30.GL_RGBA_INTEGER, GL11.GL_INT);
		
		GLProgram shader = new GLProgram()
		{
			@Override
			protected void preLinkStep()
			{
				glBindAttribLocation(m_ProgramId, 0, "in_Position");
			}
		};
		shader.init(new MyVShader(), new MyFShader());
		
		GLShaderMatrixParameter matParam = new GLShaderMatrixParameter("u_mvpMatrix");
		matParam.init(shader);
		
		GLVec2iTriangle tri = new GLVec2iTriangle(new Vec2i[]{new Vec2i(0,0), new Vec2i(599,0), new Vec2i(300,300)});
		parseObjFile("3d_models/yoda.obj");
		
		GL11.glViewport(0, 0, 600, 400);
		rt.bind();
        glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		shader.begin();
		matParam.set(Matrix.createOrtho(0, 600, 0, 400, -1, 1));
		tri.render();
		// for (GLVec2iTriangle tri : triangles) tri.render();
		shader.end();
		rt.unbind();
		
		float[][][] pixels = rt.readBackAsFloat();
		
		BufferedImage img = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
		
		for (int y=0; y < pixels.length; y++)
		{
			for (int x=0; x < pixels[0].length; x++)
			{
				int r = (int)(pixels[y][x][0] * 255.0f);
				int g = (int)(pixels[y][x][1] * 255.0f);
				int b = (int)(pixels[y][x][2] * 255.0f);

				img.setRGB(x, y, (r<<16) | (g<<8) | b);
			}
		}
		rt.dispose();
		
	    File outputFile = new File(System.getProperty("user.dir"), "result.png");
	    try
	    {
	    	ImageIO.write(img, "png", outputFile);
	    }
	    catch (IOException e)
	    {
	    	System.out.println("Error, IOException caught: " + e.toString());
	    }
	}
	
	public static void parseObjFile(String filename)
	{
	    try
	    {
            Build builder = new Build();
            Parse obj = new Parse(builder, new File(filename).toURI().toURL());
            AABB aabb = new AABB();    

            for (FaceVertex vertex : builder.faceVerticeList)
            {
                float x = vertex.v.x;
                float y = vertex.v.y;
                float z = vertex.v.z;
                
                aabb.enlarge(new Vec3f(x, y, z));
            }

            aabb.addMargin(new Vec3f(10, 10, 0)); 
            int imageWidth = (int) ((aabb.getMax().x - aabb.getMin().x) / taillePixel);
            int imageHeight = (int) ((aabb.getMax().y - aabb.getMin().y) / taillePixel);

			// List<GLVec2iTriangle> triangles = new ArrayList<>();
			// boolean uneCoupe = false;

            // Périmètre de l'objet dans le plan xy selon z
            for(float z = aabb.getMin().z; z <= aabb.getMax().z; z += 0.2)
            {
				// if (uneCoupe) break;
				// uneCoupe = true;
                coupe(imageWidth, imageHeight, builder, z, aabb);
            }
			// return triangles;
	    }
	    catch (java.io.FileNotFoundException e)
	    {
	    	System.out.println("FileNotFoundException loading file "+filename+", e=" + e);
	        e.printStackTrace();
			// return new ArrayList<>();
	    }
	    catch (java.io.IOException e)
	    {
	    	System.out.println("IOException loading file "+filename+", e=" + e);
	        e.printStackTrace();
			// return new ArrayList<>();
	    }
	}

    private static void coupe(int imageWidth, int imageHeight, Build builder, float z, AABB aabb) {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.translate(imageWidth/2, imageHeight/2);

		List<Point2D> triangles = new ArrayList<>();
		List<Point2D> sommets = new ArrayList<>();  
		List<Line2D> aretes = new ArrayList<>();

        // Parcours des faces et calcul des segments
        for (Face face : builder.faces)
        {     
            List<Point2D> sommetsTest = new ArrayList<>();  
            for (int i = 1; i <= (face.vertices.size() - 2); i++)
            {
                int vertexIndex1 = face.vertices.get(0).index;
                int vertexIndex2 = face.vertices.get(i).index;
                int vertexIndex3 = face.vertices.get(i + 1).index;

                FaceVertex vertex1 = builder.faceVerticeList.get(vertexIndex1);
                FaceVertex vertex2 = builder.faceVerticeList.get(vertexIndex2);
                FaceVertex vertex3 = builder.faceVerticeList.get(vertexIndex3);

                // Intersection des segments avec le plan Z
                intersectionSegment(z, vertex1, vertex2, sommetsTest);
                intersectionSegment(z, vertex2, vertex3, sommetsTest);
                intersectionSegment(z, vertex3, vertex1, sommetsTest);
            }

			sommets.addAll(sommetsTest);
            for (int i = 0; i < sommetsTest.size() - 1; i++) {
                Point2D point1 = sommetsTest.get(i);
                Point2D point2 = sommetsTest.get(i + 1);
                aretes.add(new Line2D.Float(point1, point2));
            }
        }

		triangles = triangulationDelaunay(sommets, imageWidth, imageHeight, aretes);
		for (int i = 0; i < triangles.size() - 1; i++) {
			Point2D point1 = triangles.get(i);
			Point2D point2 = triangles.get(i + 1);
			g2d.draw(new Line2D.Double(point1.getX()/taillePixel, point1.getY()/taillePixel, point2.getX()/taillePixel, point2.getY()/taillePixel));
			g2d.draw(new Line2D.Double(point1.getX()/taillePixel, point1.getY()/taillePixel, point1.getX()/taillePixel, point1.getY()/taillePixel));
			g2d.draw(new Line2D.Double(point2.getX()/taillePixel, point2.getY()/taillePixel, point2.getX()/taillePixel, point2.getY()/taillePixel));
		}

        // Sauvegarde de l’image
        try { 
            File dir = new File("coupes");
            if (!dir.exists()) dir.mkdir();
            ImageIO.write(image, "png", new File("coupes/" + "resultat_" + z + ".png"));
        } catch (Exception e) { 
            e.printStackTrace(); 
        }


		// return triangles;
    }

	public static void intersectionSegment(float z, FaceVertex v1, FaceVertex v2, List<Point2D> sommetsTest) 
    {
        if ((v1.v.z <= z && v2.v.z >= z) || (v1.v.z >= z && v2.v.z <= z)) 
        {
            float t = (z - v1.v.z) / (v2.v.z - v1.v.z);
            float x = v1.v.x + t * (v2.v.x - v1.v.x);
            float y = v1.v.y + t * (v2.v.y - v1.v.y);
            sommetsTest.add(new Point2D.Float(x, y));
        }
    }  

	public static List<Point2D> triangulationDelaunay(List<Point2D> sommetsTest, int imageWidth, int imageHeight, List<Line2D> aretes) {
		// Convertir les points 2D en objets Pnt compatibles avec votre triangulation
		List<Pnt> points = new ArrayList<>();
		for (Point2D point : sommetsTest) {
			if (!Double.isNaN(point.getX()) && !Double.isNaN(point.getY())) {
				float X = (float) point.getX();
				float Y = (float) point.getY();
				points.add(new Pnt(X, Y));
			}
		}
	
		// Filtrer les points trop proches
		List<Pnt> filteredPoints = new ArrayList<>();
		double threshold = 1e-5; // Distance minimale entre deux points
		for (Pnt p : points) {
			boolean isTooClose = false;
			for (Pnt existing : filteredPoints) {
				double distance = Math.sqrt(
					Math.pow(p.coord(0) - existing.coord(0), 2) + 
					Math.pow(p.coord(1) - existing.coord(1), 2)
				);
				if (distance < threshold) {
					isTooClose = true;
					break;
				}
			}
			if (!isTooClose) {
				filteredPoints.add(p);
			}
		}
	
		List<Point2D> sommetsTriangles = new ArrayList<>();
	
		// Créer un triangle initial pour la triangulation
		Pnt superP1 = new Pnt(-50, 50);
		Pnt superP2 = new Pnt(50, 50);
		Pnt superP3 = new Pnt(0, -50);
		fc.ChewTriangulation.Triangle tri = new fc.ChewTriangulation.Triangle(superP1, superP2, superP3);
	
		// Créer la triangulation et ajouter les points un par un
		Triangulation triangulation = new Triangulation(tri);
		for (Pnt p : filteredPoints) {
			triangulation.delaunayPlace(p); // Placer chaque point dans la triangulation
		}
	
		// Créer une liste pour stocker tous les triangles
		List<GLVec2iTriangle> triangles = new ArrayList<>();
	
		// Parcourir tous les triangles dans la triangulation
		for (fc.ChewTriangulation.Triangle t : triangulation.triGraph.nodeSet()) {
			// Extraire les points du triangle
			Pnt p1 = t.get(0);
			Pnt p2 = t.get(1);
			Pnt p3 = t.get(2);
	
			// Vérifier si le triangle contient un sommet du super-triangle
			if (p1.equals(superP1) || p1.equals(superP2) || p1.equals(superP3) ||
				p2.equals(superP1) || p2.equals(superP2) || p2.equals(superP3) ||
				p3.equals(superP1) || p3.equals(superP2) || p3.equals(superP3)) {
				continue; // Ignorer ce triangle
			}
	
			// Calculer le centroïde du triangle
			double centroideX = (p1.coord(0) + p2.coord(0) + p3.coord(0)) / 3;
			double centroideY = (p1.coord(1) + p2.coord(1) + p3.coord(1)) / 3;
			Point2D centroide = new Point2D.Double(centroideX, centroideY);
	
			// Vérifier si le triangle est à l'intérieur du contour
			if (!isInsideContour(centroide, aretes)) continue;

			// Ajouter les sommets du triangle à la liste
			sommetsTriangles.add(new Point2D.Double(p1.coord(0), p1.coord(1)));
			sommetsTriangles.add(new Point2D.Double(p2.coord(0), p2.coord(1)));
			sommetsTriangles.add(new Point2D.Double(p3.coord(0), p3.coord(1)));

			// Créer des objets Vec2i à partir des points du triangle
			Vec2i v1 = new Vec2i((int) p1.coord(0), (int) p1.coord(1));
			Vec2i v2 = new Vec2i((int) p2.coord(0), (int) p2.coord(1));
			Vec2i v3 = new Vec2i((int) p3.coord(0), (int) p3.coord(1));

			// Créer un GLVec2iTriangle avec les trois points
			GLVec2iTriangle glTriangle = new GLVec2iTriangle(new Vec2i[]{v1, v2, v3});

			// Ajouter le triangle à la liste
			triangles.add(glTriangle);
		}
	
		// Retourner les sommets des triangles qui sont dans le contour
		return sommetsTriangles;
	}
	
	public static boolean isInsideContour(Point2D centroide, List<Line2D> aretes) {
		// Créer un rayon partant du centroïde, à une direction aléatoire
		Line2D rayon = new Line2D.Double(centroide, new Point2D.Double(centroide.getX() + 10000, centroide.getY() + 100));

		// Compteur d'intersections
		int intersectionCount = 0;

		// Vérifier les intersections avec les arêtes
		for (Line2D arete : aretes) {
			if (rayon.intersectsLine(arete)) {
				intersectionCount++;
			}
		}

		// Si le compteur est impair, le point est à l'intérieur du contour
		return intersectionCount % 2 != 0;
	}

	public static boolean doesRayIntersectSegment(Line2D rayon, Line2D arete) {
		double x1 = rayon.getX1(), y1 = rayon.getY1();
		double x2 = rayon.getX2(), y2 = rayon.getY2();
		double x3 = arete.getX1(), y3 = arete.getY1();
		double x4 = arete.getX2(), y4 = arete.getY2();
	
		double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (denominator == 0) return false; // Les lignes sont parallèles
	
		double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denominator;
		double u = ((x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2)) / denominator;
	
		// Vérifier si l'intersection est dans les segments
		return t >= 0 && t <= 1 && u >= 0 && u <= 1;
	}
	

}
