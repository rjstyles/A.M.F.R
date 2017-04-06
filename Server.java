
	import java.net.*;
	import java.io.*;
	import java.awt.Graphics;
	import java.awt.Image;
	import javax.imageio.ImageIO;
	import java.awt.image.WritableRaster;
	import java.awt.image.BufferedImage;
	import java.io.ObjectOutputStream;

	public class Server {

		//converting int array to image ...
		public static BufferedImage getImageFromArray(int[] pixels, int width, int height) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			try {
				WritableRaster raster = (WritableRaster) image.getData();
				raster.setPixels(0,0,width,height,pixels);
				image.setData(raster);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			return image;
		}


		// saving image...
		public static void SaveImage(BufferedImage img) {
			try {

			    ImageIO.write(img, "jpg", new File("./face.jpg"));

			} catch (IOException e) {

			    System.out.println("Image could not be read");
			    System.exit(1);
			}
	    	}


		// int array writer
		private static void writeInts(OutputStream out, int[] ints) throws IOException {
			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.writeInt(ints.length);
			for (int e : ints) dataOut.writeInt(e);
			dataOut.flush();
	    	}


		// int array reader
		private static int[] readInts(InputStream in) throws IOException {
			DataInputStream dataIn = new DataInputStream(in);
			int[] ints = new int[dataIn.readInt()];
			for (int i = 0; i < ints.length; ++i) ints[i] = dataIn.readInt();
			return ints;
		}


		// recognizing face...
		private static String recognizeFace() {
			String line =  "";
			try {
				Runtime r = Runtime.getRuntime();
				Process p = r.exec("python pyfacesdemo ./face.jpg ../TrainingImages 15 3");
				p.waitFor();
				BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
				line = b.readLine();
				System.out.println(line);
				/*
				while ((line = b.readLine()) != null) {
					System.out.println(line);
				}
				*/
				b.close();
			} catch(Exception e) {
				System.out.println(e.toString());
			}
			return line;
		}


		public static void main(String[] args) throws Exception {

			// starting server...
			System.out.println("Starting server...");
			ServerSocket ss = new ServerSocket(9000);

			// connecting to client
			System.out.println("Waiting for client...");
			Socket s = ss.accept();

			System.out.println("Client connected..!!");

			while (true) {
				// receiving data from client...
				InputStream is = s.getInputStream();
				int data[] = readInts(is);
				int l = data.length;

				System.out.println("Received ...");

				BufferedImage face = getImageFromArray(data, (int)Math.sqrt(l), (int)Math.sqrt(l));
				SaveImage(face);

				// recognizing..

				String name = recognizeFace();


				//sending name...

				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(name);
			}
		}
	}
