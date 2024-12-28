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
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
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

import java.awt.geom.Point2D;
import java.awt.Color;
import fc.Math.*;

public class Main
{
	private static final float taillePixel = .05f;
	
	public static void checkGLErrorState()
	{
		int err = GL11.glGetError();
		if (err != 0)
			throw new IllegalStateException("OpenGL is in error state " + err);
	}
	
	public static void main(String[] args) throws FileNotFoundException, MalformedURLException, IOException {
		glfwInit();
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
	
		// Initialisez les dimensions de l'image
		Build builder = new Build();
		String filename = "3d_models/Cubewithhole.obj";
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

		long window = glfwCreateWindow(imageWidth, imageHeight, "Dummy", NULL, NULL);
	
		// Activer le contexte OpenGL
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
	
		// Charger les triangles après avoir activé OpenGL
		List<List<GLVec2fTriangle>> trianglesCoupes = parseObjFile(filename, imageWidth, imageHeight, aabb);
	
		GLRenderTarget rt = new GLRenderTarget(imageWidth, imageHeight, GL30.GL_RGBA32I, GL30.GL_RGBA_INTEGER, GL11.GL_INT);
	
		GLProgram shader = new GLProgram() {
			@Override
			protected void preLinkStep() {
				glBindAttribLocation(m_ProgramId, 0, "in_Position");
			}
		};
		shader.init(new MyVShader(), new MyFShader());
	
		GLShaderMatrixParameter matParam = new GLShaderMatrixParameter("u_mvpMatrix");
		matParam.init(shader);
	
		GL11.glViewport(0, 0, imageWidth, imageHeight);
	
		// Parcours des coupes et rendu
		for (int coupeIndex = 0; coupeIndex < trianglesCoupes.size(); coupeIndex++) {
			List<GLVec2fTriangle> coupe = trianglesCoupes.get(coupeIndex);
	
			rt.bind();
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
	
			shader.begin();
			matParam.set(Matrix.createOrtho(-imageWidth / 2, imageWidth / 2, -imageHeight / 2, imageHeight / 2, -1, 1));
	
			// Rasteriser les triangles
			for (GLVec2fTriangle tri : coupe) {
				tri.render();
			}
	
			shader.end();
			rt.unbind();
	
			// Lire les pixels du rendu
			float[][][] pixels = rt.readBackAsFloat();
	
			// Créer l'image pour cette coupe
			BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	
			for (int y = 0; y < pixels.length; y++) {
				for (int x = 0; x < pixels[0].length; x++) {
					int r = (int) (pixels[y][x][0] * 255.0f);
					int g = (int) (pixels[y][x][1] * 255.0f);
					int b = (int) (pixels[y][x][2] * 255.0f);
					img.setRGB(x, y, (r << 16) | (g << 8) | b);
				}
			}
	
			// Créer un dossier 'results' s'il n'existe pas déjà
			File resultsDir = new File(System.getProperty("user.dir"), "results");
			if (!resultsDir.exists()) {
				resultsDir.mkdir();  // Crée le dossier si nécessaire
			}

			// Sauvegarder l'image pour cette coupe dans le dossier 'results'
			File outputFile = new File(resultsDir, "result_coupe_" + coupeIndex + ".png");
			try {
				ImageIO.write(img, "png", outputFile);
			} catch (IOException e) {
				System.out.println("Error, IOException caught: " + e.toString());
			}
		}
		rt.dispose();
	}	
	
	public static List<List<GLVec2fTriangle>> parseObjFile(String filename, int imageWidth, int imageHeight, AABB aabb)
	{
	    try
	    {
            Build builder = new Build();
            Parse obj = new Parse(builder, new File(filename).toURI().toURL());
			List<List<GLVec2fTriangle>> trianglesCoupes = new ArrayList<>();

            // Périmètre de l'objet dans le plan xy selon z
            for(float z = aabb.getMin().z; z <= aabb.getMax().z; z += 0.2)
            {
                trianglesCoupes.add(coupe(imageWidth, imageHeight, builder, z, aabb));
            }
			return trianglesCoupes;
	    }
	    catch (java.io.FileNotFoundException e)
	    {
	    	System.out.println("FileNotFoundException loading file "+filename+", e=" + e);
	        e.printStackTrace();
			return new ArrayList<>();
	    }
	    catch (java.io.IOException e)
	    {
	    	System.out.println("IOException loading file "+filename+", e=" + e);
	        e.printStackTrace();
			return new ArrayList<>();
	    }
	}

    private static List<GLVec2fTriangle> coupe(int imageWidth, int imageHeight, Build builder, float z, AABB aabb) throws IOException {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.translate(imageWidth/2, imageHeight/2);

		List<Point2D> sommets = new ArrayList<>();  

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
            // for (int i = 0; i < sommetsTest.size() - 1; i++) {
            //     Point2D point1 = sommetsTest.get(i);
            //     Point2D point2 = sommetsTest.get(i + 1);
			// 	g2d.draw(new Line2D.Double(point1.getX()/taillePixel, point1.getY()/taillePixel, point2.getX()/taillePixel, point2.getY()/taillePixel));
            // }
        }

		List<Point2D> sommetsTriangles = new ArrayList<>();
		List<GLVec2fTriangle> triangles = triangulationDelaunay(sommets, imageWidth, imageHeight, taillePixel, sommetsTriangles);
		// for (int i = 0; i < sommetsTriangles.size() - 1; i++) {
		// 	Point2D point1 = sommetsTriangles.get(i);
		// 	Point2D point2 = sommetsTriangles.get(i + 1);
		// 	g2d.draw(new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
		// 	g2d.draw(new Line2D.Double(point1.getX(), point1.getY(), point1.getX(), point1.getY()));
		// 	g2d.draw(new Line2D.Double(point2.getX(), point2.getY(), point2.getX(), point2.getY()));
		// }

        // Sauvegarde de l’image
        // try { 
        //     File dir = new File("coupes");
        //     if (!dir.exists()) dir.mkdir();
        //     ImageIO.write(image, "png", new File("coupes/" + "resultat_" + z + ".png"));
        // } catch (Exception e) { 
        //     e.printStackTrace(); 
        // }

		return triangles;
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

	public static List<GLVec2fTriangle> triangulationDelaunay(List<Point2D> sommetsTest, int imageWidth, int imageHeight, float taillePixel, List<Point2D> sommetsTriangles) {
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
		List<GLVec2fTriangle> triangles = new ArrayList<>();
	
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

			// Créer des objets Vec2f à partir des points du triangle
			Vec2f v1 = new Vec2f((float) p1.coord(0)/taillePixel, (float) p1.coord(1)/taillePixel);
			Vec2f v2 = new Vec2f((float) p2.coord(0)/taillePixel, (float) p2.coord(1)/taillePixel);
			Vec2f v3 = new Vec2f((float) p3.coord(0)/taillePixel, (float) p3.coord(1)/taillePixel);

			// Créer un GLVec2fTriangle avec les trois points
			GLVec2fTriangle glTriangle = new GLVec2fTriangle(new Vec2f[]{v1, v2, v3});

			// Ajouter le triangle à la liste
			triangles.add(glTriangle);

			sommetsTriangles.add(new Point2D.Double(p1.coord(0)/taillePixel, p1.coord(1)/taillePixel));
			sommetsTriangles.add(new Point2D.Double(p2.coord(0)/taillePixel, p2.coord(1)/taillePixel));
			sommetsTriangles.add(new Point2D.Double(p3.coord(0)/taillePixel, p3.coord(1)/taillePixel));
		}
	
		// Retourner les sommets des triangles qui sont dans le contour
		return triangles;
	}
}
